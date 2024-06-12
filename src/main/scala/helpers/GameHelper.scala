package helpers

import java.text.SimpleDateFormat
import java.util.Date

object GameHelper {
  def generateUniqueFilename(): String = {
    val timestamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date())
    s"game_$timestamp.txt"
  }

  def parseAndSortGames(games: List[Map[String, String]]): List[Map[String, String]] = {
    games.sortBy(game => (
      -game("Score").toInt,
      game("Time").toInt,
      game("Bombs").toInt
    ))
  }

  def calculateScore(bombs: Int, userActions: Int, hints: Int, elapsedTime: Int): Int = {
    val baseScore = bombs * 10
    val hintPenalty = hints * 5
    val timePenalty = elapsedTime / 60
    val actionPenalty = userActions * 2

    baseScore - hintPenalty - timePenalty - actionPenalty
  }
}
