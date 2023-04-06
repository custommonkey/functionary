# Functionary

[![Continuous Integration](https://github.com/custommonkey/functionary/actions/workflows/ci.yml/badge.svg)](https://github.com/custommonkey/functionary/actions/workflows/ci.yml)

Functionary is a Scala library that facilitates mocking functions in your code. It allows you to create mock functions that simulate the behavior of real functions without actually executing them, which can be useful for testing purposes.

Functionary came from a need to bridge the gap between Scala 2.13 and Scala 3 which isn't currently supported by the existing scalamock library.

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

You can also compose mock functions using the `combineAll` method, which takes a sequence of mock functions and combines them into a single mock function. For example:

```scala
val functions = (1 to 4).map { i => expects(i).returns(i * 2) }
// functions: IndexedSeq[functionary.MockFunction1[Int, Int]] = Vector(mock function expects 1 and returns 2, mock function expects 2 and returns 4, mock function expects 3 and returns 6, mock function expects 4 and returns 8)
val combined = combineAll(functions)
// combined: functionary.MockFunction1[Int, Int] = (((mock function expects 1 and returns 2) or (mock function expects 2 and returns 4)) or (mock function expects 3 and returns 6)) or (mock function expects 4 and returns 8)

combined(1)
// res6: Int = 2
combined(2)
// res7: Int = 4
```

Another way to compose mock functions is to use the `foldMock` method, which takes a function that produces mock functions for each input value, and combines them into a single mock function. For example:
```scala
val folded = (1 to 4).foldMock { i => expects(i).returns(i * 10) }
// folded: functionary.MockFunction1[Int, Int] = (((mock function expects 1 and returns 10) or (mock function expects 2 and returns 20)) or (mock function expects 3 and returns 30)) or (mock function expects 4 and returns 40)

folded(1)
// res8: Int = 10
folded(2)
// res9: Int = 20
```

## Mocking traits

Functionary is focused on mocking functions so at the moment there is no specific support for mocking traits. One design pattern which avoids the need to mock traits is to use case classes to compose your APIs rather than using traits an inheritance. A trait can be seen as a collection of functions. This can be modeled by creating a class which contains functions as variables rather than methods. This allows for the implementation of individual functions to be changed by updating the value containing the function.

```scala
case class MyApi(
  sum: (Int, Int) => Int, 
  subtract: (Int, Int) => Int
)

val api = MyApi(
  _ + _,
  _ - _
)
// api: MyApi = MyApi(<function2>,<function2>)

val mockApi = MyApi(
  expects(1, 2).returns(3), 
  never[Int, Int, Int]
)
// mockApi: MyApi = MyApi(mock function expects 1, 2 and returns 3,mock function should never be called)

api.sum(1, 2)
// res10: Int = 3
mockApi.sum(1, 2)
// res11: Int = 3
```

## Limitations

Functionary does not support custom behaviors that involve side effects, as these are generally discouraged in pure functional programming. Additionally, it does not allow for mocking exceptions as these are referentially transparent and discouraged as well.

We hope that you find Functionary to be a useful tool for testing your code. If you have any questions or feedback, please don't hesitate to let us know!
