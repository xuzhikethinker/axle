package axle.game.poker

import collection._
import axle.game._
import Stream.cons

class PokerPlayerInteractive(id: String, description: String = "human")(implicit game: Poker)
  extends PokerPlayer(id, description) {

  override def introduceGame(): Unit = {
    val intro = """
Texas Hold Em Poker

Example moves:
        
  check
  raise 1.0
  call
  fold
        
"""
    println(intro)
  }

  override def displayEvents(events: List[Event[Poker]]): Unit = {
    println()
    println(events.map(_.displayTo(this)).mkString("  "))
  }

  override def endGame(state: PokerState): Unit = {
    println()
    println(state.displayTo(state.player))
    state.outcome.map(oc => println(oc))
  }

  def userInputStream(): Stream[String] = {
    print("Enter move: ")
    val command = readLine() // TODO echo characters as typed (shouldn't have to use jline for this)
    println(command)
    cons(command, userInputStream)
  }

  val moveParser = new MoveParser()

  def move(state: PokerState): (PokerMove, PokerState) = {
    // displayEvents()
    println(state.displayTo(this))
    val move = userInputStream()
      .flatMap(moveParser.parse(_)(state.player, game))
      .find(move => state(move).isDefined).get
    (move, state(move).get) // TODO .get
  }

}
