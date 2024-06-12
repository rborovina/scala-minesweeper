package view

import actions.LoadGameAction
import controller.MapCreationController
import model.BombCell
import traits.{BoardManager, MapDifficulty, ScreenManager}
import transformations.Transformation
import types.GameMap

import java.nio.file.Paths
import javax.swing.ImageIcon
import scala.swing.Swing.EmptyBorder
import scala.swing.event.{ButtonClicked, MouseClicked}
import scala.swing.{Action, BorderPanel, BoxPanel, Button, CheckBox, Dialog, GridPanel, Label, Menu, MenuBar, MenuItem, Orientation, TextField, Swing}

class MapCreationScreen(screenManager: ScreenManager, mapName: String, difficulty: String, map: GameMap) extends BaseGridScreen(screenManager) {

  private var mapCreationController: MapCreationController = MapCreationController(mapName, difficulty, map)

  private val startRowField: TextField = new TextField { columns = 3 }
  private val startColField: TextField = new TextField { columns = 3 }
  private val numRowsField: TextField = new TextField { columns = 3 }
  private val numColsField: TextField = new TextField { columns = 3 }
  private val transparentCheck: CheckBox = new CheckBox("Transparent")

  override protected val controlPanel: GridPanel = new GridPanel(3, 3)
  override protected val gridPanel: GridPanel = new GridPanel(mapCreationController.rows, mapCreationController.columns)

  drawScreen(mapCreationController)

  menuBar = new MenuBar {
    contents += new Menu("File") {
      contents += new MenuItem(Action("Save Map") {
        val (mapName, difficulty, map, gameSequence) = mapCreationController.getGameData
        LoadGameAction.saveNewMap(mapName, difficulty, map) {
          if (mapCreationController.isMapValid) {
            Dialog.showMessage(null, "New map created", title = "Success")
            screenManager.switchScreen(new MainMenuScreen(screenManager))
            close()
          } else {
            Dialog.showMessage(null, s"Map is not valid for difficulty level $difficulty", title = "Error")
          }
        }
      })

      contents += new MenuItem(Action("Go to Main Menu") {
        screenManager.switchScreen(new MainMenuScreen(screenManager))
      })
    }
  }

  contents = new BorderPanel {
    layout(controlPanel) = BorderPanel.Position.North

    layout(new BoxPanel(Orientation.Vertical) {
      contents += new BorderPanel {
        layout(new Button("+") {
          reactions += {
            case ButtonClicked(_) => updateGrid(mapCreationController.addColumnBefore)
          }
        }) = BorderPanel.Position.Center
      }
      contents += new BorderPanel {
        layout(new Button("–") {
          reactions += {
            case ButtonClicked(_) => updateGrid(mapCreationController.removeFirstColumn)
          }
        }) = BorderPanel.Position.Center
      }
    }) = BorderPanel.Position.West

    layout(new BorderPanel {

      layout(new BoxPanel(Orientation.Horizontal) {
        contents += new BorderPanel {
          layout(new Button("+") {
            reactions += {
              case ButtonClicked(_) => updateGrid(mapCreationController.addRowBefore)
            }
          }) = BorderPanel.Position.Center
        }
        contents += new BorderPanel {
          layout(new Button("–") {
            reactions += {
              case ButtonClicked(_) => updateGrid(mapCreationController.removeFirstRow)
            }
          }) = BorderPanel.Position.Center
        }
      }) = BorderPanel.Position.North

      layout(gridPanel) = BorderPanel.Position.Center

      layout(new BoxPanel(Orientation.Horizontal) {
        contents += new BorderPanel {
          layout(new Button("+") {
            reactions += {
              case ButtonClicked(_) => updateGrid(mapCreationController.addRowAfter)
            }
          }) = BorderPanel.Position.Center
        }
        contents += new BorderPanel {
          layout(new Button("–") {
            reactions += {
              case ButtonClicked(_) => updateGrid(mapCreationController.removeLastRow)
            }
          }) = BorderPanel.Position.Center
        }
      }) = BorderPanel.Position.South

    }) = BorderPanel.Position.Center

    layout(new BoxPanel(Orientation.Vertical) {
      contents += new BorderPanel {
        layout(new Button("+") {
          reactions += {
            case ButtonClicked(_) => updateGrid(mapCreationController.addColumnAfter)
          }
        }) = BorderPanel.Position.Center
      }
      contents += new BorderPanel {
        layout(new Button("–") {
          reactions += {
            case ButtonClicked(_) => updateGrid(mapCreationController.removeLastColumn)
          }
        }) = BorderPanel.Position.Center
      }
    }) = BorderPanel.Position.East

    // Adding submap selection fields and buttons in a single row with border
    layout(new BoxPanel(Orientation.Horizontal) {
      contents += new Label("Start Row:")
      contents += startRowField
      contents += Swing.HStrut(10)
      contents += new Label("Start Column:")
      contents += startColField
      contents += Swing.HStrut(10)
      contents += new Label("Number of Rows:")
      contents += numRowsField
      contents += Swing.HStrut(10)
      contents += new Label("Number of Columns:")
      contents += numColsField
      contents += Swing.HStrut(10)
      contents += transparentCheck
      contents += Swing.HStrut(10)
      contents += new Button("Apply Transformation") {
        reactions += {
          case ButtonClicked(_) =>
            val startRow = startRowField.text.toInt
            val startCol = startColField.text.toInt
            val numRows = numRowsField.text.toInt
            val numCols = numColsField.text.toInt
            val transparent = transparentCheck.selected

            val submap = mapCreationController.selectSubmap(startRow, startCol, numRows, numCols)
            val transformedSubmap = mapCreationController.rotate90DegreesClockwise.trans(submap)
            mapCreationController = mapCreationController.mergeSubmap(startRow, startCol, transformedSubmap, transparent)
            drawScreen(mapCreationController)
        }
      }
      border = Swing.EmptyBorder(10, 10, 10, 10)
    }) = BorderPanel.Position.South
  }

