package object functionary {

//  def mock[V1, R](expected: V1, returning: R): MockFunction1[V1, R] =
//    Value1(expected, returning)
//
//  def mock[V1, V2, R](v1: V1, v2: V2, returning: R): MockFunction2[V1, V2, R] =
//    Value2(v1, v2, returning)

  def any[V1] = new Thing[V1]

  def never1[V1, R]: MockFunction1[V1, R] = new Never1()

  def never2[V1, V2, R]: MockFunction2[V1, V2, R] = new Never2()

  def expecting[V1](v1: V1): Expect1[V1] = new Expect1[V1](v1)
  def expecting[V1, V2](v1: V1, v2: V2): Expect2[V1, V2] =
    new Expect2[V1, V2](v1, v2)

}
