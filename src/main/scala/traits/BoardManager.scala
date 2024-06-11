package traits

import types.{Board, GameMap, GameSequence}

trait BoardManager {
  val board: Board
  val rows: Int
  val columns: Int
  val totalBombs: Int
  val totalFlags: Int

  def onCellClicked(row: Int, col: Int): BoardManager

  def onCellRightClicked(row: Int, col: Int): BoardManager

  def getGameData: (String, String, GameMap, GameSequence)
}
