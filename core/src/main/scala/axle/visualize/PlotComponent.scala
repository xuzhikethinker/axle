package axle.visualize

import javax.swing.JPanel
import java.awt.Color
import java.awt.Font
import java.awt.FontMetrics
import java.awt.Graphics
import java.awt.Graphics2D

class PlotComponent[X, DX, Y, DY](plot: Plot[X, DX, Y, DY]) extends JPanel {

  import plot._

  val colors = List(Color.blue, Color.red, Color.green, Color.orange, Color.pink, Color.yellow)

  val colorStream = Stream.continually(colors.toStream).flatten

  val scaledArea = new ScaledArea2D(width = width - 100, height, border, minX, maxX, minY, maxY)(getXPlottable(), getYPlottable())

  val normalFont = new Font("Courier New", Font.BOLD, 12)
  val titleFont = new Font("Palatino", Font.BOLD, 20)

  def labels(g2d: Graphics2D, fontMetrics: FontMetrics): Unit = {

    title.map(text => {
      g2d.setFont(titleFont)
      g2d.drawString(text, (width - fontMetrics.stringWidth(text)) / 2, 20)
    })

    g2d.setFont(normalFont)

    xAxisLabel.map(text =>
      g2d.drawString(text, (width - fontMetrics.stringWidth(text)) / 2, height + (fontMetrics.getHeight - border) / 2)
    )

    yAxisLabel.map(text => {
      val tx = 20
      val ty = (height + fontMetrics.stringWidth(text)) / 2
      g2d.translate(tx, ty)
      g2d.rotate(clockwise90)
      g2d.drawString(text, 0, 0)
      g2d.rotate(counterClockwise90)
      g2d.translate(-tx, -ty)
    })

  }

  def key(g2d: Graphics2D): Unit = {
    val lineHeight = g2d.getFontMetrics.getHeight
    for ((((label, f), color), i) <- lfs.zip(colorStream).zipWithIndex) {
      g2d.setColor(color)
      g2d.drawString(label, 620, 50 + lineHeight * i) // TODO embed position
    }
  }

  def drawXTics(g2d: Graphics2D, fontMetrics: FontMetrics): Unit = xTics.map({
    case (x, label) => {
      val bottomScaled = Point2D(x, minY)
      val bottomUnscaled = scaledArea.framePoint(bottomScaled)
      g2d.setColor(Color.lightGray)
      scaledArea.drawLine(g2d, Point2D(x, minY), Point2D(x, maxY))
      g2d.setColor(Color.black)
      // TODO: angle xtics?
      g2d.drawString(label, bottomUnscaled.x - fontMetrics.stringWidth(label) / 2, bottomUnscaled.y + fontMetrics.getHeight)
      g2d.drawLine(bottomUnscaled.x, bottomUnscaled.y - 2, bottomUnscaled.x, bottomUnscaled.y + 2)
    }
  })

  def drawYTics(g2d: Graphics2D, fontMetrics: FontMetrics): Unit = yTics.map({
    case (y, label) => {
      val leftScaled = Point2D(minX, y)
      val leftUnscaled = scaledArea.framePoint(leftScaled)
      g2d.setColor(Color.lightGray)
      scaledArea.drawLine(g2d, leftScaled, Point2D(maxX, y))
      g2d.setColor(Color.black)
      g2d.drawString(label, leftUnscaled.x - fontMetrics.stringWidth(label) - 5, leftUnscaled.y + fontMetrics.getHeight / 2)
      g2d.drawLine(leftUnscaled.x - 2, leftUnscaled.y, leftUnscaled.x + 2, leftUnscaled.y)
    }
  })

  override def paintComponent(g: Graphics): Unit = {

    val g2d = g.asInstanceOf[Graphics2D]
    val fontMetrics = g2d.getFontMetrics

    g2d.setColor(Color.black)
    labels(g2d, fontMetrics)
    scaledArea.verticalLine(g2d, yAxis)
    scaledArea.horizontalLine(g2d, xAxis)

    drawXTics(g2d, fontMetrics)
    drawYTics(g2d, fontMetrics)

    for (((label, f), color) <- lfs.zip(colorStream)) {
      g2d.setColor(color)
      if (connect) {
        val xsStream = f.keysIterator.toStream
        for ((x0, x1) <- xsStream.zip(xsStream.tail)) {
          scaledArea.drawLine(g2d, Point2D(x0, f(x0)), Point2D(x1, f(x1)))
        }
      }
      for (x <- f.keys) {
        scaledArea.fillOval(g2d, Point2D(x, f(x)), pointDiameter, pointDiameter)
      }
    }

    if (drawKey) {
      key(g2d)
    }
  }

}
