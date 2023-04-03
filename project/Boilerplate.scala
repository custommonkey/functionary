import sbt.io.PathFinder
import sbt.File
import sbt.io.IO.{createDirectory, writeLines}

import scala.util.chaining.scalaUtilChainingOps

object Boilerplate {

  def gen(file: File): Seq[File] = (1 to 3).map { arity =>
    val name = s"MockFunction$arity"
    new File(new File(file, "functionary2"), s"$name.scala").tap { file =>
      println(s"Making file $file")
      val range = (1 to arity)
      val typeParams = range.map { i => s"V$i" }.mkString(", ")
      val params = range.map { i => s"v$i: V$i" }.mkString(", ")
      val paramsNames = range.map { i => s"v$i" }.mkString(", ")
      val typ = s"$name[$typeParams, R]"
      val compare = range.map { i => s"v$i == this.v$i" }.mkString(" && ")
      val description = range.map { n => s"v$n.toString" }.mkString(", ")
      createDirectory(file.getParentFile)
      writeLines(
        file,
        List(s"""
          |package functionary2
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
          |             |  but was (v1, v2)\"\"\".stripMargin
          |        )
          |    }
          |  }
          |
          |private case class Or$arity[$typeParams, R](a: $typ, b: $typ) extends $typ {
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
          |protected case class Value$arity[$typeParams, R]($params, returns: R, location: Location)
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
          |protected case class Predicate$arity[$typeParams, R](
          |    expected: ($typeParams) => Boolean,
          |    returns: R,
          |    location: Location
          |) extends $typ {
          |
          |  override def matches($params): Option[R] =
          |    if (expected($paramsNames)) Some(returns)
          |    else None
          |
          |  override def describe: List[String] = List(expected.toString())
          |  override def locations: List[Location] = List(location)
          |  override def value: List[($typeParams)] = Nil
          |}
          |""".stripMargin)
      )
    }
  }

}
