package traits

import scala.swing._

trait ScreenManager {
  def switchScreen(newScreen: Frame): Unit
}