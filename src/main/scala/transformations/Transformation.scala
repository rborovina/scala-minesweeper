package transformations

case class Transformation[T](trans: T => T, inv: T => T) {
  def andThen(next: Transformation[T]): Transformation[T] = Transformation(map => next.trans(trans(map)), map => inv(next.inv(map)))

  def apply(map: T): T = trans(map)

  def inverse: Transformation[T] = Transformation(inv, trans)
}
