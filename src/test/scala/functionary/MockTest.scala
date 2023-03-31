package functionary

import cats.effect.IO
import weaver.SimpleIOSuite

object MockTest extends SimpleIOSuite {

  pureTest("default values") {
    expect(any[Int]("b")(0) == "b")
  }

  pureTest("returns expected value") {
    expect(expecting("a").returning("b")("a") == "b") and
      expect(expecting(1, 2).returning("b")(1, 2) == "b")
  }

  pureTest("combine mock functions") {
    val f =
      expecting("a").returning("b") or
        expecting("a").returning("b") or
        expecting("b").returning("b")

    expect(f("a") == "b") and expect(f("b") == "b")
  }

  test("throws error for unexpected value") {
    for {
      a <- IO(expecting(0).returning("b")(1)).attempt
      b <- IO(never1[Int, Int](1)).attempt
    } yield expect(a.isLeft) and expect(b.isLeft)
  }

}
