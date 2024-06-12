import traits.ScreenManager
import view.{MainMenuScreen}

import scala.swing.{Frame, SimpleSwingApplication}

object App extends SimpleSwingApplication with ScreenManager{
  private var currentScreen: Option[Frame] = None

  def top = new MainMenuScreen(this)

  override def switchScreen(newScreen: Frame): Unit = {
    currentScreen.foreach(_.close())
    currentScreen = Some(newScreen)
    newScreen.visible = true
  }
}
