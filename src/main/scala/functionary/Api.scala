package functionary

private[functionary] trait Api extends GeneratedApi with Folding {

  def expects[V1, R](t: (V1, R))(implicit
      location: Location
  ): MockFunction1[V1, R] =
    Value1(t._1, t._2, location)

  def expects[V1, V2, R](
      t: ((V1, V2), R)
  )(implicit location: Location): MockFunction2[V1, V2, R] =
    Value2(t._1._1, t._1._2, t._2, location)

}
