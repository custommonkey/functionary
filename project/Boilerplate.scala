import sbt.File
import sbt.io.IO.{createDirectory, writeLines}

import scala.util.chaining.scalaUtilChainingOps

object Boilerplate {

  case class Parts(
      range: Range,
      name: String,
      typeParams: String,
      params: String,
      predicates: String,
      predicateNames: String,
      paramsNames: String,
      typ: String,
      compare: String,
      applyPredicates: String,
      description: String,
      tuple: String,
      wildcards: String,
      args: String,
      equalThings: String
  )

  class Arity(val value: Int) extends AnyVal

  private def all(f: Int => String)(implicit arity: Arity): String =
    (1 to arity.value).map(f).mkString(", ")

  object Parts {
    def apply(arity: Int): Parts = {
      implicit val a = new Arity(arity)
      val range = 1 to arity
      val name = s"MockFunction$arity"
      def and(f: Int => String) = range.map(f).mkString(" && ")
      val typeParams = all(i => s"V$i")
      val equals = all(i => s"v$i.equals")
      new Parts(
        range = range,
        name = name,
        typeParams = typeParams,
        equalThings = equals,
        params = all(i => s"v$i: V$i"),
        predicates = all(i => s"p$i: V$i => Boolean"),
        predicateNames = all(i => s"p$i"),
        paramsNames = all(i => s"v$i"),
        typ = s"$name[$typeParams, R]",
        compare = and(i => s"this.v$i(v$i)"),
        applyPredicates = and(i => s"p$i(v$i)"),
        description = all(i => s"v$i.toString"),
        tuple = all(i => s"Param.Value(t._1._$i)"),
        wildcards = all(_ => "_"),
        args = all(i => s"args(${i - 1}).asInstanceOf[V$i]")
      )
    }
  }

  private val MaxArity = 2

