package com.example.scalaspringexperiment.dao

import scala.annotation.StaticAnnotation

class Column(
  val name: String
) extends StaticAnnotation

case class ColumnMeta(
  name: String
)

object Column {
  import scala.quoted.*

  inline def extractColumns[T]: Seq[ColumnMeta] = ${
    extractColumnMetadataImpl[T]
  }

  def extractColumnMetadataImpl[T: Type](using q: Quotes): Expr[List[ColumnMeta]] = {
    import q.reflect.*

    val tpe = TypeRepr.of[T]
    val sym = tpe.typeSymbol

    val constructorParams = sym.primaryConstructor.paramSymss.flatten

    val metas: List[Expr[ColumnMeta]] = constructorParams.flatMap { param =>
      param.annotations.collect {
        case Apply(_, List(Literal(StringConstant(colName))))
          if param.annotations.exists(_.tpe =:= TypeRepr.of[Column]) =>
          println(s"[Macro] Found Column($colName) on param ${param.name}")
          '{ ColumnMeta(name = ${ Expr(colName) }) }
      }
    }

    Expr.ofList(metas)
  }
}
