package functionary

import weaver.SimpleIOSuite

import scala.util.Try

object MockTest extends SimpleIOSuite {

  private val f1: MockFunction1[String, String] = expects("a").returns("b")
  private val f2: MockFunction2[Int, Int, String] = expects(1, 2).returns("b")
  private val f3: MockFunction1[Int, Int] = never1[Int, Int]
  private val f4: MockFunction1[Int, String] =
    expectsAny[Int].returns("b")
//  private val f5: MockFunction1[Int, Int] = expects(1 , 2)
//  private val f6: MockFunction2[Int, Int, Int] = expects((1, 2) -> 2)

  pureTest("default values") {
    expect(f4(0) == "b")
  }

  pureTest("predicates") {
    val p = expects((s: String) => s.isEmpty).returns(1)
    expect(p("") == 1)
  }

  pureTest("returns expected value") {
    expect(f1("a") == "b") and expect(f2(1, 2) == "b")
    // and
//      expect(f5(1) == 2) and
//      expect(f6(1, 2) == 2)
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

  private def fails[A](f: => A) = Try(f).failed.get.getMessage

  pureTest("throws error for unexpected value") {
    val ff = fails(expects(0).returns("b")(1))
    val ee = fails(f3(1))
    println(ee)
    expect(ff.startsWith("""Expected 0""")) and
      expect(ee startsWith "Expected ,") // TODO Better error
  }

  pureTest("sane to string") {
    expect(f1.toString() == "mock function(a) = b")
  }

}
