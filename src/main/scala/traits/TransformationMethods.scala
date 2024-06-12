package traits

import transformations.Transformation
import types.GameMap

trait TransformationMethods {
  def addRowBefore: Transformation[GameMap]

  def addRowAfter: Transformation[GameMap]

  def addColumnBefore: Transformation[GameMap]

  def addColumnAfter: Transformation[GameMap]

  def removeFirstRow: Transformation[GameMap]

  def removeLastRow: Transformation[GameMap]

  def removeFirstColumn: Transformation[GameMap]

  def removeLastColumn: Transformation[GameMap]

  def rotate90DegreesClockwise: Transformation[GameMap]

  def rotate90DegreesCounterClockwise: Transformation[GameMap]

  def reflectHorizontally: Transformation[GameMap]

  def reflectVertically: Transformation[GameMap]

  def reflectDiagonally: Transformation[GameMap]

  def inverse(transformation: Transformation[GameMap]): Transformation[GameMap]

  def translate(dx: Int, dy: Int): Transformation[GameMap]

  def centralSymmetry: Transformation[GameMap]
}
