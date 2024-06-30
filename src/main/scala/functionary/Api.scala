package functionary

import java.lang.reflect.{InvocationHandler, Method}
import java.lang.reflect.Proxy.newProxyInstance
import scala.reflect.{ClassTag, classTag}

private[functionary] trait Api extends GeneratedApi with Folding {

  def expects[V1, R](t: (V1, R))(implicit
      location: Location
  ): MockFunction1[V1, R] =
    Value1(t._1, t._2, location)

  def expects[V1, V2, R](
      t: ((V1, V2), R)
  )(implicit location: Location): MockFunction2[V1, V2, R] =
    Value2(t._1._1, t._1._2, t._2, location)

  def mock[A: ClassTag](
      functions: (String, MockFunction[_])*
  ): A = mock(
    Option(classTag[A].runtimeClass.getClassLoader)
      .getOrElse(getClass.getClassLoader),
    functions: _*
  )

  private def mock[A: ClassTag](
      loader: ClassLoader,
      functions: (String, MockFunction[_])*
  ): A = {
    val clazz = classTag[A].runtimeClass
    newProxyInstance(
      loader,
      Array(clazz),
      invocationHandler(functions, clazz)
    )
      .asInstanceOf[A]
  }

  private def invocationHandler[A](
      functions: Seq[(String, MockFunction[_])],
      clazz: Class[_]
  ): InvocationHandler = (_, method, args) =>
    functions.toMap.get(method.getName) match {
      case Some(value) => value(args.toSeq).asInstanceOf[AnyRef]
      case None =>
        val className = clazz.getSimpleName
        if (method.getName == "toString") {
          toString(functions, method, className)
        } else {
          throw new AssertionError(
            "unexpected call to " + toString(functions, method, className)
          )
        }
    }

  private def toString[A](
      functions: Seq[(String, MockFunction[_])],
      method: Method,
      className: String
  ) = {
    s"""mock function $className.${method.getName}
       | ${functions
        .map { case (name, function) => function.toString(Some(name)) }
        .mkString(", ")}
       |""".stripMargin
  }
}
