package model

trait Cell {
  def isRevealed: Boolean

  def isFlagged: Boolean

  def reveal(): Cell

  def flag(): Cell

  def unflag(): Cell

  def toggleFlag(): Cell
}