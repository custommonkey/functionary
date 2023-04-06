package functionary.cats

import cats.effect.{Deferred, IO}
import cats.effect.kernel.{Ref, Resource}
import functionary.{Location, MockFunction1, Returns1}

class Context(ref: Deferred[IO, String]) {
  def expects[A](a: A): Returns1[A] = new Returns1[A] {
    override def returns[R](r: R): MockFunction1[A, R] =
      new MockFunction1[A, R] {
        override def matches(v1: A): Option[R] = ???
        override def describe: List[String] = ???
        override def locations: List[Location] = ???
        override def value: List[A] = ???
        override def apply(v1: A): R = ref.complete(v1.toString)
      }
  }
}

def mocks[A](f: Context => A) =
  for {
    r <- Deferred[IO, String]
    _ <- r.get
  } yield new Context(r)
