package functionary

import cats.effect.IO
import weaver.SimpleIOSuite

object MockTest extends SimpleIOSuite {

  private val f1: MockFunction1[String, String] = expects("a").returns("b")
  private val f2: MockFunction2[Int, Int, String] = expects(1, 2).returns("b")
  private val f3 = never1[Int, Int]
  private val f4 = expectsAny[Int].returns("b")

  pureTest("default values") {
    expect(f4(0) == "b")
  }

  pureTest("returns expected value") {
    expect(f1("a") == "b") and expect(f2(1, 2) == "b")
  }

  pureTest("combine mock functions") {
    val value = expects("b").returns("b")
    val f = f1 or f1 or value
    val ff = all(List(f1, f1, value))
    val xx: MockFunction1[Int, Int] = (0 to 1).flatMock { i =>
      expects(i).returns(i)
    }

    expect(f("a") == "b") and expect(f("b") == "b") and
      expect(ff("a") == "b") and expect(ff("b") == "b") and
      expect(xx(1) == 1)
  }

  test("throws error for unexpected value") {
    for {
      a <- IO(expects(0).returns("b")(1)).attempt
      b <- IO(f3(1)).attempt
    } yield expect(a.isLeft) and expect(b.isLeft)
  }

}
