package functionary

trait MockFunction[A] {
  def apply(args: Seq[AnyRef]): A
  def toString(named: Option[String]): String
}
