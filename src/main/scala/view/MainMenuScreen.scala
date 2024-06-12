package view

import traits.ScreenManager
import view.dialogs.GameSelectionDialog

import scala.swing.Swing.{EmptyBorder, VStrut}
import scala.swing.event.ButtonClicked
import scala.swing.{Alignment, BorderPanel, BoxPanel, Button, Dialog, GridPanel, Label, MainFrame, Orientation}

class MainMenuScreen(screenManager: ScreenManager) extends MainFrame {
  title = "Minesweeper"
  resizable = false

  private val gameTitle = new Label {
    text = "<html><h1>Minesweeper</h1></html>"
    horizontalAlignment = Alignment.Center
  }

  private val gameInfo = new Label {
    text = "<html><h3>Version: 1.0</h3><h3>Author: Radmilo Borovina 2023/3483</h3></html>"
    horizontalAlignment = Alignment.Center
  }

  private val newGameButton = new Button {
    text = "Start a New Game"
  }

  private val loadGameButton = new Button {
    text = "Load Existing Game"
  }

  private val createLevelButton = new Button {
    text = "Create a New Game Level"
  }

  private val viewScoresButton = new Button {
    text = "View Best Scores"
  }

  listenTo(newGameButton, loadGameButton, createLevelButton, viewScoresButton)

  reactions += {
    case ButtonClicked(`newGameButton`) =>
      GameSelectionDialog.showDifficultySelectionDialog((gameId, difficulty, map, gameSequence) => {
        screenManager.switchScreen(new GameScreen(screenManager, gameId, difficulty, map, gameSequence, 0))
        close()
      })
    case ButtonClicked(`loadGameButton`) =>
      GameSelectionDialog.showGameSelectionDialog((gameId, map, gameSequence, elapsedTime) => {
        screenManager.switchScreen(new GameScreen(screenManager, gameId, "", map, gameSequence, elapsedTime))
        close()
      })
    case ButtonClicked(`createLevelButton`) =>
      GameSelectionDialog.showCreateNewMapDialog((mapName, difficulty, map) => {
        screenManager.switchScreen(new MapCreationScreen(screenManager, mapName, difficulty, map))
        close()
      })
    case ButtonClicked(`viewScoresButton`) =>
      Dialog.showMessage(null, "View best scores clicked", title = "Best Scores")
  }

  contents = new BorderPanel {
    layout(new BoxPanel(Orientation.Vertical) {
      contents += gameTitle
      contents += gameInfo
      contents += VStrut(10)
    }) = BorderPanel.Position.North

    layout(new BoxPanel(Orientation.Horizontal) {
      contents += newGameButton
      contents += loadGameButton
      contents += createLevelButton
      contents += viewScoresButton
    }) = BorderPanel.Position.Center

    border = EmptyBorder(10, 10, 10, 10)
  }

}
