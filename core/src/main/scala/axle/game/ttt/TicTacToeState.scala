
package axle.game.ttt

import axle.game._

import axle.matrix.ArrayMatrixFactory._

case class TicTacToeState(player: Player[TicTacToe], board: ArrayMatrix[Option[String]])
  extends State[TicTacToe] {

  val boardSize = board.columns
  val numPositions = board.length

  override def toString(): String = {

    val keyWidth = numPositions.toString().length

    "Board:         Movement Key:\n" +
      0.until(boardSize).map(r => {
        board.row(r).toList.map(_.getOrElse(" ")).mkString("|") +
          "          " +
          (1 + r * boardSize).to(1 + (r + 1) * boardSize).mkString("|") // TODO rjust(keyWidth)
      }).mkString("\n")

  }

  def positionToRow(position: Int) = (position - 1) / boardSize

  def positionToColumn(position: Int) = (position - 1) % boardSize

  def getBoardAt(position: Int) = board(positionToRow(position), positionToColumn(position))

  // The validation in InteractiveTicTacToePlayer.chooseMove might be better placed here
  def setBoardAt(position: Int, player: Player[TicTacToe]) =
    board(positionToRow(position), positionToColumn(position)) = Some(player.id)

  def hasWonRow(player: Player[TicTacToe]) = 0.until(boardSize).exists(board.row(_).toList.forall(_ == player.id))

  def hasWonColumn(player: Player[TicTacToe]) = 0.until(boardSize).exists(board.column(_).toList.forall(_ == player.id))

  def hasWonDiagonal(player: Player[TicTacToe]) = {
    val indexes = 0 until boardSize
    indexes.forall(i => board(i, i) == player.id) || indexes.forall(i => board(i, (boardSize - 1) - i) == player.id)
  }

  def hasWon(player: Player[TicTacToe]) = hasWonRow(player) || hasWonColumn(player) || hasWonDiagonal(player)

  def openPositions() = 1.to(numPositions).filter(getBoardAt(_).isEmpty)

  def getOutcome(): Option[Outcome[TicTacToe]] = {
    for (player <- game.players.values) {
      val tttp = player.asInstanceOf[Player[TicTacToe]] // TODO remove cast
      if (hasWon(tttp)) {
        return Some(Outcome[TicTacToe](game, Some(tttp)))
      }
    }

    if (openPositions().length == 0) {
      return Some(Outcome[TicTacToe](game, None))
    }

    None
  }

  def applyMove(move: TicTacToeMove): TicTacToeState = {
    val resultBoard = board.dup
    resultBoard(positionToRow(move.position), positionToColumn(move.position)) = Some(player.id)
    TicTacToeState(game.playerAfter(move.player), resultBoard)
  }

}
