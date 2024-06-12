package view

import scala.swing.Swing.EmptyBorder
import scala.swing.{Dimension, GridPanel, Label, MainFrame, ScrollPane}

class ScoresScreen(games: List[Map[String, String]]) extends MainFrame {
  title = "Best Scores"
  resizable = true

  private val sortedGames = games.sortBy(game => (
    -game.getOrElse("Score", "0").toInt,
    game.getOrElse("Time", "0").toInt,
    game.getOrElse("Bombs", "0").toInt
  ))

  private val columnHeaders = Seq("Game Name", "Score", "Bombs", "Hints", "Rows", "Cols", "Time")

  private val headerPanel = new GridPanel(1, columnHeaders.length) {
    columnHeaders.foreach { header =>
      contents += new Label(header) {
        border = EmptyBorder(5, 5, 5, 5)
      }
    }
    border = EmptyBorder(10, 10, 10, 10)
  }

  private val scorePanel = new GridPanel(sortedGames.length, columnHeaders.length) {
    sortedGames.foreach { game =>
      val gameData = Seq(
        game.getOrElse("Game Name", "N/A"),
        game.getOrElse("Score", "0"),
        game.getOrElse("Bombs", "0"),
        game.getOrElse("Hints", "0"),
        game.getOrElse("Rows", "0"),
        game.getOrElse("Cols", "0"),
        formatTime(game.getOrElse("Time", "0").toInt)
      )
      gameData.foreach { data =>
        contents += new Label(data) {
          border = EmptyBorder(5, 5, 5, 5)
        }
      }
    }
    border = EmptyBorder(10, 10, 10, 10)
  }

  contents = new ScrollPane(new GridPanel(2, 1) {
    contents += headerPanel
    contents += scorePanel
  })
  size = new Dimension(600, 400)

  private def formatTime(seconds: Int): String = {
    val hours = seconds / 3600
    val minutes = (seconds % 3600) / 60
    val secs = seconds % 60
    f"$hours%02d:$minutes%02d:$secs%02d"
  }
}
