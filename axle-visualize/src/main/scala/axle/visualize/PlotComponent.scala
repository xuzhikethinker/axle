package axle.visualize

import javax.swing.JPanel
import java.awt.{ Color, Font, FontMetrics, Graphics, Graphics2D, Dimension }
import scala.concurrent.duration._
import axle.quanta._
import axle.actor.Defaults._
import Angle._
import Color._
import collection._
import Stream.continually
import akka.pattern.ask
import akka.actor.{ Props, Actor, ActorRef, ActorSystem, ActorLogging }
import akka.util.Timeout
import collection.immutable.TreeMap
import scala.concurrent.duration._
import java.awt.Frame
import scala.concurrent.Await
import axle.visualize.element._
import axle.algebra.Plottable

class PlotView[X: Plottable, Y: Plottable](plot: Plot[X, Y], data: Seq[(String, SortedMap[X, Y])], normalFont: Font) {

  import plot._

  val colorStream = continually(colors.toStream).flatten

  val xPlottable = implicitly[Plottable[X]]
  val yPlottable = implicitly[Plottable[Y]]

  val keyOpt = if (drawKey)
    Some(new Key(plot, normalFont, colorStream, keyWidth, keyTopPadding, data))
  else
    None

  val minX = List(yAxis, data.map(_._2.firstKey).min(xPlottable)).min(xPlottable)
  val maxX = List(yAxis, data.map(_._2.lastKey).max(xPlottable)).max(xPlottable)
  val minY = List(xAxis, data.map(lf => (lf._2.values ++ List(yPlottable.zero())).filter(yPlottable.isPlottable(_)).min(yPlottable)).min(yPlottable)).min(yPlottable)
  val maxY = List(xAxis, data.map(lf => (lf._2.values ++ List(yPlottable.zero())).filter(yPlottable.isPlottable(_)).max(yPlottable)).max(yPlottable)).max(yPlottable)
  val minPoint = Point2D(minX, minY)
  val maxPoint = Point2D(maxX, maxY)

  val scaledArea = new ScaledArea2D(
    width = if (drawKey) width - (keyWidth + keyLeftPadding) else width,
    height, border,
    minPoint.x, maxPoint.x, minPoint.y, maxPoint.y
  )

  val vLine = new VerticalLine(scaledArea, yAxis, black)
  val hLine = new HorizontalLine(scaledArea, xAxis, black)
  val xTics = new XTics(scaledArea, xPlottable.tics(minX, maxX), normalFont, true, 0 *: °, black)
  val yTics = new YTics(scaledArea, yPlottable.tics(minY, maxY), normalFont, black)

  val dataLines = new DataLines(scaledArea, data, colorStream, pointDiameter, connect)

}

class PlotComponent[X: Plottable, Y: Plottable](plot: Plot[X, Y]) extends JPanel with Fed {

  import plot._

  setMinimumSize(new Dimension(width, height))

  def feeder() = dataFeedActor
  
  val normalFont = new Font(fontName, Font.BOLD, fontSize)
  val xAxisLabelText = xAxisLabel.map(new Text(_, normalFont, width / 2, height - border / 2))
  val yAxisLabelText = yAxisLabel.map(new Text(_, normalFont, 20, height / 2, angle = Some(90 *: °)))
  val titleFont = new Font(titleFontName, Font.BOLD, titleFontSize)
  val titleText = title.map(new Text(_, titleFont, width / 2, titleFontSize))

  override def paintComponent(g: Graphics): Unit = {

    import DataFeedProtocol._

    val g2d = g.asInstanceOf[Graphics2D]

    val dataFuture = (dataFeedActor ? Fetch()).mapTo[List[(String, TreeMap[X, Y])]]

    // Getting rid of this Await is awaiting a better approach to integrating AWT and Akka
    val data = Await.result(dataFuture, 1.seconds)

    val view = new PlotView(plot, data, normalFont)

    import view._

    val paintables =
      Vector(vLine, hLine, xTics, yTics, dataLines) ++
        Vector(titleText, xAxisLabelText, yAxisLabelText, view.keyOpt).flatMap(i => i)

    for (paintable <- paintables) {
      paintable.paint(g2d)
    }

  }

}
