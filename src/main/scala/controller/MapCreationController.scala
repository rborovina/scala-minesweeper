package controller

import model.{BombCell, EmptyCell}
import traits.{BoardManager, MapDifficulty}
import transformations.Transformation
import types.{Board, GameMap, GameSequence}

import scala.annotation.tailrec

class MapCreationController(mapName: String, difficulty: String, map: GameMap) extends BoardManager {

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
    value match
      case '#' => '-'
      case _ => '#'
  }

  override def onCellRightClicked(row: Int, col: Int): MapCreationController = {
    this
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

  def addRowBefore: Transformation = Transformation { existingMap =>
    val columns: Int = if (existingMap.isEmpty || existingMap(0).isEmpty) 0 else existingMap(0).length
    val newRow = Array.fill(columns)('-')
    newRow +: existingMap
  }

  def addRowAfter: Transformation = Transformation { existingMap =>
    val columns: Int = if (existingMap.isEmpty || existingMap(0).isEmpty) 0 else existingMap(0).length
    val newRow = Array.fill(columns)('-')
    existingMap :+ newRow
  }

  def addColumnBefore: Transformation = Transformation { existingMap =>
    existingMap.map(row => '-' +: row)
  }

  def addColumnAfter: Transformation = Transformation { existingMap =>
    existingMap.map(row => row :+ '-')
  }

  def removeFirstRow: Transformation = Transformation { existingMap =>
    if (existingMap.nonEmpty) existingMap.tail else existingMap
  }

  def removeLastRow: Transformation = Transformation { existingMap =>
    if (existingMap.nonEmpty) existingMap.init else existingMap
  }

  def removeFirstColumn: Transformation = Transformation { existingMap =>
    if (existingMap.nonEmpty && existingMap.head.nonEmpty) existingMap.map(_.tail) else existingMap
  }

  def removeLastColumn: Transformation = Transformation { existingMap =>
    if (existingMap.nonEmpty && existingMap.head.nonEmpty) existingMap.map(_.init) else existingMap
  }

  def rotate90DegreesClockwise: Transformation = Transformation { existingMap =>
    val transposed = existingMap.transpose
    transposed.map(_.reverse)
  }

  def rotate90DegreesCounterClockwise: Transformation = Transformation { existingMap =>
    val transposed = existingMap.transpose
    transposed.reverse
  }

  def reflectHorizontally: Transformation = Transformation { existingMap =>
    existingMap.map(_.reverse)
  }

  def reflectVertically: Transformation = Transformation { existingMap =>
    existingMap.reverse
  }

  def reflectDiagonally: Transformation = Transformation { existingMap =>
    val transposed = existingMap.transpose
    transposed.map(_.reverse)
  }

  def inverse(transformation: Transformation): Transformation = Transformation { existingMap =>
    transformation(transformation(transformation(transformation(existingMap))))
  }

  def translate(dx: Int, dy: Int): Transformation = Transformation { existingMap =>
    if (dx == 0 && dy == 0) existingMap
    else {
      val newMap = Array.fill(rows + math.abs(dy))(Array.fill(columns + math.abs(dx))('-'))
      for (r <- existingMap.indices; c <- existingMap(r).indices) {
        val newRow = r + (if (dy > 0) dy else 0)
        val newCol = c + (if (dx > 0) dx else 0)
        newMap(newRow)(newCol) = existingMap(r)(c)
      }
      newMap
    }
  }

  def centralSymmetry: Transformation = Transformation { existingMap =>
    reflectVertically.andThen(reflectHorizontally)(existingMap)
  }

  def withUpdatedMap(f: Transformation): MapCreationController = {
    copy(map = f(map))
  }

  private def applyTransformations(transformations: Transformation): GameMap = {
    transformations(map)
  }

  def updateMapWithTransformations(transformations: Transformation): MapCreationController = {
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
