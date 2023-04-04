package functionary

private[functionary] trait Api extends GeneratedApi {

  def expects[V1, R](t: (V1, R))(implicit
      location: Location
  ): MockFunction1[V1, R] =
    Value1(t._1, t._2, location)

  def expects[V1, V2, R](
      t: ((V1, V2), R)
  )(implicit location: Location): MockFunction2[V1, V2, R] =
    Value2(t._1._1, t._1._2, t._2, location)

  final implicit class FlatMock[A](i: Iterable[A]) {
    def flatMock[B, C](f: A => MockFunction1[B, C]): MockFunction1[B, C] =
      foldMock[A, B, C](i)(f)

    def flatMock[B, C, D](
        f: A => MockFunction2[B, C, D]
    ): MockFunction2[B, C, D] =
      foldMock[A, B, C, D](i)(f)
  }

}
