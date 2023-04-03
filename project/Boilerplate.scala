import sbt.io.PathFinder
import sbt.File
import sbt.io.IO.{createDirectory, writeLines}

import scala.util.chaining.scalaUtilChainingOps

object Boilerplate {

  def gen(file: File): Seq[File] = (1 to 5).map { arity =>
    val name = s"MockFunction$arity"
    new File(new File(file, "functionary"), s"$name.scala").tap { file =>
      println(s"Making file $file")
      val range = (1 to arity)
      val typeParams = range.map { i => s"V$i" }.mkString(", ")
      val params = range.map { i => s"v$i: V$i" }.mkString(", ")
      val predicates = range.map { i => s"p$i: V$i => Boolean" }.mkString(", ")
      val predicateNames = range.map { i => s"p$i" }.mkString(", ")
      val paramsNames = range.map { i => s"v$i" }.mkString(", ")
      val moreParamsNames = range.map { i => s"$$v$i" }.mkString(", ")
      val typ = s"$name[$typeParams, R]"
      val compare = range.map { i => s"v$i == this.v$i" }.mkString(" && ")
      val applyPredicates = range.map { i => s"p$i(v$i)" }.mkString(" && ")
      val description = range.map { n => s"v$n.toString" }.mkString(", ")
      createDirectory(file.getParentFile)
      writeLines(
        file,
        List(s"""
          |package functionary
          |
          |import functionary.Location
          |
          |sealed trait $typ extends(($typeParams) => R) {
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
          |}
          |
          |class Never$arity[$typeParams, R](location: Location) extends $typ {
          |  override def matches($params): Option[R] = None
          |  override def value: List[($typeParams)] = Nil
          |  override def locations: List[Location] = List(location)
          |  override def describe: List[String] = Nil
          |}
          |
          |protected class AAny$arity[$typeParams, R](r: R, location: Location)
          |    extends $typ {
          |  override def matches($params): Option[R] = Some(r)
          |  override def describe: List[String] = Nil
          |  override def locations: List[Location] = List(location)
          |  override def value: List[($typeParams)] = Nil
          |}
          |
          |class ExpectAny$arity[$typeParams](location: Location) {
          |  def returns[R](r: R): $typ = new AAny$arity(r, location)
          |}
          |
          |case class Or$arity[$typeParams, R](a: $typ, b: $typ) extends $typ {
          |  override def matches($params): Option[R] =
          |    a.matches($paramsNames).orElse(b.matches($paramsNames))
          |
          |  override def describe: List[String] = a.describe ++ b.describe
          |  override def locations: List[Location] = a.locations ++ b.locations
          |  override def value: List[($typeParams)] = a.value ++ b.value
          |}
          |
          |class Expect$arity[$typeParams]($params, location: Location) {
          |  def returns[R](r: R): $typ = Value$arity($paramsNames, r, location)
          |}
          |
          |case class Value$arity[$typeParams, R]($params, returns: R, location: Location)
          |    extends $typ {
          |
          |  override def matches($params): Option[R] =
          |    if ($compare) Some(returns)
          |    else None
          |
          |  override def value: List[($typeParams)] = List(($paramsNames))
          |  override def describe: List[String] = List($description)
          |  override def locations: List[Location] = List(location)
          |}
          |
          |class PartialPredicate$arity[$typeParams]($predicates, location: Location) {
          |  def returns[R](r: R): $typ = Predicate$arity($predicateNames, r, location)
          |}
          |
          |case class Predicate$arity[$typeParams, R](
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
          |}
          |""".stripMargin)
      )
    }
  }

}