  def gen(outDir: File): Seq[File] = {
    val extensionFile = new File(packageDir(outDir), "Folding.scala").tap {
      file =>

        val apiFunctions = (1 to MaxArity).flatMap { arity =>
          val parts = Parts(arity)
          import parts.*
          List(
            s"""
             |  def foldMock[$typeParams, R](f: A => $typ): $typ =
             |    functionary.foldMock[A, $typeParams, R](i)(f)
             |""".stripMargin
          )
        }
        writeLines(
          file,
          "package functionary" +:
            "private[functionary] trait Folding {" +:
            "  implicit class FoldsOps[A](i: Iterable[A]) {" +:
            apiFunctions :+
            "  }" :+
            "}"
        )
    }

    val apiFile = new File(packageDir(outDir), "GeneratedApi.scala").tap {
      file =>

        val apiFunctions = (1 to MaxArity).map { arity =>
          implicit val a: Arity = new Arity(arity)

          val parts = Parts(arity)
          import parts.*

          val x = if (arity == 1) {
            typeParams
          } else { s"($typeParams)" }

          val normalTuple = if (arity == 1) {
            "Param.Value(t._1)"
          } else tuple

          val values =
            (1 to arity).map { i => s"Param.Value(v$i)" }.mkString(", ")

          val xxx =
            (1 to arity).map { i => s"Param.Predicate(p$i)" }.mkString(", ")

          s"""
             |  def expects[$typeParams]($params)(implicit location: Location): Value$arity[$typeParams, Nothing] =
             |    new Value$arity[$typeParams, Nothing]($values, None, location, None)
             |
             |  def tuple[$typeParams, R](t: ($x, R))(implicit location: Location):  Value$arity[$typeParams, R]  =
             |    new Value$arity[$typeParams, R]($normalTuple, Some(t._2), location, None)
             |
             |  def expects[$typeParams]($predicates)(implicit location: Location): Value$arity[$typeParams, Nothing] =
             |    new Value$arity[$typeParams, Nothing]($xxx, None, location, None)
             |
             |  def returns[$typeParams, R](r: R)(implicit location: Location): Value$arity[$typeParams, R] =
             |    new Value$arity[$typeParams, R](
             |      $anys,
             |      Some(r),
             |      location,
             |      None
             |    )
             |
             |  def never[$typeParams, R](implicit location: Location): Value$arity[$typeParams, R] =
             |    new Value$arity($anys, None, location, None)
             |
             |  def combineAll[$typeParams, R](i: Iterable[$typ]): $typ =
             |    i.reduce(_ or _)
             |
             |  def combineAll[$typeParams, R](i: $typ*): $typ =
             |    i.reduce(_ or _)
             |
             |  def foldMock[A, $typeParams, R](i: Iterable[A])(f: A => $typ): $typ =
             |    combineAll(i.map(f))
             |
             |""".stripMargin
        }

        writeLines(
          file,
          "package functionary" +:
            "private[functionary] trait GeneratedApi {" +:
            apiFunctions :+
            "}"
        )
    }

    (1 to MaxArity).map { arity =>

      implicit val a: Arity = new Arity(arity)

      val parts = Parts(arity)
      import parts.*

      outFile(outDir, name).tap { file =>
        println(s"Making file $file")

        val values = (1 to arity).map { i => s"v$i: Param[V$i]" }.mkString(", ")
        val paramTypes = (1 to arity).map { i => s"Param[V$i]" }.mkString(", ")

        val typr = s"$name[$typeParams, RR]"

        val valueClass =
          s"""case class Value$arity[$typeParams, R](
             |  $values,
             |  returns: Option[R],
             |  location: Location,
             |  name: Option[String])
             |extends $typ {
             |
             |  override def matches($params): Option[R] =
             |    if ($compare) returns
             |    else None
             |
             |  def returns[RR](r: RR): $typr  =
             |    new Value$arity($paramsNames, Some(r), location, None)
             |
             |  override def value: List[($paramTypes)] = List(($paramsNames))
             |
             |  override def describe: List[String] = List($description)
             |
             |  override def locations: List[Location] = List(location)
             |
             |  def named(name: String): $typ =
             |    new Value$arity($paramsNames, returns, location, Some(name))
             |
             |}
             |""".stripMargin

        val orClass =
          s"""case class Or$arity[$typeParams, R](a: $typ, b: $typ) extends $typ {
             |  override def matches($params): Option[R] =
             |    a.matches($paramsNames).orElse(b.matches($paramsNames))
             |
             |  override def describe: List[String] = a.describe ++ b.describe
             |
             |  override def locations: List[Location] = a.locations ++ b.locations
             |
             |  override def value: List[($paramTypes)] = a.value ++ b.value
             |
             |}
             |""".stripMargin

        val moreParamsNames = all(i => s"$$v$i")

        val mockFunctionTrait =
          s"""sealed trait $typ extends(($typeParams) => R) with MockFunction[R] {
             |
             |  def matches($params): Option[R]
             |
             |  def describe: List[String]
             |
             |  def locations: List[Location]
             |
             |  def value: List[($paramTypes)]
             |
             |  def or(that: $typ): $typ = Or$arity(this, that)
             |
             |  override def apply(args: Seq[AnyRef]): R = apply($args)
             |
             |  def apply($params): R =
             |    matches($paramsNames) match {
             |      case Some(value) => value
             |      case None =>
             |        throw new AssertionError(
             |          s\"\"\"Expected $${value.mkString(", ")},
             |             |  defined at $${locations.mkString("\\n  ")}
             |             |  but was ($moreParamsNames)\"\"\".stripMargin
             |        )
             |    }
             |
             |  override def toString(): String =
             |    this match {
             |      case Value$arity($paramsNames, returnsOpt, _, nameOpt) =>
             |        val name = nameOpt.map(" " + _).getOrElse("")
             |        val returns = returnsOpt.map(_.toString).getOrElse("<nothing>")
             |        s"mock function$$name expects $moreParamsNames and returns $$returns"
             |      case Or$arity(a, b) => s"($$a) or ($$b)"
             |    }
             |
             |}
             |""".stripMargin

        writeLines(
          file,
          List(
            "package functionary",
            "",
            mockFunctionTrait,
            orClass,
            valueClass
          )
        )
      }
    } :+ apiFile :+ extensionFile
  }

  private def anys(implicit arity: Arity) =
    List.fill(arity.value)("Param.Any()").mkString(", ")

  private def packageDir(outDir: File) =
    new File(outDir, "functionary").tap(createDirectory)

  private def outFile(file: File, name: String) =
    new File(packageDir(file), s"$name.scala")

}
