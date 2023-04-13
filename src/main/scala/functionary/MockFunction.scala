package functionary

trait MockFunction[A] {
  def apply(args: Seq[AnyRef]): A
  def toString(named: String): String
}
