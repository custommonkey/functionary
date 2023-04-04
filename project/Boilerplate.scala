import sbt.File
import sbt.io.IO.{createDirectory, writeLines}

import scala.util.chaining.scalaUtilChainingOps

object Boilerplate {

  def gen(outDir: File): Seq[File] = {
    val apiFile = new File(packageDir(outDir), "GeneratedApi.scala").tap {
      file =>

        val apiFunctions = (1 to 5).flatMap { arity =>
          val range = (1 to arity)
          val name = s"MockFunction$arity"
          val typeParams = range.map { i => s"V$i" }.mkString(", ")
          val typ = s"$name[$typeParams, R]"
          val paramsNames = range.map { i => s"v$i" }.mkString(", ")
          val params = range.map { i => s"v$i: V$i" }.mkString(", ")
          val predicates =
            range.map { i => s"p$i: V$i => Boolean" }.mkString(", ")
          val predicateNames = range.map { i => s"p$i" }.mkString(", ")
          List(
            s"""
          |  def expects[$typeParams]($params)(implicit location: Location): Returns$arity[$typeParams] =
          |    new PartialExpect$arity[$typeParams]($paramsNames, location)
          |""".stripMargin,
            s"""|
          |  def expects[$typeParams]($predicates)(implicit location: Location): Returns$arity[$typeParams] =
          |    new PartialPredicate$arity[$typeParams]($predicateNames, location)
          |""".stripMargin,
            s"""|
          |  def expectsAny[$typeParams](implicit location: Location): Returns$arity[$typeParams] =
          |    new ExpectAny$arity[$typeParams](location)
          |""".stripMargin,
            s"""|
          |  def never[$typeParams, R](implicit location: Location): $typ =
          |    new Never$arity(location)
          |""".stripMargin,
            s"""|
          |  def combineAll[$typeParams, R](i: Iterable[$typ]): $typ =
          |    i.reduce(_ or _)
          |""".stripMargin
          )
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

      val name = s"MockFunction$arity"

      outFile(outDir, name).tap { file =>
        println(s"Making file $file")

        val range = (1 to arity)
        val typeParams = range.map { i => s"V$i" }.mkString(", ")
        val params = range.map { i => s"v$i: V$i" }.mkString(", ")
        val predicates =
          range.map { i => s"p$i: V$i => Boolean" }.mkString(", ")
        val predicateNames = range.map { i => s"p$i" }.mkString(", ")
        val paramsNames = range.map { i => s"v$i" }.mkString(", ")
        val moreParamsNames = range.map { i => s"$$v$i" }.mkString(", ")
        val typ = s"$name[$typeParams, R]"
        val compare = range.map { i => s"v$i == this.v$i" }.mkString(" && ")
        val applyPredicates = range.map { i => s"p$i(v$i)" }.mkString(" && ")
        val description = range.map { n => s"v$n.toString" }.mkString(", ")

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
          s"""sealed trait $typ extends(($typeParams) => R) {
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
             |             |  but was ($moreParamsNames)\"\"\".stripMargin
             |        )
             |    }
             |
             |  override def toString(): String = this match {
             |    case Value$arity($paramsNames, returns, _) =>
             |      s"mock function($moreParamsNames) = $$returns"
             |    case _        => ???
             |  }
             |}""".stripMargin

        val returningTrait =
          s"""sealed trait Returns$arity[$typeParams] {
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
    } :+ apiFile
  }

  private def packageDir(outDir: File) =
    new File(outDir, "functionary").tap(createDirectory)

  private def outFile(file: File, name: String) =
    new File(packageDir(file), s"$name.scala")

}
