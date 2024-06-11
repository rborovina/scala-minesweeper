package view

import actions.LoadGameAction
import controller.MapCreationController
import model.BombCell
import traits.{BoardManager, ScreenManager}
import types.GameMap

import java.nio.file.Paths
import javax.swing.ImageIcon
import scala.swing.event.ButtonClicked
import scala.swing.{Action, BorderPanel, BoxPanel, Button, Dialog, GridPanel, Label, Menu, MenuBar, MenuItem}

class MapCreationScreen(screenManager: ScreenManager, mapName: String, difficulty: String, map: GameMap) extends BaseGridScreen(screenManager) {

  private var mapCreationController: MapCreationController = MapCreationController(mapName, difficulty, map)

  override protected val controlPanel: GridPanel = new GridPanel(1, 3)
  override protected val gridPanel: GridPanel = new GridPanel(mapCreationController.rows, mapCreationController.columns)

  drawScreen(mapCreationController)

  menuBar = new MenuBar {
    contents += new Menu("File") {
      contents += new MenuItem(Action("Save Map") {
        val (mapName, difficulty, map, gameSequence) = mapCreationController.getGameData
        LoadGameAction.saveNewMap(mapName, difficulty, map) {
          Dialog.showMessage(null, "New map created", title = "Success")
          screenManager.switchScreen(new MainMenuScreen(screenManager))
          close()
        }
      })

      contents += new MenuItem(Action("Go to Main Menu") {
        screenManager.switchScreen(new MainMenuScreen(screenManager))
      })
    }
  }

  contents = new BorderPanel {
    layout(controlPanel) = BorderPanel.Position.North

    layout(new BorderPanel {
      layout(new Button("+") {
        reactions += {
          case ButtonClicked(_) => updateGrid(mapCreationController.addColumnBefore)
        }
      }) = BorderPanel.Position.Center
    }) = BorderPanel.Position.West

    layout(new BorderPanel {
      layout(new BorderPanel {
        layout(new Button("+") {
          reactions += {
            case ButtonClicked(_) => updateGrid(mapCreationController.addRowBefore)
          }
        }) = BorderPanel.Position.Center
      }) = BorderPanel.Position.North

      layout(gridPanel) = BorderPanel.Position.Center

      layout(new BorderPanel {
        layout(new Button("+") {
          reactions += {
            case ButtonClicked(_) => updateGrid(mapCreationController.addRowAfter)
          }
        }) = BorderPanel.Position.Center
      }) = BorderPanel.Position.South

    }) = BorderPanel.Position.Center

    layout(new BorderPanel {
      layout(new Button("+") {
        reactions += {
          case ButtonClicked(_) => updateGrid(mapCreationController.addColumnAfter)
        }
      }) = BorderPanel.Position.Center
    }) = BorderPanel.Position.East

  }

  override protected def handleCellClicked(row: Int, col: Int): Unit = {
    mapCreationController = mapCreationController.onCellClicked(row, col)
    drawScreen(mapCreationController)
  }

  override protected def handleCellRightClicked(row: Int, col: Int): Unit = {}

  private def updateGrid(transform: GameMap => GameMap): Unit = {
    mapCreationController = mapCreationController.withUpdatedMap(transform)
    drawScreen(mapCreationController)
  }

  override protected def drawStatsPanel(boardManager: BoardManager): Unit = {
    controlPanel.contents.clear()
    controlPanel.contents += new Label("Bombs: " + boardManager.totalBombs)
    controlPanel.contents += new Button("Add Row Before") {
      reactions += {
        case ButtonClicked(_) =>
      }
    }
    controlPanel.contents += new Button("Add Row After") {
      reactions += {
        case ButtonClicked(_) =>
      }
    }

    controlPanel.revalidate()
    controlPanel.repaint()
  }

  override protected def drawBoard(boardManager: BoardManager): Unit = {
    gridPanel.contents.clear()

    gridPanel.rows = boardManager.rows
    gridPanel.columns = boardManager.columns

    gridPanel.contents ++= (
      for ((row, rowIndex) <- boardManager.board.zipWithIndex; (cell, colIndex) <- row.iterator.zipWithIndex) yield new Button {
        icon = cell match {
          case BombCell(_, _) => new ImageIcon(new ImageIcon(Paths.get(assetsPath, "mine.png").toString)
            .getImage.getScaledInstance(32, 32, java.awt.Image.SCALE_DEFAULT))
          case _ => null
        }
        reactions += {
          case ButtonClicked(_) => handleCellClicked(rowIndex, colIndex)
        }
      })

    gridPanel.revalidate()
    gridPanel.repaint()
  }

}
