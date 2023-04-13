package functionary

import munit.FunSuite

import scala.util.Try

class MockTest extends FunSuite {

  private val f1: MockFunction1[String, String] = expects("a").returns("b")
  private val f2: MockFunction2[Int, Int, String] = expects(1, 2).returns("b")
  private val f3: MockFunction1[Int, Int] = never[Int, Int]
  private val f4: MockFunction1[Int, String] =
    expectsAny[Int].returns("b")
//  private val f5: MockFunction1[Int, Int] = expects(1 , 2)
  private val f6: MockFunction2[Int, Int, Int] = tuple((1, 2) -> 2)

  test("default values") {
    assertEquals(f4(0), "b")
  }

  test("predicates") {
    val p: MockFunction1[String, Int] =
      expects((s: String) => s.isEmpty).returns(1)

    assertEquals(p(""), 1)
  }

  test("returns expected value") {
    assertEquals(f1("a"), "b")
    assertEquals(f2(1, 2), "b")
//      expect(f5(1) == 2) and
    assertEquals(f6(1, 2), 2)
  }

  test("combine mock functions") {
    val value = expects("b").returns("b")
    val f = f1 or f1 or value
    val ff = combineAll(f1, f1, value)
    val xx = (0 to 1).foldMock { i =>
      expects(i).returns(i)
    }

    assertEquals(f("a"), "b")
    assertEquals(f("b"), "b")
    assertEquals(ff("a"), "b")
    assertEquals(ff("b"), "b")
    assertEquals(xx(1), 1)
  }

  private def fails[A](f: => A) = Try(f).failed.get.getMessage

  test("throws error for unexpected value") {
    val ff = fails(expects(0).returns("b")(1))
    val ee = fails(f3(1))
    println(ee)
    assert(ff.startsWith("""Expected 0"""))
    assert(ee startsWith "Expected ,") // TODO Better error
  }

  test("sane to string") {
    assertEquals(f1.toString(), "mock function  expects a and returns b")
  }

  test("proxy") {
    trait Thing {
      def f(a: Int): Int
      def x(a: Int): Int
    }

    val mocked = mock[Thing](
      "f" -> expects(1).returns(2)
    )

    assertEquals(mocked.f(1), 2)

    try {
      mocked.x(1)
    } catch {
      case e: AssertionError =>
        val message = e.getMessage
        val expected =
          """unexpected call to mock function Thing$1.x
            | mock function f expects 1 and returns 2""".stripMargin

        assertNoDiff(message, expected)
    }

  }

}
