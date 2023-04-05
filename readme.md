# Functionary

[![Continuous Integration](https://github.com/custommonkey/functionary/actions/workflows/ci.yml/badge.svg)](https://github.com/custommonkey/functionary/actions/workflows/ci.yml)

Functionary is a Scala library that provides an easy-to-use API for mocking functions in your code. It allows you to create mock functions that simulate the behavior of real functions without actually executing them, which can be useful for testing purposes.

## Getting Started

To get started with Functionary, simply add it as a dependency to your Scala project and import it into your test files. Then, you can use the provided functions to create and configure mock functions as needed.

```scala
import functionary.expects
import functionary.never
import functionary.tuple
import functionary.combineAll
import functionary.FoldsOps
```

## Defining a Mock Function

To define a mock function, use the `expects` function, which takes one or more arguments representing the expected input to the function. You can then use the `returns` method to specify the value that the function should return when called with the expected input.

```scala
val f = expects(1).returns(2)
// f: functionary.MockFunction1[Int, Int] = mock function expects 1 and returns 2

f(1)
// res0: Int = 2
```

Functionary supports functions with varying arity. For example, you can define a mock function with two arguments like this:
```scala
val f2 = expects(1, 2).returns(3)
// f2: functionary.MockFunction2[Int, Int, Int] = mock function expects 1, 2 and returns 3

f2(1, 2)
// res1: Int = 3
```

Functionary also allows the use of predicates. For example, you can define a mock function that takes a string argument and returns 1 if the string is empty:
```scala
val f4 = expects((s: String) => s.isEmpty).returns(1)
// f4: functionary.MockFunction1[String, Int] = mock function expects <function1> and returns 1

f4("")
// res2: Int = 1
```

```scala
val f5 = tuple((1, 2) -> 3)
// f5: functionary.MockFunction2[Int, Int, Int] = mock function expects 1, 2 and returns 3

f5(1, 2)
// res3: Int = 3
```

## Composing Mock Functions

You can compose mock functions to handle multiple cases using the `or` method. For example, you can define a mock function that returns different values for different input values like this:

```scala
val f3 = expects(1).returns(2) or expects(2).returns(4) 
// f3: functionary.MockFunction1[Int, Int] = (mock function expects 1 and returns 2) or (mock function expects 2 and returns 4) 

f3(1)
// res4: Int = 2
f3(2)
// res5: Int = 4
```

```scala
val list = combineAll((1 to 4).map { i => expects(i).returns(i * 2) })
// list: functionary.MockFunction1[Int, Int] = (((mock function expects 1 and returns 2) or (mock function expects 2 and returns 4)) or (mock function expects 3 and returns 6)) or (mock function expects 4 and returns 8)

list(1)
// res6: Int = 2
list(2)
// res7: Int = 4
```

```scala
val folded = (1 to 4).foldMock { i => expects(i).returns(i * 10) }
// folded: functionary.MockFunction1[Int, Int] = (((mock function expects 1 and returns 10) or (mock function expects 2 and returns 20)) or (mock function expects 3 and returns 30)) or (mock function expects 4 and returns 40)

folded(1)
// res8: Int = 10
folded(2)
// res9: Int = 20
```

```scala
case class MyApi(sum :(Int,  Int) => Int, subtract: (Int, Int) => Int)

object MyApi {
  def apply(): MyApi = MyApi(_ + _, _ - _)
}

val api = MyApi(expects(1, 2).returns(3), never[Int, Int, Int])
// api: MyApi = MyApi(mock function expects 1, 2 and returns 3,mock function should never be called)

api.sum(1, 2)
// res10: Int = 3
```

## Limitations

Functionary does not support custom behaviors that involve side effects, as these are generally discouraged in pure functional programming. Additionally, it does not allow for mocking exceptions as these are referentially transparent and discouraged as well.

We hope that you find Functionary to be a useful tool for testing your code. If you have any questions or feedback, please don't hesitate to let us know!
