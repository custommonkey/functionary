package functionary

sealed trait MockFunction1[A, B] extends (A => B) {

  def matches(actual: A): Option[B]

  def or(that: MockFunction1[A, B]): MockFunction1[A, B] =
    Or(this, that)

  def describe: List[String]
  def locations: List[Location]

  def apply(actual: A): B =
    matches(actual) match {
      case Some(value) => value
      case None =>
        throw new AssertionError(
          s"""Expected ${describe.mkString(", ")},
             |  but was $actual
             |  at ${locations.mkString(", ")}""".stripMargin
        )
    }

  override def toString(): String = this match {
    case PValue1(_, _, _) => ???
    case Value1(expected, returns, _) =>
      s"mock function($expected) = $returns"
    case Or(_, _)        => ???
    case _: AAny[_, _]   => ???
    case _: Never1[_, _] => ???
  }
}

sealed trait MockFunction2[V1, V2, B] extends ((V1, V2) => B) {

  def matches(v1: V1, v2: V2): Option[B]

  def or(that: MockFunction2[V1, V2, B]): MockFunction2[V1, V2, B] =
    Or2(this, that)

  def value: List[(V1, V2)]

  def apply(v1: V1, v2: V2): B =
    matches(v1, v2) match {
      case Some(value) => value
      case None =>
        throw new AssertionError(
          s"""Expected ${value.mkString(", ")},
             |  but was ($v1, $v2)""".stripMargin
        )
    }

}

protected case class PValue1[A, B](
    expected: A => Boolean,
    returns: B,
    location: Location
) extends MockFunction1[A, B] {

  override def matches(actual: A): Option[B] =
    if (expected(actual)) Some(returns)
    else None

  override def describe: List[String] = List(expected.toString())

  override def locations: List[Location] = List(location)
}

protected case class Value1[A, B](expected: A, returns: B, location: Location)
    extends MockFunction1[A, B] {

  override def matches(actual: A): Option[B] =
    if (expected == actual) Some(returns)
    else None

  override def describe: List[String] = List(expected.toString)

  override def locations: List[Location] = List(location)
}

protected case class Value2[V1, V2, R](v1: V1, v2: V2, returns: R)
    extends MockFunction2[V1, V2, R] {

  override def matches(_v1: V1, _v2: V2): Option[R] =
    if (v1 == _v1 && v2 == _v2) Some(returns)
    else None

  override def value: List[(V1, V2)] = List((v1, v2))
}

protected case class Or[A, B](a: MockFunction1[A, B], b: MockFunction1[A, B])
    extends MockFunction1[A, B] {

  override def matches(actual: A): Option[B] =
    a.matches(actual).orElse(b.matches(actual))

  override def describe: List[String] = a.describe ++ b.describe

  override def locations: List[Location] = a.locations ++ b.locations
}

protected case class Or2[V1, V2, B](
    a: MockFunction2[V1, V2, B],
    b: MockFunction2[V1, V2, B]
) extends MockFunction2[V1, V2, B] {

  override def matches(v1: V1, v2: V2): Option[B] =
    a.matches(v1, v2).orElse(b.matches(v1, v2))

  override def value: List[(V1, V2)] = a.value ++ b.value
}

protected class AAny[V1, R](r: R, location: Location)
    extends MockFunction1[V1, R] {
  override def matches(a: V1): Option[R] = Some(r)

  override def describe: List[String] = Nil

  override def locations: List[Location] = List(location)
}

class ExpectAny1[V1](location: Location) {
  def returns[R](r: R): MockFunction1[V1, R] = new AAny(r, location)
}

protected class Never1[A, B](location: Location) extends MockFunction1[A, B] {
  override def matches(a: A): Option[B] = None

  override def describe: List[String] = Nil

  override def locations: List[Location] = List(location)
}

protected class Never2[V1, V2, B]() extends MockFunction2[V1, V2, B] {
  override def matches(a: V1, b: V2): Option[B] = None

  override def value: List[(V1, V2)] = Nil
}

class Expect1[V1](v1: V1, location: Location) {
  def returns[R](r: R): MockFunction1[V1, R] = Value1(v1, r, location)
}
class Predicate1[V1](v1: V1 => Boolean, location: Location) {
  def returns[R](r: R): MockFunction1[V1, R] = PValue1(v1, r, location)
}

class Expect2[V1, V2](v1: V1, v2: V2) {
  def returns[R](r: R): MockFunction2[V1, V2, R] = Value2(v1, v2, r)
}
