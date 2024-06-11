package view

import traits.{BoardManager, ScreenManager}

import java.nio.file.Paths
import scala.swing.{Dimension, GridPanel, MainFrame}

abstract class BaseGridScreen(screenManager: ScreenManager) extends MainFrame {
  title = "Minesweeper"
  preferredSize = new Dimension(800, 600)
  protected val assetsPath: String = Paths.get("assets").toAbsolutePath.toString

  protected val controlPanel: GridPanel
  protected val gridPanel: GridPanel

  protected def drawStatsPanel(boardManager: BoardManager): Unit

  protected def drawBoard(boardManager: BoardManager): Unit

  protected def drawScreen(boardManager: BoardManager): Unit = {
    drawStatsPanel(boardManager)
    drawBoard(boardManager)
  }

  protected def handleCellClicked(row: Int, col: Int): Unit

  protected def handleCellRightClicked(row: Int, col: Int): Unit
}
