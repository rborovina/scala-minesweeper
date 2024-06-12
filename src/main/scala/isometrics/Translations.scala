package isometrics

import transformations.Transformation

import scala.annotation.tailrec
import scala.reflect.ClassTag

trait Translations[T] {

  def translate(dx: Int, dy: Int)(implicit ct: ClassTag[T]): Transformation[Array[Array[T]]] = {
    @tailrec
    def translateHelper(dx: Int, dy: Int, accumulated: Transformation[Array[Array[T]]]): Transformation[Array[Array[T]]] = {
      if (dx == 0 && dy == 0) accumulated
      else if (dx > 0) {
        translateHelper(dx - 1, dy, accumulated.andThen(shiftRight))
      } else if (dx < 0) {
        translateHelper(dx + 1, dy, accumulated.andThen(shiftLeft))
      } else if (dy > 0) {
        translateHelper(dx, dy - 1, accumulated.andThen(shiftDown))
      } else {
        translateHelper(dx, dy + 1, accumulated.andThen(shiftUp))
      }
    }

    val trans = translateHelper(dx, dy, Transformation(identity, identity))
    val inv = translateHelper(-dx, -dy, Transformation(identity, identity))

    Transformation(trans.trans, inv.trans)
  }

  private def shiftRight(implicit ct: ClassTag[T]): Transformation[Array[Array[T]]] = Transformation(
    trans = { existingMap =>
      existingMap.map(row => Array.fill(1)(null.asInstanceOf[T]) ++ row)
    },
    inv = { existingMap =>
      if (existingMap.nonEmpty && existingMap.head.nonEmpty) existingMap.map(_.tail) else existingMap
    }
  )

  private def shiftLeft(implicit ct: ClassTag[T]): Transformation[Array[Array[T]]] = Transformation(
    trans = { existingMap =>
      existingMap.map(row => row :+ null.asInstanceOf[T])
    },
    inv = { existingMap =>
      if (existingMap.nonEmpty && existingMap.head.nonEmpty) existingMap.map(_.init) else existingMap
    }
  )

  private def shiftDown(implicit ct: ClassTag[T]): Transformation[Array[Array[T]]] = Transformation(
    trans = { existingMap =>
      val columns = if (existingMap.isEmpty || existingMap.head.isEmpty) 0 else existingMap.head.length
      val newRow = Array.fill[T](columns)(null.asInstanceOf[T])
      existingMap :+ newRow
    },
    inv = { existingMap =>
      if (existingMap.nonEmpty) existingMap.init else existingMap
    }
  )

  private def shiftUp(implicit ct: ClassTag[T]): Transformation[Array[Array[T]]] = Transformation(
    trans = { existingMap =>
      val columns = if (existingMap.isEmpty || existingMap.head.isEmpty) 0 else existingMap.head.length
      val newRow = Array.fill[T](columns)(null.asInstanceOf[T])
      newRow +: existingMap
    },
    inv = { existingMap =>
      if (existingMap.nonEmpty) existingMap.tail else existingMap
    }
  )
}
