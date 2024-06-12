package isometrics

import transformations.Transformation

import scala.reflect.ClassTag

trait MaskTransformation[T] {
  def createMask(rows: Int, cols: Int, startX: Int, startY: Int)(implicit ct: ClassTag[T]): Transformation[Array[Array[T]]] = Transformation(
    trans = { existingMap =>
      Array.tabulate(rows, cols) { (i, j) =>
        if (i + startY < existingMap.length && j + startX < existingMap.head.length)
          existingMap(i + startY)(j + startX)
        else
          null.asInstanceOf[T]
      }
    },
    inv = { existingMap =>
      existingMap
    }
  )
}
