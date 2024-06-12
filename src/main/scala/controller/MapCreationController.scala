package controller

import isometrics.{AxisSymmetries, MaskTransformation, Translations}
import model.{BombCell, EmptyCell}
import traits.{BoardManager, MapDifficulty}
import transformations.Transformation
import types.{Board, GameMap, GameSequence}

import scala.annotation.tailrec
import scala.reflect.ClassTag

class MapCreationController(mapName: String, difficulty: String, map: GameMap)
  extends BoardManager with AxisSymmetries[Char] with Translations[Char] with MaskTransformation[Char] {

  implicit val ct: ClassTag[Char] = ClassTag.Char

  override val rows: Int = if (map.isEmpty) 0 else map.length
  override val columns: Int = if (map.isEmpty || map(0).isEmpty) 0 else map(0).length

  override val board: Board = initialize(map)

  override val totalBombs: Int = countTotalBombs(board)
  override val totalFlags: Int = countTotalFlags(board)

  private def initialize(map: GameMap): Board = {
    map.map(row => row.map {
      case '#' => BombCell()
      case _ => EmptyCell()
    })
  }

  override def onCellClicked(row: Int, col: Int): MapCreationController = {
    val updatedElement = toggleElementValue(map(row)(col))
    val updatedMap = map.updated(row, map(row).updated(col, updatedElement))
    copy(updatedMap)
  }

  private def toggleElementValue(value: Char): Char = {
    value match {
      case '#' => '-'
      case _ => '#'
    }
  }

  override def onCellRightClicked(row: Int, col: Int): MapCreationController = this

  def selectSubmap(startRow: Int, startCol: Int, subRows: Int, subCols: Int): GameMap = {
    map.slice(startRow, startRow + subRows).map(_.slice(startCol, startCol + subCols))
  }

  def expandMap(finalRows: Int, finalCols: Int, startRow: Int, startCol: Int): Transformation[GameMap] = {
    Transformation(
      trans = { existingMap =>
        Array.tabulate(finalRows, finalCols) { (r, c) =>
          if (r >= startRow && r < startRow + existingMap.length && c >= startCol && c < startCol + existingMap(0).length)
            existingMap(r - startRow)(c - startCol)
          else '-'
        }
      },
      inv = { expandedMap =>
        expandedMap
      }
    )
  }


  def mergeMaps(map: GameMap)(startRow: Int, startCol: Int, transparent: Boolean): Transformation[GameMap] = {
    Transformation(
      trans = { submap =>
        map.zipWithIndex.map { case (row, rowIndex) =>
          row.zipWithIndex.map { case (cell, colIndex) =>
            if (rowIndex >= startRow && rowIndex < startRow + submap.length &&
              colIndex >= startCol && colIndex < startCol + submap(0).length) {
              val newRow = rowIndex - startRow
              val newCol = colIndex - startCol
              if (transparent && cell == '#') '#' else submap(newRow)(newCol)
            } else {
              cell
            }
          }
        }
      },
      inv = { _ =>
        map
      }
    )
  }


  def mergeTransparently(originalMap: GameMap): Transformation[GameMap] = {
    Transformation(
      trans = { transformedSubmap =>
        transformedSubmap.zipWithIndex.map { case (row, r) =>
          row.zipWithIndex.map { case (cell, c) =>
            if (originalMap(r)(c) == '#') '#' else cell
          }
        }
      },
      inv = { transformedSubmap =>
        transformedSubmap
      }
    )
  }

  private def countTotalBombs(board: Board): Int = {
    board.flatten.count {
      case BombCell(_, _) => true
      case _ => false
    }
  }

  private def countTotalFlags(board: Board): Int = {
    board.flatten.count(_.isFlagged)
  }

  private def wrapTranslationWithMask(dx: Int, dy: Int): Transformation[GameMap] = {
    Transformation(
      trans = { existingMap =>
        val translatedMap = translate(dx, dy).trans(existingMap)
        val newRows = existingMap.length - math.abs(dy)
        val newCols = if (existingMap.isEmpty) 0 else existingMap.head.length - math.abs(dx)
        val startX = math.abs(dx)
        val startY = math.abs(dy)
        createMask(newRows, newCols, startX, startY).trans(translatedMap)
      },
      inv = { existingMap =>
        existingMap
      }
    )
  }

  def addRowBefore: Transformation[GameMap] = translate(0, -1)

  def addRowAfter: Transformation[GameMap] = translate(0, 1)

  def addColumnBefore: Transformation[GameMap] = translate(1, 0)

  def addColumnAfter: Transformation[GameMap] = translate(-1, 0)

  def removeFirstRow: Transformation[GameMap] = wrapTranslationWithMask(0, 1)

  def removeLastRow: Transformation[GameMap] = wrapTranslationWithMask(0, -1)

  def removeFirstColumn: Transformation[GameMap] = wrapTranslationWithMask(-1, 0)

  def removeLastColumn: Transformation[GameMap] = wrapTranslationWithMask(1, 0)

  def withUpdatedMap(f: Transformation[GameMap]): MapCreationController = {
    copy(map = f(map))
  }

  private def applyTransformations(map: GameMap)(transformations: Array[Transformation[GameMap]]): GameMap = {
    transformations.foldLeft(map) { (currentMap, transformation) =>
      transformation.trans(currentMap)
    }
  }

  def updateWithTransformations(map: GameMap)(transformations: Array[Transformation[GameMap]]): MapCreationController = {
    val newMap = applyTransformations(map)(transformations)
    copy(map = newMap)
  }

  def updateMapWithTransformations(transformations: Array[Transformation[GameMap]]): MapCreationController = updateWithTransformations(map)(transformations)

  def isMapValid: Boolean = {
    val currentDifficulty = MapDifficulty.fromName(difficulty)

    val (minRows, maxRows) = currentDifficulty.rowsRange
    val (minCols, maxCols) = currentDifficulty.columnsRange
    val (minBombs, maxBombs) = currentDifficulty.bombsRange

    val validRows = map.length >= minRows && map.length <= maxRows
    val validCols = map(0).length >= minCols && map(0).length <= maxCols
    val validBombs = countTotalBombs(board) >= minBombs && countTotalBombs(board) <= maxBombs

    validRows && validCols && validBombs
  }

  def clearMap: Transformation[GameMap] = {
    Transformation(
      trans = { existingMap =>
        val rows = existingMap.length
        val cols = if (existingMap.isEmpty) 0 else existingMap(0).length
        Array.fill(rows, cols)('-')
      },
      inv = { existingMap =>
        existingMap
      }
    )
  }


  def getMapDifficulty: MapDifficulty = MapDifficulty.fromName(difficulty)

  override def getGameData: (String, String, GameMap, GameSequence) = {
    (mapName, difficulty, map, Array.empty)
  }

  def getMap: GameMap = map

  private def copy(map: GameMap): MapCreationController = {
    new MapCreationController(mapName, difficulty, map)
  }
}
