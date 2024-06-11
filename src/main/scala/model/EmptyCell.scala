package model

case class EmptyCell(neighboringBombs: Int = 0, isRevealed: Boolean = false, isFlagged: Boolean = false) extends Cell {
  override def reveal(): Cell = this.copy(isRevealed = true)

  override def flag(): Cell = this.copy(isFlagged = true)

  override def unflag(): Cell = this.copy(isFlagged = false)

  override def toggleFlag(): Cell = if (isFlagged) unflag() else flag()

  def setNeighboringBombs(neighboringBombs: Int): EmptyCell = this.copy(neighboringBombs = neighboringBombs)
}