package functionary

import sourcecode.File
import sourcecode.Line

case class Location(file: File, line: Line) {
  override def toString: String = s"${file.value}:${line.value}"
}

object Location {
  implicit def location(implicit line: Line, file: File): Location =
    Location(file, line)
}
