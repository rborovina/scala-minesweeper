package controller

import actions.UserAction
import actions.UserAction.{Hint, LeftClick, RightClick}
import model.{BombCell, Cell, EmptyCell}
import traits.{BoardManager, MapDifficulty}
import types.{Board, GameMap, GameSequence}

import scala.annotation.tailrec
import scala.collection.immutable.Queue
import scala.collection.immutable.Set
import scala.reflect.ClassTag

class GameController(gameId: String, difficulty: String, map: GameMap, gameSequence: GameSequence = Array.empty, elapsedTime: Int = 0, onGameOver: () => Unit, onGameWon: () => Unit) extends BoardManager {

  override val rows: Int = map.length
  override val columns: Int = map(0).length

  override val board: Board = initialize(map, gameSequence)

  override val totalBombs: Int = countTotalBombs(board)

  override val totalFlags: Int = countTotalFlags(board)

  private def initialize(map: GameMap, gameSequence: GameSequence): Board = {
    val board: Board = map.map(row => row.map {
      case '#' => BombCell()
      case _ => EmptyCell()
    })

    val boardWithNeighboringBombs = calculateNeighboringBombs(board)
    setupGameState(boardWithNeighboringBombs, gameSequence)
  }

  private def setupGameState(board: Board, gameSequence: GameSequence): Board = {
    @annotation.tailrec
    def applyActions(currentBoard: Board, sequences: GameSequence, index: Int): Board = {
      if (index >= sequences.length) {
        currentBoard
      } else {
        val (action, row, col) = sequences(index)
        val updatedBoard = action match {
          case LeftClick => handleLeftClick(currentBoard, row, col)
          case RightClick => handleRightClick(currentBoard, row, col)
          case Hint => handleLeftClick(currentBoard, row, col)
        }
        applyActions(updatedBoard, sequences, index + 1)
      }
    }

    applyActions(board, gameSequence, 0)
  }

  private def getCellByPosition(board: Board, row: Int, col: Int): Cell = board(row)(col)

  private def calculateNeighboringBombs(board: Board): Board = {
    val directions = List(
      (-1, -1), (-1, 0), (-1, 1),
      (0, -1), (0, 1),
      (1, -1), (1, 0), (1, 1)
    )

    val newBoard = Array.ofDim[Cell](board.length, board(0).length)

    for {
      row <- board.indices
      col <- board(row).indices
    } {
      newBoard(row)(col) = board(row)(col) match {
        case EmptyCell(_, _, _) =>
          val bombCount = directions.count { case (dr, dc) =>
            val newRow = row + dr
            val newCol = col + dc
            newRow >= 0 && newRow < board.length &&
              newCol >= 0 && newCol < board(row).length && {
              board(newRow)(newCol) match {
                case BombCell(_, _) => true
                case _ => false
              }
            }
          }
          EmptyCell(bombCount)
        case cell => cell
      }
    }

    newBoard
  }

  private def revealNeighboringCells(board: Board, row: Int, col: Int): Board = {

    def isCellPositionValid(row: Int, col: Int): Boolean = {
      row >= 0 && row < board.length && col >= 0 && col < board(0).length
    }

    @tailrec
    def revealNeighbors(queue: Queue[(Int, Int)], visited: Set[(Int, Int)], board: Board): Board = {
      if (queue.isEmpty) board
      else {
        val ((row, col), rest) = queue.dequeue
        if (visited.contains((row, col)) || !isCellPositionValid(row, col)) revealNeighbors(rest, visited, board)
        else {
          val updatedVisited = visited + ((row, col))

          board(row)(col) match
            case EmptyCell(neighboringBombs, _, isFlagged) if neighboringBombs == 0 && !isFlagged =>
              val updatedBoard = board.updated(row, board(row).updated(col, board(row)(col).reveal()))
              val neighbors = for {
                dr <- -1 to 1
                dc <- -1 to 1
                if isCellPositionValid(row + dr, col + dc)
              } yield (row + dr, col + dc)

              val newNeighbors = neighbors.filterNot(updatedVisited.contains)
              val newQueue = rest.enqueueAll(newNeighbors)
              revealNeighbors(newQueue, updatedVisited, updatedBoard)
            case EmptyCell(_, _, isFlagged) if !isFlagged =>
              val updatedBoard = board.updated(row, board(row).updated(col, board(row)(col).reveal()))
              revealNeighbors(rest, updatedVisited, updatedBoard)
            case _ =>
              revealNeighbors(rest, updatedVisited, board)
        }
      }
    }

    val visited = Set.empty[(Int, Int)]
    val queue = Queue((row, col))
    revealNeighbors(queue, visited, board.map(_.clone))
  }

  private def handleLeftClick(board: Board, row: Int, col: Int): Board = {
    val cell = getCellByPosition(board, row, col)

    if (cell.isFlagged) {
      return board
    }

    val updatedCell = cell.reveal()

    board(row)(col) = updatedCell

    updatedCell match
      case BombCell(_, _) =>
        onGameOver()
        revealAllCells(board)
      case _ if isGameCompleted(board) =>
        onGameWon()
        revealAllCells(board)
      case _ => revealNeighboringCells(board, row, col)
  }

  private def handleRightClick(board: Board, row: Int, col: Int): Board = {
    val cell = getCellByPosition(board, row, col)

    if (cell.isRevealed) {
      return board
    }

    board(row)(col) = cell.toggleFlag()

    board
  }

  override def onCellClicked(row: Int, col: Int): GameController = {
    copy(gameSequence :+ (UserAction.LeftClick, row, col))
  }

  override def onCellRightClicked(row: Int, col: Int): GameController = {
    copy(gameSequence :+ (UserAction.RightClick, row, col))
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

  private def isGameCompleted(board: Board): Boolean = {
    board.flatten.forall {
      case BombCell(isRevealed, _) => !isRevealed
      case EmptyCell(_, isRevealed, _) => isRevealed
    }
  }

  private def revealAllCells(board: Board): Board = {
    board.map(_.map(cell => cell.reveal()))
  }

  def getMapDifficulty: MapDifficulty = MapDifficulty.fromName(difficulty)

  private var startTime: Long = System.currentTimeMillis()

  def getElapsedTime: Int = {
    elapsedTime + ((System.currentTimeMillis() - startTime) / 1000).toInt
  }

  def pauseTime(): GameController = {
    val currentElapsedTime = getElapsedTime
    new GameController(gameId, difficulty, map, gameSequence, currentElapsedTime, onGameOver, onGameWon)
  }

  def resumeTime(): GameController = {
    val newController = copy()
    newController.startTime = System.currentTimeMillis()
    newController
  }


  def provideHint(): GameController = {
    val safeCells = for {
      (row, rowIndex) <- board.zipWithIndex
      (cell, colIndex) <- row.zipWithIndex
      if !cell.isInstanceOf[BombCell] && !cell.isRevealed && !cell.isFlagged
    } yield (rowIndex, colIndex)

    if (safeCells.nonEmpty) {
      val (hintRow, hintCol) = safeCells(scala.util.Random.nextInt(safeCells.length))

      copy(gameSequence :+ (UserAction.Hint, hintRow, hintCol))
    } else {
      this
    }
  }

  private def copy(gameSequence: GameSequence = gameSequence, elapsedTime: Int = getElapsedTime): GameController = {
    new GameController(gameId, difficulty, map, gameSequence, elapsedTime, onGameOver, onGameWon)
  }

  override def getGameData: (String, String, GameMap, GameSequence, Int) = {
    (gameId, difficulty, map, gameSequence, getElapsedTime)
  }
}
