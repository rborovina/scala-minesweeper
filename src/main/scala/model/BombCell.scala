package model

case class BombCell(isRevealed: Boolean = false, isFlagged: Boolean = false) extends Cell {
  def reveal(): Cell = this.copy(isRevealed = true)

  override def flag(): Cell = this.copy(isFlagged = true)

  override def unflag(): Cell = this.copy(isFlagged = false)

  override def toggleFlag(): Cell = if (isFlagged) unflag() else flag()
}