package functionary

trait MockFunction[A] {
  def apply(args: Seq[AnyRef]): A
}
