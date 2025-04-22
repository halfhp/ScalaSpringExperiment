package com.example.scalaspringexperiment.dao

case class TableInfo[T](
  columns: Seq[ColumnMeta],
) {
  lazy val columnNames: Seq[String] = columns.map(_.name)
}

object TableInfo {
  inline def build[T](): TableInfo[T] = {
    TableInfo[T](
      columns = Column.extractColumns[T]
    )
  }
}

