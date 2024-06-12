package isometrics

import transformations.Transformation

import scala.reflect.ClassTag

trait AxisSymmetries[T] {

  def rotate90DegreesClockwise(implicit ct: ClassTag[T]): Transformation[Array[Array[T]]] = Transformation(
    trans = { existingMap =>
      val transposed = existingMap.transpose
      transposed.map(_.reverse)
    },
    inv = { existingMap =>
      val transposed = existingMap.transpose
      transposed.reverse
    }
  )

  def rotate90DegreesCounterClockwise(implicit ct: ClassTag[T]): Transformation[Array[Array[T]]] = Transformation(
    trans = { existingMap =>
      val transposed = existingMap.transpose
      transposed.reverse
    },
    inv = { existingMap =>
      val transposed = existingMap.transpose
      transposed.map(_.reverse)
    }
  )

  def reflectHorizontally(implicit ct: ClassTag[T]): Transformation[Array[Array[T]]] = Transformation(
    trans = { existingMap =>
      if (existingMap.nonEmpty) existingMap.map(_.reverse) else existingMap
    },
    inv = { existingMap =>
      if (existingMap.nonEmpty) existingMap.map(_.reverse) else existingMap
    }
  )

  def reflectVertically(implicit ct: ClassTag[T]): Transformation[Array[Array[T]]] = Transformation(
    trans = { existingMap =>
      if (existingMap.nonEmpty) existingMap.reverse else existingMap
    },
    inv = { existingMap =>
      if (existingMap.nonEmpty) existingMap.reverse else existingMap
    }
  )

  def reflectDiagonally(implicit ct: ClassTag[T]): Transformation[Array[Array[T]]] = Transformation(
    trans = { existingMap =>
      if (existingMap.nonEmpty && existingMap.head.nonEmpty) existingMap.transpose else existingMap
    },
    inv = { existingMap =>
      if (existingMap.nonEmpty && existingMap.head.nonEmpty) existingMap.transpose else existingMap
    }
  )

  def centralSymmetry(implicit ct: ClassTag[T]): Transformation[Array[Array[T]]] = Transformation(
    trans = reflectVertically.andThen(reflectHorizontally).trans,
    inv = reflectVertically.andThen(reflectHorizontally).trans
  )

}
