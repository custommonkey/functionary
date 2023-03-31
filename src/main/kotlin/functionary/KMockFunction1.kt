package functionary

sealed interface MockFunction1<A, B> : ((A) -> B) {

    fun matches(actual: A): B?

    fun or(that: MockFunction1<A, B>): MockFunction1<A, B> =
        Or(this, that)

    override fun invoke(actual: A): B =
        matches(actual) ?: throw AssertionError()
}

sealed interface MockFunction2<V1, V2, B> : ((V1, V2) -> B) {

    fun matches(v1: V1, v2: V2): B?

    fun or(that: MockFunction2<V1, V2, B>): MockFunction2<V1, V2, B> =
        Or2(this, that)

    override fun invoke(v1: V1, v2: V2): B = matches(v1, v2) ?: throw AssertionError()
}

private data class Value<A, B>(val expected: A, val returning: B) :
    MockFunction1<A, B> {

    override fun matches(actual: A): B? =
        if (actual == expected) {
            returning
        } else {
            null
        }
}

private data class Value2<V1, V2, R>(val v1: V1, val v2: V2, val returning: R) :
    MockFunction2<V1, V2, R> {

    override fun matches(v1: V1, v2: V2): R? =
        if (this.v1 == v1 && this.v2 == v2) {
            returning
        } else {
            null
        }
}

private data class Or<A, B>(val a: MockFunction1<A, B>, val b: MockFunction1<A, B>) :
    MockFunction1<A, B> {

    override fun matches(actual: A): B? =
        a.matches(actual) ?: b.matches(actual)
}

private data class Or2<V1, V2, B>(
    val a: MockFunction2<V1, V2, B>,
    val b: MockFunction2<V1, V2, B>,
) : MockFunction2<V1, V2, B> {

    override fun matches(v1: V1, v2: V2): B? =
        a.matches(v1, v2) ?: b.matches(v1, v2)
}

private class AAny<V1, R>(val r: R) : MockFunction1<V1, R> {
    override fun matches(actual: V1): R? = r
}

fun <V1, R> mock(expected: V1, returning: R): MockFunction1<V1, R> =
    Value(expected, returning)

fun <V1, V2, R> mock(v1: V1, v2: V2, returning: R): MockFunction2<V1, V2, R> =
    Value2(v1, v2, returning)

class Thing<V1> {
    fun <R> invoke(r: R): MockFunction1<V1, R> = AAny(r)
}

fun <V1> any() = Thing<V1>()
