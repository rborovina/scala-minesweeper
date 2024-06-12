package transformations

case class Transformation[T](trans: T => T) {
  def andThen(next: Transformation[T]): Transformation[T] = Transformation(map => next.trans(trans(map)))

  def apply(map: T): T = trans(map)
}
