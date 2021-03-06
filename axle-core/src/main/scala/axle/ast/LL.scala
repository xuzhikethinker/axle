
package axle.ast

/**
 *
 * http://en.wikipedia.org/wiki/LL_parser
 * http://www.jambe.co.nz/UNI/FirstAndFollowSets.html
 * http://www.cs.pitt.edu/~mock/cs1622/lectures/lecture10.pdf
 * see http://blog.rafaelferreira.net/2008/07/type-safe-builder-pattern-in-scala.html
 *
 * TODO test first and follow (including scribd example)
 * TODO model input buffer
 * TODO create AST
 */

import collection._
import axle.algebra._

class Symbol(_label: String) {
  def label() = _label
  //override def toString() = "'" + label + "'"
  override def toString() = _label
}

case class Terminal(_label: String) extends Symbol(_label)
case class NonTerminal(_label: String) extends Symbol(_label)

object ⊥ extends Terminal("⊥") // also known as '$'
object ε extends Symbol("ε") // TODO terminal or non-terminal?

case class LLRule(id: Int, from: NonTerminal, rhs: List[Symbol]) {
  override def toString() = from + " " + "->" + " " + rhs.mkString("", " ", "")
}

abstract class LLParserAction()
case class Shift() extends LLParserAction()
case class Reduce(rule: LLRule) extends LLParserAction()
case class ParseError(msg: String) extends LLParserAction()

case class LLParserState(
  grammar: LLLanguage,
  input: String,
  stack: List[Symbol],
  // derivation: List[LLParserAction],
  i: Int) {

  lazy val inputBufferWithMarker = input.substring(0, i) + "|" + input.substring(i, input.length)

  override def toString =
    inputBufferWithMarker + "\n" +
      stack.mkString("", " ", "")

  def inputSymbol: Terminal = grammar.terminalsByName(input(i).toString)

  def apply(action: LLParserAction): LLParserState = action match {
    case Shift() => {
      assert(stack.head === inputSymbol)
      LLParserState(grammar, input, stack.tail, i + 1)
    }
    case Reduce(rule) => {
      assert(stack.head == rule.from)
      LLParserState(grammar, input, rule.rhs ++ stack.tail, i)
    }
    case ParseError(msg) => { sys.error(this + "\nparse error: " + msg) }
  }

  def nextAction(): LLParserAction = stack.head match {

    case sts if sts === inputSymbol => Shift()

    case foo @ NonTerminal(_) =>
      if (grammar.parseTable.contains((foo, inputSymbol))) {
        Reduce(grammar.parseTable((foo, inputSymbol)))
      } else {
        ParseError("no rule")
      }

    case _ => ParseError("stack = " + stack + ", inputSymbol = " + inputSymbol + ". A non-matching non-terminal")

  }

  def finished(): Boolean = input.length === i && stack.head === ⊥

}
