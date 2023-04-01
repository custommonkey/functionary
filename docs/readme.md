# Functionary

Functionary is a Scala library that provides an easy-to-use API for mocking functions in your code. It allows you to create mock functions that simulate the behavior of real functions without actually executing them, which can be useful for testing purposes.

## Getting Started

To get started with Functionary, simply add it as a dependency to your Scala project and import it into your test files. Then, you can use the provided functions to create and configure mock functions as needed.

```scala mdoc
import functionary.expects
```

## Defining a Mock Function

To define a mock function, use the `expects` function, which takes one or more arguments representing the expected input to the function. You can then use the `returns` method to specify the value that the function should return when called with the expected input.

```scala mdoc
val f = expects(1).returns(2)

f(1)
```

Functionary supports functions with varying arity. For example, you can define a mock function with two arguments like this:
```scala mdoc
val f2 = expects(1, 2).returns(3)

f2(1, 2)
```

Functionary also allows the use of predicates. For example, you can define a mock function that takes a string argument and returns 1 if the string is empty:
```scala mdoc
val f4 = expects((s: String) => s.isEmpty).returns(1)

f4("")
```

## Composing Mock Functions

You can compose mock functions to handle multiple cases using the `or` method. For example, you can define a mock function that returns different values for different input values like this:

```scala mdoc
val f3 = expects(1).returns(2) or expects(2).returns(4) 

f3(1)
f3(2)
```

## Limitations

Functionary does not support custom behaviors that involve side effects, as these are generally discouraged in pure functional programming. Additionally, it does not allow for mocking exceptions as these are referentially transparent and discouraged as well.

We hope that you find Functionary to be a useful tool for testing your code. If you have any questions or feedback, please don't hesitate to let us know!
