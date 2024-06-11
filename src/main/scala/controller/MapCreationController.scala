package controller

import model.{BombCell, EmptyCell}
import traits.BoardManager
import types.{Board, GameMap, GameSequence}

class MapCreationController(mapName: String, difficulty: String, map: GameMap) extends BoardManager {

  override val rows: Int = map.length
  override val columns: Int = map(0).length

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

  def addRowBefore: GameMap => GameMap = { existingMap =>
    val newRow = Array.fill(columns)('-')
    newRow +: existingMap
  }

  def addRowAfter: GameMap => GameMap = { existingMap =>
    val newRow = Array.fill(columns)('-')
    existingMap :+ newRow
  }

  def addColumnBefore: GameMap => GameMap = { existingMap =>
    existingMap.map(row => '-' +: row)
  }

  def addColumnAfter: GameMap => GameMap = { existingMap =>
    existingMap.map(row => row :+ '-')
  }

  def withUpdatedMap(f: GameMap => GameMap): MapCreationController = {
    copy(map = f(map))
  }

  override def getGameData: (String, String, GameMap, GameSequence) = {
    (mapName, difficulty, map, Array.empty)
  }

  private def copy(map: GameMap): MapCreationController = {
    new MapCreationController(mapName, difficulty, map)
  }
}
