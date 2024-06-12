package transformations

import types.GameMap

case class Transformation(trans: GameMap => GameMap) {
  def andThen(next: Transformation): Transformation = Transformation(map => next.trans(trans(map)))

  def apply(map: GameMap): GameMap = trans(map)
}
