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
      moreParamsNames: String,
      typ: String,
      compare: String,
      applyPredicates: String,
      description: String,
      tuple: String,
      wildcards: String
  )

  object Parts {
    def apply(arity: Int): Parts = {
      val range = (1 to arity)
      val name = s"MockFunction$arity"
      def all(f: Int => String) = range.map(f).mkString(", ")
      def and(f: Int => String) = range.map(f).mkString(" && ")
      val typeParams = all(i => s"V$i")
      new Parts(
        range,
        name,
        typeParams,
        all(i => s"v$i: V$i"),
        all(i => s"p$i: V$i => Boolean"),
        all(i => s"p$i"),
        all(i => s"v$i"),
        all(i => s"$$v$i"),
        s"$name[$typeParams, R]",
        and(i => s"v$i == this.v$i"),
        and(i => s"p$i(v$i)"),
        all(i => s"v$i.toString"),
        all(i => s"t._1._$i"),
        all(_ => "_")
      )
    }
  }

  def gen(outDir: File): Seq[File] = {
    val extensionFile = new File(packageDir(outDir), "Folding.scala").tap {
      file =>

        val apiFunctions = (1 to 5).flatMap { arity =>
          val parts = Parts(arity)
          import parts._
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

        val apiFunctions = (1 to 5).map { arity =>
          val parts = Parts(arity)
          import parts._

          val x = if (arity == 1) {
            typeParams
          } else { s"($typeParams)" }
          val y = if (arity == 1) {
            "t._1"
          } else tuple

          s"""
                |  def expects[$typeParams]($params)(implicit location: Location): Returns$arity[$typeParams] =
                |    new PartialExpect$arity[$typeParams]($paramsNames, location)
                |
                |  def tuple[$typeParams, R](t: ($x, R))(implicit location: Location): $typ =
                |    new Value$arity[$typeParams, R]($y, t._2, location)
                |
                |  def expects[$typeParams]($predicates)(implicit location: Location): Returns$arity[$typeParams] =
                |    new PartialPredicate$arity[$typeParams]($predicateNames, location)
                |
                |  def expectsAny[$typeParams](implicit location: Location): Returns$arity[$typeParams] =
                |    new ExpectAny$arity[$typeParams](location)
                |
                |  def never[$typeParams, R](implicit location: Location): $typ =
                |    new Never$arity(location)
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

    (1 to 5).map { arity =>

      val parts = Parts(arity)
      import parts._

      outFile(outDir, name).tap { file =>
        println(s"Making file $file")

        val predicateClass =
          s"""private case class Predicate$arity[$typeParams, R](
             |    $predicates,
             |    returns: R,
             |    location: Location
             |) extends $typ {
             |
             |  override def matches($params): Option[R] =
             |    if ($applyPredicates) Some(returns)
             |    else None
             |
             |  override def describe: List[String] = Nil
             |  override def locations: List[Location] = List(location)
             |  override def value: List[($typeParams)] = Nil
             |}""".stripMargin

        val valueClass =
          s"""case class Value$arity[$typeParams, R]($params, returns: R, location: Location) extends $typ {
             |
             |  override def matches($params): Option[R] =
             |    if ($compare) Some(returns)
             |    else None
             |
             |  override def value: List[($typeParams)] = List(($paramsNames))
             |  override def describe: List[String] = List($description)
             |  override def locations: List[Location] = List(location)
             |}""".stripMargin

        val orClass =
          s"""case class Or$arity[$typeParams, R](a: $typ, b: $typ) extends $typ {
             |  override def matches($params): Option[R] =
             |    a.matches($paramsNames).orElse(b.matches($paramsNames))
             |
             |  override def describe: List[String] = a.describe ++ b.describe
             |  override def locations: List[Location] = a.locations ++ b.locations
             |  override def value: List[($typeParams)] = a.value ++ b.value
             |}""".stripMargin

        val aanyClass =
          s"""case class AAny$arity[$typeParams, R](r: R, location: Location) extends $typ {
             |  override def matches($params): Option[R] = Some(r)
             |  override def describe: List[String] = Nil
             |  override def locations: List[Location] = List(location)
             |  override def value: List[($typeParams)] = Nil
             |}""".stripMargin

        val neverClass =
          s"""class Never$arity[$typeParams, R](location: Location) extends $typ {
             |  override def matches($params): Option[R] = None
             |  override def value: List[($typeParams)] = Nil
             |  override def locations: List[Location] = List(location)
             |  override def describe: List[String] = Nil
             |}""".stripMargin

        val mockFunctionTrait =
          s"""trait $typ extends(($typeParams) => R) {
             |
             |  def matches($params): Option[R]
             |  def describe: List[String]
             |  def locations: List[Location]
             |  def value: List[($typeParams)]
             |
             |  def or(that: $typ): $typ = Or$arity(this, that)
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
             |  override def toString(): String = this match {
             |    case Value$arity($paramsNames, returns, _) =>
             |      s"mock function expects $moreParamsNames and returns $$returns"
             |    case Predicate$arity($paramsNames, returns, _) =>
             |      s"mock function expects $moreParamsNames and returns $$returns"
             |    case Or$arity(a, b) =>
             |      s"($$a) or ($$b)"
             |    case _: Never$arity[$wildcards, _] => "mock function should never be called"
             |    case _ => ???
             |  }
             |}""".stripMargin

        val returningTrait =
          s"""trait Returns$arity[$typeParams] {
             |  def returns[R](r: R): $typ
             |}""".stripMargin

        val expectAnyClass =
          s"""class ExpectAny$arity[$typeParams](location: Location) extends Returns$arity[$typeParams] {
             |  def returns[R](r: R): $typ = new AAny$arity(r, location)
             |}""".stripMargin

        val partialExpectClass =
          s"""private class PartialExpect$arity[$typeParams]($params, location: Location) extends Returns$arity[$typeParams]{
             |  override def returns[R](r: R): $typ = Value$arity($paramsNames, r, location)
             |}""".stripMargin

        val partialPredicateClass =
          s"""private class PartialPredicate$arity[$typeParams]($predicates, location: Location) extends Returns$arity[$typeParams] {
             |  override def returns[R](r: R): $typ = Predicate$arity[$typeParams, R]($predicateNames, r, location)
             |}""".stripMargin

        writeLines(
          file,
          List(
            "package functionary",
            "import functionary.Location",
            mockFunctionTrait,
            neverClass,
            aanyClass,
            expectAnyClass,
            orClass,
            partialExpectClass,
            valueClass,
            predicateClass,
            returningTrait,
            partialPredicateClass
          )
        )
      }
    } :+ apiFile :+ extensionFile
  }

  private def packageDir(outDir: File) =
    new File(outDir, "functionary").tap(createDirectory)

  private def outFile(file: File, name: String) =
    new File(packageDir(file), s"$name.scala")

}
