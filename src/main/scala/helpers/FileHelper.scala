package helpers

import actions.UserAction
import types.{GameMap, GameSequence}

import java.io.{BufferedWriter, FileWriter}
import scala.io.Source
import scala.util.{Try, Using}

object FileHelper {

  private def readFileLines(filePath: String): Array[String] = {
    Using(Source.fromFile(filePath)) { fileSource =>
      fileSource.getLines().toArray
    }.get
  }

  private def loadMap(lines: Array[String]): GameMap = {
    lines.map(_.toCharArray)
  }

  private def loadGameActions(lines: Array[String]): GameSequence = {
    lines.map(line => {
      val parts = line.split(",")
      val action = parts(0) match {
        case "L" => UserAction.LeftClick
        case "R" => UserAction.RightClick
        case "H" => UserAction.Hint
      }
      (action, parts(1).toInt, parts(2).toInt)
    })
  }

  def saveFile(filePath: String)(map: GameMap, gameSequence: GameSequence, elapsedTime: Int): Try[Unit] = {
    Using(new BufferedWriter(new FileWriter(filePath))) { writer =>
      map.foreach(row => writer.write(row.mkString("") + "\n"))
      gameSequence.foreach { case (action, row, col) =>
        val actionString = action match {
          case UserAction.LeftClick => "L"
          case UserAction.RightClick => "R"
          case UserAction.Hint => "H"
        }
        writer.write(s"$actionString,$row,$col\n")
      }
      writer.write(s"Time,$elapsedTime\n")
    }
  }

  def loadFile(filePath: String): (GameMap, GameSequence, Int) = {
    val lines = readFileLines(filePath)
    val mapLines = lines.takeWhile(line => !line.startsWith("L") && !line.startsWith("R") && !line.startsWith("H") && !line.startsWith("Time"))
    val map = loadMap(mapLines)
    val actionLines = lines.drop(mapLines.length).takeWhile(!_.startsWith("Time"))
    val gameSequence: GameSequence = loadGameActions(actionLines)
    val timeLine = lines.find(_.startsWith("Time")).getOrElse("Time,0")
    val elapsedTime = timeLine.split(",")(1).toInt

    (map, gameSequence, elapsedTime)
  }

  def logCompletedGame(gameName: String, bombs: Int, hints: Int, rows: Int, cols: Int, time: Int): Try[Unit] = {
    Using(new BufferedWriter(new FileWriter("completed_games_log.txt", true))) { writer =>
      writer.write(s"Game Name: $gameName, Bombs: $bombs, Hints: $hints, Rows: $rows, Cols: $cols, Time: $time\n")
    }
  }
}
