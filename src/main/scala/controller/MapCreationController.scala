package controller

import isometrics.{AxisSymmetries, Translations}
import model.{BombCell, EmptyCell}
import traits.{BoardManager, MapDifficulty}
import transformations.Transformation
import types.{Board, GameMap, GameSequence}

import scala.annotation.tailrec
import scala.reflect.ClassTag

class MapCreationController(mapName: String, difficulty: String, map: GameMap)
  extends BoardManager with AxisSymmetries[Char] with Translations[Char] {

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

  private def countTotalBombs(board: Board): Int = {
    board.flatten.count {
      case BombCell(_, _) => true
      case _ => false
    }
  }

  private def countTotalFlags(board: Board): Int = {
    board.flatten.count(_.isFlagged)
  }

  private def addEmptyRow(atStart: Boolean): Transformation[GameMap] = Transformation(
    trans = { existingMap =>
      val columns = if (existingMap.isEmpty || existingMap(0).isEmpty) 0 else existingMap(0).length
      val newRow = Array.fill(columns)('-')
      if (atStart) newRow +: existingMap else existingMap :+ newRow
    },
    inv = { existingMap =>
      if (existingMap.nonEmpty) {
        if (atStart) existingMap.tail else existingMap.init
      } else existingMap
    }
  )

  private def addEmptyColumn(atStart: Boolean): Transformation[GameMap] = Transformation(
    trans = { existingMap =>
      if (atStart) existingMap.map(row => '-' +: row) else existingMap.map(row => row :+ '-')
    },
    inv = { existingMap =>
      if (existingMap.nonEmpty && existingMap.head.nonEmpty) {
        if (atStart) existingMap.map(_.tail) else existingMap.map(_.init)
      } else existingMap
    }
  )

  def addRowBefore: Transformation[GameMap] = translate(0, -1)

  def addRowAfter: Transformation[GameMap] = translate(0, 1)

  def addColumnBefore: Transformation[GameMap] = translate(1, 0)

  def addColumnAfter: Transformation[GameMap] = translate(-1, 0)

  def removeFirstRow: Transformation[GameMap] = translate(0, -1)

  def removeLastRow: Transformation[GameMap] = translate(0, 1)

  def removeFirstColumn: Transformation[GameMap] = translate(-1, 0)

  def removeLastColumn: Transformation[GameMap] = translate(1, 0)

  def withUpdatedMap(f: Transformation[GameMap]): MapCreationController = {
    copy(map = f(map))
  }

  private def applyTransformations(transformations: Transformation[GameMap]): GameMap = {
    transformations(map)
  }

  def updateMapWithTransformations(transformations: Transformation[GameMap]): MapCreationController = {
    val newMap = applyTransformations(transformations)
    copy(map = newMap)
  }

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

  def clearArea(startRow: Int, startCol: Int, width: Int, height: Int): MapCreationController = {
    @tailrec
    def clear(rows: GameMap, rowIndex: Int = 0): GameMap = {
      if (rowIndex >= rows.length) rows
      else {
        val updatedRow = rows(rowIndex).zipWithIndex.map { case (cell, colIndex) =>
          if (rowIndex >= startRow && rowIndex < startRow + height &&
            colIndex >= startCol && colIndex < startCol + width &&
            cell == '#') '-' else cell
        }
        clear(rows.updated(rowIndex, updatedRow), rowIndex + 1)
      }
    }

    val updatedMap = clear(map)
    copy(updatedMap)
  }

  def getMapDifficulty: MapDifficulty = MapDifficulty.fromName(difficulty)

  override def getGameData: (String, String, GameMap, GameSequence) = {
    (mapName, difficulty, map, Array.empty)
  }

  private def copy(map: GameMap): MapCreationController = {
    new MapCreationController(mapName, difficulty, map)
  }
}
