package axle.quanta

import spire.math._
import axle.graph._

class Information extends Quantum {

  class InformationQuantity(
    magnitude: Number = one,
    _unit: Option[Q] = None,
    _name: Option[String] = None,
    _symbol: Option[String] = None,
    _link: Option[String] = None) extends Quantity(magnitude, _unit, _name, _symbol, _link)

  type Q = InformationQuantity

  def newUnitOfMeasurement(
    name: Option[String] = None,
    symbol: Option[String] = None,
    link: Option[String] = None): InformationQuantity =
    new InformationQuantity(one, None, name, symbol, link)

  def newQuantity(magnitude: Number, unit: InformationQuantity): InformationQuantity =
    new InformationQuantity(magnitude, Some(unit), None, None, None)

  def conversionGraph() = _conversionGraph

  lazy val _conversionGraph = conversions(
    List(
      unit("bit", "b"),
      unit("nibble", "nibble"),
      unit("byte", "B", Some("http://en.wikipedia.org/wiki/Byte")),
      unit("kilobyte", "KB"),
      unit("megabyte", "MB"),
      unit("gigabyte", "GB"),
      unit("terabyte", "TB"),
      unit("petabyte", "PB")
    ),
    (vs: Seq[Vertex[InformationQuantity]]) => vs match {
      case bit :: nibble :: byte :: kilobyte :: megabyte :: gigabyte :: terabyte :: petabyte :: Nil => trips2fns(List(
        (bit, nibble, 4),
        (bit, byte, 8),
        (byte, kilobyte, 1024),
        (kilobyte, megabyte, 1024),
        (megabyte, gigabyte, 1024),
        (gigabyte, terabyte, 1024),
        (terabyte, petabyte, 1024)
      ))
      case _ => Nil
    }
  )

  val wikipediaUrl = "http://en.wikipedia.org/wiki/Information"

  lazy val bit = byName("bit")
  lazy val nibble = byName("nibble")
  lazy val byte = byName("byte")
  lazy val kilobyte = byName("kilobyte")
  lazy val megabyte = byName("megabyte")
  lazy val gigabyte = byName("gigabyte")
  lazy val terabyte = byName("terabyte")
  lazy val petabyte = byName("petabyte")

  lazy val KB = kilobyte
  lazy val MB = megabyte
  lazy val GB = gigabyte
  lazy val TB = terabyte
  lazy val PB = petabyte

}

object Information extends Information()
