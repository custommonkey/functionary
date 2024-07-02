package functionary

import java.lang.reflect.InvocationHandler
import java.lang.reflect.Proxy.newProxyInstance
import scala.reflect.{ClassTag, classTag}

private[functionary] trait Api extends GeneratedApi with Folding {

  def expects[V1, R](t: (V1, R))(implicit
      location: Location
  ): MockFunction1[V1, R] =
    Value1(Param.Value(t._1), Some(t._2), location, None)

  def expects[V1, V2, R](
      t: ((V1, V2), R)
  )(implicit location: Location): MockFunction2[V1, V2, R] =
    Value2(
      Param.Value(t._1._1),
      Param.Value(t._1._2),
      Some(t._2),
      location,
      None
    )

  private def classLoader[A: ClassTag] =
    Option(classTag[A].runtimeClass.getClassLoader)
      .getOrElse(getClass.getClassLoader)

  def mock[A: ClassTag](functions: (String, MockFunction[_])*): A = {
    val clazz = classTag[A].runtimeClass
    newProxyInstance(
      classLoader,
      Array(clazz),
      invocationHandler(functions, clazz)
    )
      .asInstanceOf[A]
  }

  private def invocationHandler(
      functions: Seq[(String, MockFunction[_])],
      clazz: Class[_]
  ): InvocationHandler = { (_, method, args) =>
    functions.toMap.get(method.getName) match {
      case Some(value) => value(args.toSeq).asInstanceOf[AnyRef]
      case None =>
        val className = clazz.getSimpleName
        if (method.getName == "toString") {
          val str = toString(functions, className)
          str
        } else {
          val str = toString(functions, className)
          throw new AssertionError("unexpected call to " + str)
        }
    }
  }

  private def toString(
      functions: Seq[(String, MockFunction[_])],
      className: String
  ): String =
    s"""mock $className
       | ${functions.map(_._2.toString).mkString(", ")}
       |""".stripMargin

}
