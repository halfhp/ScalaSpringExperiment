package com.example.scalaspringexperiment.dao

case class TableInfo[T](
  table: TableMeta,
  columns: Seq[ColumnMeta],
) {
  lazy val columnNames: Seq[String] = columns.map(_.name)
}

object TableInfo {
  inline def build[T](): TableInfo[T] = {
    TableInfo[T](
      table = Table.extractTableMeta[T],
      columns = Column.extractColumns[T]
    )
  }
}

