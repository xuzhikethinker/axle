package org.pingel.axle.quanta

object Power extends Quantum {

  val wikipediaUrl = "http://en.wikipedia.org/wiki/Power_(physics)"

  val watt = UnitOfMeasurement(this, "watt", "w")
  val kilowatt = watt kilo
  val megawatt = watt mega
  val gigawatt = watt giga
  val milliwatt = watt milli

  val horsepower = UnitOfMeasurement(this, "horsepower", "hp")

  val unitsOfMeasurement = List(watt, kilowatt, megawatt, gigawatt, horsepower)

  val derivations = List(
    Energy / Time
  )

  val examples = List(
    Quantity(60.0, watt, Some("Light Bulb"), Some("Light Bulb")),
    Quantity(2080.0, megawatt, Some("Hoover Dam"), Some("http://en.wikipedia.org/wiki/Hoover_Dam")),
    Quantity(420.0, horsepower, Some("2012 Mustang GT"), Some("http://en.wikipedia.org/wiki/Ford_Mustang"))
  )

}