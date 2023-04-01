package functionary

def expectsAny[V1] = ExpectAny1[V1]()
def never1[V1, R]: MockFunction1[V1, R] = Never1()
def never2[V1, V2, R]: MockFunction2[V1, V2, R] = Never2()

def expects[V1](v1: V1): Expect1[V1] = Expect1[V1](v1)
def expects[V1](v1: V1 => Boolean): Predicate1[V1] = Predicate1[V1](v1)
def expects[V1, V2](v1: V1, v2: V2): Expect2[V1, V2] = Expect2[V1, V2](v1, v2)
def expects[V1, R](t: (V1, R)): MockFunction1[V1, R] = Value1(t._1, t._2)

def all[ A, B]( i: Iterable[MockFunction1[A, B]] ): MockFunction1[A, B] =
  i.reduce(_ or _)

def all[A, B, C]( i: Iterable[MockFunction2[A, B, C]] ): MockFunction2[A, B, C] =
  i.reduce(_ or _)

def flatMock[A, B, C](i: Iterable[A])(
  f: A => MockFunction1[B, C]
): MockFunction1[B, C] = all(i.map(f))

def flatMock2[A, B, C, D](i: Iterable[A])(
  f: A => MockFunction2[B, C, D]
): MockFunction2[B, C, D] = all(i.map(f))

implicit class FlatMock[A](i: Iterable[A]) {
  def flatMock[B, C](f: A => MockFunction1[B, C]): MockFunction1[B, C] =
    functionary.flatMock(i)(f)

  def flatMock[B, C, D](
                         f: A => MockFunction2[B, C, D]
                       ): MockFunction2[B, C, D] =
    functionary.flatMock2(i)(f)
}
