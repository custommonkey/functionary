package functionary

import munit.FunSuite

class MockTest extends FunSuite {

  private val expectsAreturnsB = expects("a").returns("b")
  private val expects12returnsB = expects(1, 2).returns("b")
  private val expectsIntNeverReturnsIntNever = never[Int, Int]
  private val expectsAnyIntReturnsB = returns[Int, String]("b")
//  private val f5: MockFunction1[Int, Int] = expects(1 , 2)
  private val f6 = tuple((1, 2) -> 2)

  test("default values") {
    assertEquals(expectsAnyIntReturnsB(0), "b")
  }

  test("predicates") {
    val p = expects((s: String) => s.isEmpty).returns(1)

    assertEquals(p(""), 1)
  }

  test("returns expected value") {
    assertEquals(expectsAreturnsB("a"), "b")
    assertEquals(expects12returnsB(1, 2), "b")
//      expect(f5(1) == 2) and
    assertEquals(f6(1, 2), 2)
  }

  test("combine mock functions") {
    val expectsBreturnsB = expects("b").returns("b")
    val f = expectsAreturnsB or expectsAreturnsB or expectsBreturnsB
    val ff = combineAll(expectsAreturnsB, expectsAreturnsB, expectsBreturnsB)
    val xx = (0 to 1).foldMock { i =>
      expects(i).returns(i)
    }

    assertEquals(f("a"), "b")
    assertEquals(f("b"), "b")
    assertEquals(ff("a"), "b")
    assertEquals(ff("b"), "b")
    assertEquals(xx(1), 1)
  }

  test("throws error for unexpected value") {
    val ff =
      intercept[AssertionError](expectsAreturnsB("b")(1)).getMessage

    assert(clue(ff).startsWith("""Expected a,"""))

    val ee =
      intercept[AssertionError](expectsIntNeverReturnsIntNever(1)).getMessage

    assert(clue(ee).startsWith("Expected *,"))
  }

  test("sane to string") {
    Seq(
      expectsAreturnsB -> "mock function expects a and returns b",
      expectsAnyIntReturnsB -> "mock function expects * and returns b",
      expectsIntNeverReturnsIntNever -> "mock function expects * and returns <nothing>",
      expectsIntNeverReturnsIntNever.named(
        "pete"
      ) -> "mock function pete expects * and returns <nothing>"
    ).foreach { case (function, msg) =>
      assertEquals(function.toString(), msg)
    }
  }

  trait Thing {
    def f(a: Int): Int
    def x(a: Int): Int
  }

  test("proxy") {

    val mocked = mock[Thing](
      "f" -> expects(1).returns(2)
    )

    assertEquals(mocked.f(1), 2)

    val message = intercept[AssertionError](mocked.x(1)).getMessage

    val expected =
      """unexpected call to mock Thing
       | mock function expects 1 and returns 2""".stripMargin // TODO, name should be x

    assertNoDiff(message, expected)

  }

}
