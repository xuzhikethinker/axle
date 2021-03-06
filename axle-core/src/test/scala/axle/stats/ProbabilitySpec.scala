package axle.stats

import org.specs2.mutable._

import axle.stats._
import axle.game.Dice._

class ProbabilitySpec extends Specification {

  "coins" should {
    "work" in {

      val coin1 = coin()
      val coin2 = coin()
      P(coin1 is 'HEAD)() must be equalTo 0.5
      P((coin1 is 'HEAD) and (coin2 is 'HEAD))() must be equalTo 0.25
      P((coin1 is 'HEAD) or (coin2 is 'HEAD))() must be equalTo 0.75
    }
  }

  "dice" should {
    "work" in {

      val d6a = die(6)
      val d6b = die(6)
      P(d6a is 1)() must be equalTo 0.16666666666666666
      P((d6a is 1) and (d6b is 2))() must be equalTo 0.027777777777777776
      P((d6a isnt 3))() must be equalTo 0.8333333333333334
    }
  }

}
