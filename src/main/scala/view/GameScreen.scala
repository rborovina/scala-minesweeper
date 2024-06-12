package view

import actions.LoadGameAction
import controller.GameController
import model.{BombCell, Cell, EmptyCell}
import traits.{BoardManager, ScreenManager}
import types.{GameMap, GameSequence}
import view.dialogs.GameSelectionDialog

import javax.swing.ImageIcon
import java.nio.file.Paths
import scala.swing
import scala.swing.event.{ButtonClicked, MouseClicked}
import scala.swing.{Action, BorderPanel, Button, Dialog, GridPanel, Label, Menu, MenuBar, MenuItem}

class GameScreen(screenManager: ScreenManager, gameId: String, difficulty: String, map: GameMap, gameSequence: GameSequence) extends BaseGridScreen(screenManager) {

  private def handleGameOver(): Unit = {
    Dialog.showMessage(null, "It's a bomb!", title = "Game over")
  }

  private def handleGameWon(): Unit = {
    Dialog.showMessage(null, "You cleaned up the whole board!", title = "Game won!")
  }

  private var gameController: GameController = GameController(gameId, difficulty, map, gameSequence, onGameOver = handleGameOver, onGameWon = handleGameWon)

  override protected val controlPanel: GridPanel = new GridPanel(1, 4) // Adjust columns to fit the hint button
  override protected val gridPanel: GridPanel = new GridPanel(gameController.rows, gameController.columns)

  drawScreen(gameController)

  menuBar = new MenuBar {
    contents += new Menu("File") {
      contents += new MenuItem(Action("Start a New Game") {
        GameSelectionDialog.showDifficultySelectionDialog((gameId, difficulty, map, gameSequence) => {
          screenManager.switchScreen(new GameScreen(screenManager, gameId, difficulty, map, gameSequence))
          close()
        })
      })

      contents += new MenuItem(Action("Save Game") {
        val (gameId, _, map, gameSequence) = gameController.getGameData
        LoadGameAction.saveGame(gameId, map, gameSequence) {
          Dialog.showMessage(null, "Game Successfully saved", title = "Success")
        }
      })

      contents += new MenuItem(Action("Save Game and Go to Main Menu") {
        val (gameId, _, map, gameSequence) = gameController.getGameData
        LoadGameAction.saveGame(gameId, map, gameSequence) {
          Dialog.showMessage(null, "Game Successfully saved", title = "Success")
          screenManager.switchScreen(new MainMenuScreen(screenManager))
        }
      })

      contents += new MenuItem(Action("Go to Main Menu") {
        screenManager.switchScreen(new MainMenuScreen(screenManager))
      })
    }
  }

  contents = new BorderPanel {
    layout(controlPanel) = BorderPanel.Position.North
    layout(gridPanel) = BorderPanel.Position.Center
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
    controlPanel.contents += new Label("Time: 0") // TODO: Show actual time

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
