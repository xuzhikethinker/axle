package axle.quanta

import spire.math._
import axle.graph._

class Energy extends Quantum {

  class EnergyQuantity(
    magnitude: Number = one,
    _unit: Option[Q] = None,
    _name: Option[String] = None,
    _symbol: Option[String] = None,
    _link: Option[String] = None) extends Quantity(magnitude, _unit, _name, _symbol, _link)

  type Q = EnergyQuantity

  def newUnitOfMeasurement(
    name: Option[String] = None,
    symbol: Option[String] = None,
    link: Option[String] = None): EnergyQuantity =
    new EnergyQuantity(one, None, name, symbol, link)

  def newQuantity(magnitude: Number, unit: EnergyQuantity): EnergyQuantity =
    new EnergyQuantity(magnitude, Some(unit), None, None, None)

  def conversionGraph() = _conversionGraph

  import Power.{ kilowatt }
  import Time.{ hour }

  val wikipediaUrl = "http://en.wikipedia.org/wiki/Energy"

  lazy val _conversionGraph = conversions(
    List(
      derive(kilowatt.by[Time.type, this.type](hour, this)),
      unit("joule", "J"),
      unit("kilojoule", "KJ"),
      unit("megajoule", "MJ"),
      unit("ton TNT", "T", Some("http://en.wikipedia.org/wiki/TNT_equivalent")),
      unit("kiloton", "KT"),
      unit("megaton", "MT"),
      unit("gigaton", "GT")
    ),
    (vs: Seq[Vertex[EnergyQuantity]]) => vs match {
      case kwh :: j :: kj :: mj :: t :: kt :: mt :: gt :: Nil => trips2fns(List(
        (mj, t, 4.184),
        (j, kj, 1E3),
        (j, mj, 1E6),
        (t, kt, 1E3),
        (t, mt, 1E6),
        (t, gt, 1E9)
      ))
      case _ => Nil
    }
  )

  lazy val kwh = byName("kwh")
  lazy val joule = byName("joule")
  lazy val kilojoule = byName("kilojoule")
  lazy val megajoule = byName("megajoule")
  lazy val tonTNT = byName("ton TNT")
  lazy val kiloton = byName("kiloton")
  lazy val megaton = byName("megaton")
  lazy val gigaton = byName("gigaton")

  lazy val castleBravo = 15 *: megaton // Some("Castle Bravo Thermonuclear Bomb"), None, Some("http://en.wikipedia.org/wiki/Castle_Bravo"))

}

object Energy extends Energy()
