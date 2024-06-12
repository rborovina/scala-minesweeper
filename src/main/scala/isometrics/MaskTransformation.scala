package isometrics

import transformations.Transformation

import scala.reflect.ClassTag

trait MaskTransformation[T] {

  def createMask(rows: Int, columns: Int)(implicit ct: ClassTag[T]): Transformation[Array[Array[T]]] = Transformation(
    trans = { existingMap =>
      existingMap.take(rows).map(_.take(columns))
    },
    inv = { existingMap =>
      existingMap
    }
  )

}
