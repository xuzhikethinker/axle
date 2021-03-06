package axle.game.ttt

import axle.algebra._

class AITicTacToePlayer(aitttPlayerId: String, aitttDescription: String = "minimax")(implicit ttt: TicTacToe)
  extends TicTacToePlayer(aitttPlayerId, aitttDescription) {

  val heuristic = (state: TicTacToeState) => ttt.players.map(p => {
    (p, state.outcome.map(out => if (out.winner === Some(p)) 1.0 else -1.0).getOrElse(0.0))
  }).toMap

  def move(state: TicTacToeState): (TicTacToeMove, TicTacToeState) = {
    val (move, newState, values) = ttt.minimax(state, 3, heuristic)
    (move, newState)
  }
}