  override protected def handleCellClicked(row: Int, col: Int): Unit = {
    mapCreationController = mapCreationController.onCellClicked(row, col)
    drawScreen(mapCreationController)
  }

  override protected def handleCellRightClicked(row: Int, col: Int): Unit = {
    startRowField.text = row.toString
    startColField.text = col.toString
  }

  private def updateGrid(transform: Transformation[GameMap]): Unit = {
    mapCreationController = mapCreationController.withUpdatedMap(transform)
    drawScreen(mapCreationController)
  }

  override protected def drawStatsPanel(boardManager: BoardManager): Unit = {
    val mapDifficulty: MapDifficulty = boardManager.getMapDifficulty

    controlPanel.contents.clear()
    controlPanel.contents += new Label(s"Bombs: ${boardManager.totalBombs} (Min: ${mapDifficulty.bombsRange._1} Max: ${mapDifficulty.bombsRange._2})")
    controlPanel.contents += new Label(s"Rows: ${boardManager.rows} (Min: ${mapDifficulty.rowsRange._1} Max: ${mapDifficulty.rowsRange._2})")
    controlPanel.contents += new Label(s"Columns: ${boardManager.columns} (Min: ${mapDifficulty.columnsRange._1} Max: ${mapDifficulty.columnsRange._2})")

    controlPanel.contents += new Button("Rotate Left") {
      reactions += {
        case ButtonClicked(_) => updateGrid(mapCreationController.rotate90DegreesCounterClockwise)
      }
    }

    controlPanel.contents += new Button("Rotate Right") {
      reactions += {
        case ButtonClicked(_) => updateGrid(mapCreationController.rotate90DegreesClockwise)
      }
    }

    controlPanel.contents += new Button("Reflect Horizontally") {
      reactions += {
        case ButtonClicked(_) => updateGrid(mapCreationController.reflectHorizontally)
      }
    }
    controlPanel.contents += new Button("Reflect Vertically") {
      reactions += {
        case ButtonClicked(_) => updateGrid(mapCreationController.reflectVertically)
      }
    }
    controlPanel.contents += new Button("Reflect Diagonally") {
      reactions += {
        case ButtonClicked(_) => updateGrid(mapCreationController.reflectDiagonally)
      }
    }

    controlPanel.border = EmptyBorder(10, 10, 10, 10)

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
        listenTo(mouse.clicks)
        reactions += {
          case MouseClicked(_, _, c, _, _) if c != 0 => handleCellRightClicked(rowIndex, colIndex)
          case ButtonClicked(_) => handleCellClicked(rowIndex, colIndex)
        }
      })

    gridPanel.revalidate()
    gridPanel.repaint()
  }

}
