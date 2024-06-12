package actions

import helpers.FileHelper
import types.{GameMap, GameSequence}
import java.io.File
import java.nio.file.{Files, Paths}
import scala.util.Try

object LoadGameAction {

  private val mapsPath = Paths.get("maps").toAbsolutePath.toString

  def loadGamesForGameType(gameType: String)(callback: Seq[String] => Unit): Unit = {
    val mapsFolder = Paths.get(mapsPath, gameType)
    val mapFiles = Files.list(mapsFolder)
      .toArray
      .map(_.toString)
      .map(file => file.substring(file.lastIndexOf(File.separator) + 1))
      .map(_.stripSuffix(".txt"))
    callback(mapFiles)
  }

  def loadGame(folder: String, gameName: String)(callback: (GameMap, GameSequence, Int) => Unit): Unit = {
    val mapFilePath = Paths.get(mapsPath, folder, s"$gameName.txt").toString
    val (map, gameSequence, elapsedTime) = FileHelper.loadFile(mapFilePath)
    callback(map, gameSequence, elapsedTime)
  }

  private def saveFile(fileType: String)(gameId: String, map: GameMap, gameSequence: GameSequence, elapsedTime: Int)(callback: => Unit): Try[Unit] = {
    Try {
      val gamePath = Paths.get(mapsPath, fileType, s"$gameId").toString
      FileHelper.saveFile(gamePath)(map, gameSequence, elapsedTime)
      callback
    }
  }

  def saveGame: (String, GameMap, GameSequence, Int) => Unit => Try[Unit] = saveFile("Games")

  def saveNewMap(mapName: String, difficulty: String, map: GameMap): Unit => Try[Unit] = saveFile(difficulty)(mapName, map, Array.empty, 0)
}
