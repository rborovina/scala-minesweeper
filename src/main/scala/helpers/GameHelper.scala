package helpers

import java.text.SimpleDateFormat
import java.util.Date

object GameHelper {
  def generateUniqueFilename(): String = {
    val timestamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date())
    s"game_$timestamp.txt"
  }
}
