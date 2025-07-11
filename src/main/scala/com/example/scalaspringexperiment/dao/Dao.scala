package com.example.scalaspringexperiment.dao

import cats.*
import cats.effect.*
import cats.implicits.*
import com.example.scalaspringexperiment.entity.DomainEntity
import doobie.*
import doobie.implicits.*
import doobie.util.fragment.*
import cats.data.Chain
import org.slf4j.Logger

object DaoUtils {

  private def getField[A](
    name: String,
    f: Fragment
  ): A = {
    val fragmentClass = classOf[Fragment]
    val field = fragmentClass.getDeclaredField(name)
    field.setAccessible(true)
    field.get(f).asInstanceOf[A]
  }

  /**
   * This is a workaround to adjust the properties of a Fragment.
   * Since Fragment is final and its sql and elems properties are protected, we're forced to use reflection here
   *
   * @param entity
   * @param r
   * @param w
   * @tparam T
   * @return
   */
  def insertValuesFr[T](
    entity: T,
    tableInfo: TableInfo[T]
  )(
    implicit r: Read[T],
    w: Write[T]
  ): Fragment = {
    val f = fr0"$entity"
    val fragmentClass = classOf[Fragment]

    val sql: String = getField[String]("sql", f)
    val elems: Chain[Elem] = getField[Chain[Elem]]("elems", f)

    val mask: Seq[Boolean] = tableInfo.columns.map(_.autoGenerated)

    val filteredElems = elems.toList.zip(mask).collect {
      case (e, false) => e
    }

    val filteredSql = filteredElems.map(_ => "?").mkString(",")
    Fragment(filteredSql, filteredElems)
  }

  def updateValuesFr[T](
    entity: T,
    tableInfo: TableInfo[T]
  )(
    implicit r: Read[T],
    w: Write[T]
  ): Fragment = {
    val f = fr0"$entity"
    val sql: String = getField[String]("sql", f)
    val elems: Chain[Elem] = getField[Chain[Elem]]("elems", f)

    val mask: Seq[Boolean] = tableInfo.columns.map(_.autoGenerated)

    val updateFrs: List[Fragment] = elems.toList.zip(tableInfo.columns)
      .filterNot { (_, e) => e.autoGenerated }
      .map { (elem, col) =>
      Fragment(s"${col.name} = ?", List(elem))
    }

    updateFrs.intercalate(fr", ")
  }
}

trait Dao[T <: DomainEntity] {
  val logger: Logger
  val ds: Resource[IO, DataSourceTransactor[IO]]
  val tableInfo: TableInfo[T]

  implicit val reader: Read[T]
  implicit val writer: Write[T]

  // TODO - figure out how to get this working again
  // implicit val logHandler: LogHandler = LogHandler(evt => logger.info(evt.toString))

  def insert(
    model: T
  ): IO[T] = ds.use { xa =>
    val theTableName = Fragment.const0(tableInfo.table.name)
    val theInsertCols = Fragment.const0(tableInfo.insertColumnNames.mkString(","))
    val insertValues = DaoUtils.insertValuesFr(model, tableInfo)
    for {
      sql <- IO(sql"INSERT INTO $theTableName ($theInsertCols) VALUES ($insertValues)")
      query <- IO(sql.update.withUniqueGeneratedKeys[T](tableInfo.columnNames *))
      result <- query.transact(xa)
    } yield result
  }

  def update(
    model: T
  ): IO[T] = ds.use { xa =>
    val theTableName = Fragment.const0(tableInfo.table.name)
    for {
      query <- IO(sql"UPDATE $theTableName SET ${DaoUtils.updateValuesFr(model, tableInfo)} WHERE id = ${model.id}")
      result <- query.update.withUniqueGeneratedKeys[T](tableInfo.columnNames *).transact(xa)
    } yield result
  }

  def delete(
    entity: T
  ): IO[Int] = deleteById(entity.id)

  def deleteById(
    id: Long
  ): IO[Int] = ds.use { xa =>
    val theTableName = Fragment.const0(tableInfo.table.name)
    for {
      query <- IO(sql"DELETE FROM $theTableName WHERE id = $id")
      result <- query.update.run.transact(xa)
    } yield result
  }

  def findById(
    id: Long
  ): IO[Option[T]] = ds.use { xa =>
    (Fragment.const(s"select * from ${tableInfo.table.name} where id = ") ++ fr"$id LIMIT 1").query[T]
      .option
      .transact(xa)
  }
}
