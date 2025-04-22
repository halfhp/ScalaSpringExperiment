package com.example.scalaspringexperiment.dao

import scala.annotation.StaticAnnotation

class Table(
  val name: String
) extends StaticAnnotation

case class TableMeta(
  name: String
)

object Table {
  import scala.quoted.*

  inline def extractTableMeta[T]: TableMeta = ${
    extractTableMetaImpl[T]
  }

  def extractTableMetaImpl[T: Type](using Quotes): Expr[TableMeta] = {
    import quotes.reflect.*

    val tpe = TypeRepr.of[T]
    val symbol = tpe.typeSymbol

    println(s"[Macro] Extracting from type: ${symbol.fullName}")

    symbol.annotations.foreach { ann =>
      println(s"[Macro]  Annotation: ${ann.show}")
    }

    val maybeMeta: Option[Expr[TableMeta]] = symbol.annotations.collectFirst {
      // Match @Table("...") as: new Table("foo")
      case Apply(Select(New(tpt), _), List(Literal(StringConstant(tableName))))
        if tpt.tpe =:= TypeRepr.of[Table] =>
        println(s"[Macro] Found @Table with name: $tableName")
        '{ TableMeta(name = ${ Expr(tableName) }) }
    }

    maybeMeta.getOrElse {
      report.throwError(s"No valid @Table annotation found on ${symbol.fullName}")
    }
  }
}


