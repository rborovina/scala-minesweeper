package view.dialogs

import actions.LoadGameAction
import helpers.GameHelper
import types.{GameMap, GameSequence}

import scala.swing.{BoxPanel, Button, ComboBox, Dialog, Label, ListView, MainFrame, Orientation, ScrollPane, TextField}
import scala.swing.event.SelectionChanged
import scala.swing.Swing._

object GameSelectionDialog {

  def showDifficultySelectionDialog(callback: (String, String, GameMap, GameSequence) => Unit): Unit = {
    val difficulties = Seq("Beginner", "Intermediate", "Hard")
    val difficultyComboBox = new ComboBox(difficulties)

    val mapList = new ListView(Seq.empty[String])
    mapList.selection.intervalMode = ListView.IntervalMode.Single
    mapList.peer.setVisibleRowCount(5)

    val randomMapButton = new Button("Select Random Map")

    val panel = new BoxPanel(Orientation.Vertical) {
      contents += new Label("Select Game Difficulty")
      contents += difficultyComboBox
      contents += new Label("Select Map:")
      contents += new ScrollPane(mapList)
      contents += randomMapButton
      border = EmptyBorder(10, 10, 10, 10)
    }

    def refreshMapList(difficulty: String): Unit = {
      LoadGameAction.loadGamesForGameType(difficulty) { maps =>
        mapList.listData = maps
      }
    }

    refreshMapList(difficulties.head)

    difficultyComboBox.listenTo(difficultyComboBox.selection)
    difficultyComboBox.reactions += {
      case SelectionChanged(_) =>
        val selectedDifficulty = difficultyComboBox.selection.item
        refreshMapList(selectedDifficulty)
    }

    val frame = new MainFrame()
    frame.visible = true

    randomMapButton.reactions += {
      case scala.swing.event.ButtonClicked(_) =>
        if (mapList.listData.nonEmpty) {
          val randomMap = mapList.listData(scala.util.Random.nextInt(mapList.listData.size))
          val selectedDifficulty = difficultyComboBox.selection.item
          LoadGameAction.loadGame(selectedDifficulty, randomMap) { (map, gameSequence, _) =>
            callback(GameHelper.generateUniqueFilename(), selectedDifficulty, map, gameSequence)
            frame.close()
          }
        } else {
          Dialog.showMessage(null, "No maps available to select randomly.", title = "Error")
        }
    }

    val result = Dialog.showConfirmation(
      frame,
      panel.peer,
      title = "Select Difficulty",
      optionType = Dialog.Options.OkCancel
    )

    if (result == Dialog.Result.Ok) {
      val selectedDifficulty = difficultyComboBox.selection.item
      val selectedMapOption = mapList.selection.items.headOption
      val selectedMap = selectedMapOption.getOrElse("")
      if (selectedMap.nonEmpty) {
        LoadGameAction.loadGame(selectedDifficulty, selectedMap)((map, gameSequence, _) => {
          callback(GameHelper.generateUniqueFilename(), selectedDifficulty, map, gameSequence)
          frame.close()
        })
      } else {
        println("No map selected")
      }
    }
  }

  def showGameSelectionDialog(callback: (String, GameMap, GameSequence, Int) => Unit): Unit = {
    val mapList = new ListView(Seq.empty[String])
    mapList.selection.intervalMode = ListView.IntervalMode.Single
    mapList.peer.setVisibleRowCount(5)

    LoadGameAction.loadGamesForGameType("Games") { maps =>
      mapList.listData = maps
    }

    val randomMapButton = new Button("Select Random Map")

    val panel = new BoxPanel(Orientation.Vertical) {
      contents += new Label("Select Game:")
      contents += new ScrollPane(mapList)
      contents += randomMapButton
      border = EmptyBorder(10, 10, 10, 10)
    }

    val frame = new MainFrame()
    frame.visible = true

    randomMapButton.reactions += {
      case scala.swing.event.ButtonClicked(_) =>
        if (mapList.listData.nonEmpty) {
          val randomMap = mapList.listData(scala.util.Random.nextInt(mapList.listData.size))
          LoadGameAction.loadGame("Games", randomMap) { (map, gameSequence, elapsedTime) =>
            callback(randomMap, map, gameSequence, elapsedTime)
            frame.close()
          }
        } else {
          Dialog.showMessage(null, "No maps available to select randomly.", title = "Error")
        }
    }


    val result = Dialog.showConfirmation(
      frame,
      panel.peer,
      title = "Select Difficulty",
      optionType = Dialog.Options.OkCancel
    )

    if (result == Dialog.Result.Ok) {
      val selectedMapOption = mapList.selection.items.headOption
      val gameId = selectedMapOption.getOrElse("")
      if (gameId.nonEmpty) {
        LoadGameAction.loadGame("Games", gameId)((map, gameSequence, elapsedTime) => {
          callback(gameId, map, gameSequence, elapsedTime)
          frame.close()
        })
      } else {
        println("No map selected")
      }
    }
  }

  def showCreateNewMapDialog(callback: (String, String, GameMap) => Unit): Unit = {
    val difficulties = Seq("Beginner", "Intermediate", "Hard")
    val difficultyComboBox = new ComboBox(difficulties)

    val mapList = new ListView(Seq.empty[String])
    mapList.selection.intervalMode = ListView.IntervalMode.Single
    mapList.peer.setVisibleRowCount(5)

    val mapNameField = new TextField()

    val panel = new BoxPanel(Orientation.Vertical) {
      contents += new Label("Select base map")
      contents += difficultyComboBox
      contents += new Label("Select Map:")
      contents += new ScrollPane(mapList)
      contents += new Label("New Map Name:")
      contents += mapNameField
      border = EmptyBorder(10, 10, 10, 10)
    }

    def refreshMapList(difficulty: String): Unit = {
      LoadGameAction.loadGamesForGameType(difficulty) { maps =>
        mapList.listData = maps
      }
    }

    refreshMapList(difficulties.head)

    difficultyComboBox.listenTo(difficultyComboBox.selection)
    difficultyComboBox.reactions += {
      case SelectionChanged(_) =>
        val selectedDifficulty = difficultyComboBox.selection.item
        refreshMapList(selectedDifficulty)
    }

    val frame = new MainFrame()
    frame.visible = true

    val result = Dialog.showConfirmation(
      frame,
      panel.peer,
      title = "Create new map",
      optionType = Dialog.Options.OkCancel
    )

    if (result == Dialog.Result.Ok) {
      val selectedDifficulty = difficultyComboBox.selection.item
      val selectedMapOption = mapList.selection.items.headOption
      val selectedMap = selectedMapOption.getOrElse("")
      val newMapName = mapNameField.text.trim
      if (selectedMap.nonEmpty && newMapName.nonEmpty) {
        LoadGameAction.loadGame(selectedDifficulty, selectedMap)((map, gameSequence, _) => {
          callback(s"$newMapName.txt", selectedDifficulty, map)
          frame.close()
        })
      } else {
        println("No map selected or new map name is empty")
      }
    }
  }
}
