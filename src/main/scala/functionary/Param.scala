package functionary

sealed trait Param[A] {
  def apply(a: A): Boolean
}

object Param {
  case class Value[A](value: A) extends Param[A] {
    override def apply(a: A): Boolean = value == a
    override def toString: String = value.toString
  }
  case class Any[A]() extends Param[A] {
    override def apply(a: A): Boolean = true
    override def toString: String = "*"
  }
  case class Predicate[A](predicate: A => Boolean) extends Param[A] {
    override def apply(a: A): Boolean = predicate(a)
  }
}
