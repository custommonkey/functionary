package functionary

import java.lang.reflect.Method
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

  def mock[A: ClassTag](functions: (String, MockFunction[_])*): A = {
    val xxx = functions.toMap
    java.lang.reflect.Proxy
      .newProxyInstance(
        getClass.getClassLoader,
        Array(classTag[A].runtimeClass),
        { (_: Any, method: Method, args: Array[AnyRef]) =>
          xxx.get(method.getName) match {
            case Some(apply) => apply(args.toSeq).asInstanceOf[AnyRef]
            case None =>
              val className = classTag[A].runtimeClass.getSimpleName
              throw new AssertionError(
                s"""unexpected call to mock function $className.${method.getName}
                   | ${functions
                    .map { case (name, function) => function.toString(name) }
                    .mkString(", ")}
                   |""".stripMargin
              )
          }
        }
      )
      .asInstanceOf[A]

  }
}
