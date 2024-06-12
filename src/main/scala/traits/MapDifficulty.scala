package traits

sealed trait MapDifficulty {
  val name: String
  val rowsRange: (Int, Int)
  val columnsRange: (Int, Int)
  val bombsRange: (Int, Int)
}

object MapDifficulty {
  case object Beginner extends MapDifficulty {
    val name = "Beginner"
    val rowsRange: (Int, Int) = (2, 10)
    val columnsRange: (Int, Int) = (2, 10)
    val bombsRange: (Int, Int) = (2, 5)
  }

  case object Intermediate extends MapDifficulty {
    val name = "Intermediate"
    val rowsRange: (Int, Int) = (10, 25)
    val columnsRange: (Int, Int) = (10, 25)
    val bombsRange: (Int, Int) = (12, 30)
  }

  case object Hard extends MapDifficulty {
    val name = "Hard"
    val rowsRange: (Int, Int) = (25, 50)
    val columnsRange: (Int, Int) = (25, 50)
    val bombsRange: (Int, Int) = (50, 99)
  }

  def fromName(name: String): MapDifficulty = {
    name.toLowerCase match {
      case "beginner" => Beginner
      case "intermediate" => Intermediate
      case "hard" => Hard
    }
  }

  val allDifficulties: Seq[MapDifficulty] = Seq(Beginner, Intermediate, Hard)
}
