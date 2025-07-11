package com.example.scalaspringexperiment.dao

import scala.annotation.StaticAnnotation

class Column(
  val name: String,
  val autoGenerated: Boolean = false
) extends StaticAnnotation

case class ColumnMeta(
  name: String,
  autoGenerated: Boolean
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
        // Match @Column("name", autoGenerated = true)
        case Apply(Select(New(tpt), _), args)
          if tpt.tpe =:= TypeRepr.of[Column] && args.nonEmpty =>

          val nameExprOpt = args.headOption.collect {
            case Literal(StringConstant(name)) => Expr(name)
          }

          val autoGenExprOpt = args.lift(1).collect {
            case Literal(BooleanConstant(auto)) => Expr(auto)
          }

          (nameExprOpt, autoGenExprOpt) match {
            case (Some(nameExpr), Some(autoExpr)) =>
              println(s"[Macro] Found Column(name = ${nameExpr.valueOrAbort}, autoGenerated = ${autoExpr.valueOrAbort}) on param ${param.name}")
              '{ ColumnMeta(name = $nameExpr, autoGenerated = $autoExpr) }

            case (Some(nameExpr), None) =>
              println(s"[Macro] Found Column(name = ${nameExpr.valueOrAbort}, autoGenerated = false) on param ${param.name}")
              '{ ColumnMeta(name = $nameExpr, autoGenerated = false) }

            case _ =>
              report.throwError(s"Invalid @Column annotation on ${param.name}")
          }
      }
    }

    Expr.ofList(metas)
  }


  //  def extractColumnMetadataImpl[T: Type](using q: Quotes): Expr[List[ColumnMeta]] = {
//    import q.reflect.*
//
//    val tpe = TypeRepr.of[T]
//    val sym = tpe.typeSymbol
//
//    val constructorParams = sym.primaryConstructor.paramSymss.flatten
//
//    val metas: List[Expr[ColumnMeta]] = constructorParams.flatMap { param =>
//      param.annotations.collect {
//        case Apply(_, List(Literal(StringConstant(colName))))
//          if param.annotations.exists(_.tpe =:= TypeRepr.of[Column]) =>
//          println(s"[Macro] Found Column($colName) on param ${param.name}")
//          '{ ColumnMeta(name = ${ Expr(colName) }) }
//      }
//    }
//
//    Expr.ofList(metas)
//  }
}
