package view

import actions.LoadGameAction
import controller.GameController
import model.{BombCell, Cell, EmptyCell}
import traits.{BoardManager, ScreenManager}
import types.{GameMap, GameSequence}
import view.dialogs.GameSelectionDialog

import javax.swing.{ImageIcon, Timer}
import java.nio.file.Paths
import scala.swing
import scala.swing.event.{ButtonClicked, MouseClicked}
import scala.swing.{Action, BorderPanel, Button, Dialog, GridPanel, Label, Menu, MenuBar, MenuItem}
import actions.LoadGameAction
import controller.GameController
import helpers.{FileHelper, GameHelper}
import model.{BombCell, Cell, EmptyCell}
import traits.{BoardManager, ScreenManager}
import types.{GameMap, GameSequence}
import view.dialogs.GameSelectionDialog

import javax.swing.ImageIcon
import java.nio.file.Paths
import scala.swing
import scala.swing.event.{ButtonClicked, MouseClicked}
import scala.swing.{Action, BorderPanel, Button, Dialog, GridPanel, Label, Menu, MenuBar, MenuItem}
import scala.concurrent.duration.*
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.swing.Swing.ActionListener
import scala.util.{Failure, Success}

class GameScreen(screenManager: ScreenManager, gameId: String, difficulty: String, map: GameMap, gameSequence: GameSequence, elapsedTime: Int) extends BaseGridScreen(screenManager) {

  private def handleGameOver(): Unit = {
    timer.stop()
    Dialog.showMessage(null, "It's a bomb!", title = "Game over")
  }

  private def handleGameWon(): Unit = {
    timer.stop()

    val (gameId, _, map, gameSequence, elapsedTime) = gameController.pauseTime().getGameData

    val totalBombs = gameController.totalBombs
    val userActionsCount = gameController.getUserActionsCount
    val hintsCount = gameController.getHintsCount

    val score = GameHelper.calculateScore(totalBombs, userActionsCount, hintsCount, elapsedTime)

    FileHelper.logCompletedGame(gameId, score, totalBombs, hintsCount, gameController.rows, gameController.columns, elapsedTime) match {
      case Success(_) => Dialog.showMessage(null, "You cleaned up the whole board!", title = "Game won!")
      case Failure(ex) => println(s"Failed to log game $gameId: ${ex.getMessage}")
    }
  }

  private var gameController: GameController = new GameController(gameId, difficulty, map, gameSequence, elapsedTime, onGameOver = handleGameOver, onGameWon = handleGameWon).resumeTime()

  private val timer = new Timer(1000, ActionListener(_ => updateElapsedTime()))

  override protected val controlPanel: GridPanel = new GridPanel(1, 4)
  override protected val gridPanel: GridPanel = new GridPanel(gameController.rows, gameController.columns)

  timer.start()
  drawScreen(gameController)

  menuBar = new MenuBar {
    contents += new Menu("File") {
      contents += new MenuItem(Action("Start a New Game") {
        GameSelectionDialog.showDifficultySelectionDialog((gameId, difficulty, map, gameSequence) => {
          screenManager.switchScreen(new GameScreen(screenManager, gameId, difficulty, map, gameSequence, 0))
          close()
        })
      })

      contents += new MenuItem(Action("Save Game") {
        val (gameId, _, map, gameSequence, elapsedTime) = gameController.pauseTime().getGameData
        LoadGameAction.saveGame(gameId, map, gameSequence, elapsedTime) {
          Dialog.showMessage(null, "Game Successfully saved", title = "Success")
        }
      })

      contents += new MenuItem(Action("Save Game and Go to Main Menu") {
        val (gameId, _, map, gameSequence, elapsedTime) = gameController.pauseTime().getGameData
        LoadGameAction.saveGame(gameId, map, gameSequence, elapsedTime) {
          Dialog.showMessage(null, "Game Successfully saved", title = "Success")
          screenManager.switchScreen(new MainMenuScreen(screenManager))
        }
      })

      contents += new MenuItem(Action("Go to Main Menu") {
        timer.stop()
        screenManager.switchScreen(new MainMenuScreen(screenManager))
      })
    }
  }

  contents = new BorderPanel {
    layout(controlPanel) = BorderPanel.Position.North
    layout(gridPanel) = BorderPanel.Position.Center
  }

  private def updateElapsedTime(): Unit = {
    drawStatsPanel(gameController)
  }

  override protected def handleCellClicked(row: Int, col: Int): Unit = {
    gameController = gameController.onCellClicked(row, col)
    drawScreen(gameController)
  }

  override protected def handleCellRightClicked(row: Int, col: Int): Unit = {
    gameController = gameController.onCellRightClicked(row, col)
    drawScreen(gameController)
  }

  override protected def drawStatsPanel(boardManager: BoardManager): Unit = {
    controlPanel.contents.clear()
    controlPanel.contents += new Label("Bombs: " + boardManager.totalBombs)
    controlPanel.contents += new Label("Flags: " + boardManager.totalFlags)
    controlPanel.contents += new Label(s"Time: ${gameController.getElapsedTime}")

    controlPanel.contents += new Button("Hint") {
      reactions += {
        case ButtonClicked(_) =>
          gameController = gameController.provideHint()
          drawScreen(gameController)
      }
    }

    controlPanel.revalidate()
    controlPanel.repaint()
  }

  override protected def drawBoard(boardManager: BoardManager): Unit = {
    gridPanel.contents.clear()

    gridPanel.contents ++= (
      for ((row, rowIndex) <- boardManager.board.zipWithIndex; (cell, colIndex) <- row.iterator.zipWithIndex) yield new Button {
        icon = cell match {
          case BombCell(isRevealed, isFlagged) if isRevealed && isFlagged => new ImageIcon(new ImageIcon(Paths.get(assetsPath, "flagged-mine.png").toString)
            .getImage.getScaledInstance(32, 32, java.awt.Image.SCALE_DEFAULT))
          case BombCell(isRevealed, _) if isRevealed => new ImageIcon(new ImageIcon(Paths.get(assetsPath, "mine.png").toString)
            .getImage.getScaledInstance(32, 32, java.awt.Image.SCALE_DEFAULT))
          case _ if cell.isFlagged => new ImageIcon(new ImageIcon(Paths.get(assetsPath, "flag.png").toString)
            .getImage.getScaledInstance(32, 32, java.awt.Image.SCALE_DEFAULT))
          case _ => null
        }
        text = cell match {
          case EmptyCell(neighboringBombs, _, _) if cell.isRevealed && neighboringBombs > 0 => neighboringBombs.toString
          case _ => ""
        }
        listenTo(mouse.clicks)
        reactions += {
          case MouseClicked(_, _, c, _, _) if c != 0 => handleCellRightClicked(rowIndex, colIndex)
          case ButtonClicked(_) => handleCellClicked(rowIndex, colIndex)
        }
        enabled = !cell.isRevealed
      })

    gridPanel.revalidate()
    gridPanel.repaint()
  }

}
