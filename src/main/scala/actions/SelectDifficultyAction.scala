package actions

object SelectDifficultyAction {
  private val difficultyMap = Map(
    "Beginner" -> (9, 9, 10),
    "Medium" -> (16, 16, 40),
    "Expert" -> (30, 16, 99)
  )

  def selectDifficulty(callback: () => Unit ): Unit = {
    
  }
}
