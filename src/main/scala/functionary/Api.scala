package functionary

protected trait Api {

  def never1[V1, R](implicit
      location: Location
  ): MockFunction1[V1, R] =
    new Never1(location)
  def never2[V1, V2, R](implicit
      location: Location
  ): MockFunction2[V1, V2, R] =
    new Never2(location)

  def expectsAny[V1](implicit location: Location) =
    new ExpectAny1[V1](location)

  def expects[V1](v1: V1)(implicit
      location: Location
  ): Expect1[V1] =
    new Expect1[V1](v1, location)

  def expects[V1](v1: V1 => Boolean)(implicit
      location: Location
  ): PartialPredicate1[V1] =
    new PartialPredicate1[V1](v1, location)

  def expects[V1, R](t: (V1, R))(implicit
      location: Location
  ): MockFunction1[V1, R] =
    new Value1(t._1, t._2, location)

  def expects[V1, V2, R](
      t: ((V1, V2), R)
  )(implicit location: Location): MockFunction2[V1, V2, R] =
    new Value2(t._1._1, t._1._2, t._2, location)

  def expects[V1, V2](v1: V1, v2: V2)(implicit
      location: Location
  ): Expect2[V1, V2] =
    new Expect2[V1, V2](v1, v2, location)

  def all[A, B](
      i: Iterable[MockFunction1[A, B]]
  ): MockFunction1[A, B] =
    i.reduce(_ or _)

  def all[A, B, C](
      i: Iterable[MockFunction2[A, B, C]]
  ): MockFunction2[A, B, C] =
    i.reduce(_ or _)

  def flatMock[A, B, C](i: Iterable[A])(
      f: A => MockFunction1[B, C]
  ): MockFunction1[B, C] = all(i.map(f))

  def flatMock2[A, B, C, D](i: Iterable[A])(
      f: A => MockFunction2[B, C, D]
  ): MockFunction2[B, C, D] = all(i.map(f))

  final implicit class FlatMock[A](i: Iterable[A]) {
    def flatMock[B, C](
        f: A => MockFunction1[B, C]
    ): MockFunction1[B, C] =
      functionary.flatMock(i)(f)

    def flatMock[B, C, D](
        f: A => MockFunction2[B, C, D]
    ): MockFunction2[B, C, D] =
      flatMock2(i)(f)
  }

}
