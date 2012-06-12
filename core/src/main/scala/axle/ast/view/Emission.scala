package axle.ast.view

import axle.ast._
import scala.xml._
import scala.collection.mutable.ArrayBuffer
import axle.Loggable

object Emission extends Loggable {

  def emit(stmt: Statement, node: MetaNode, grammar: Language, formatter: MetaNodeFormatter[_, _]): Unit = {

    // println("emit(stmt = " + stmt + ", nodeOpt = " + nodeOpt + ", grammar, formatter)")

    //info("stmt: " + stmt)
    //info("node: " + node)
    //info

    (node, stmt) match {
      case (MetaNodeRule(_, m, _), Sub(name)) => {
        m.get(name).map(subtree =>
          formatter.needsParens(name, node, subtree, grammar) match {
            case true => {
              formatter.raw("(")
              emit(grammar, subtree, formatter)
              formatter.raw(")")
            }
            case false => emit(grammar, subtree, formatter)
          }
        )
      }
      case (MetaNodeRule(_, m, _), Spread()) => {
        m("spread") match {
          case MetaNodeList(l, _) => l.map({ c =>
            {
              emit(grammar, c, formatter)
              formatter.newline(false, c)
            }
          })
          case _ => throw new Exception("spread statement is applied to something other than MetaNodeList")
        }
      }

      case (_, Nop()) => Text("") // TODO is there an empty Node?

      case (MetaNodeRule(_, m, _), Attr(attr)) => m(attr).asInstanceOf[MetaNodeValue].value.map(v => formatter.name(v))

      case (_, Lit(value: String)) => formatter.raw(value)

      case (_, Sq(stmts @ _*)) => stmts.map(s => emit(s, node, grammar, formatter))

      // stmts.filter( ! Existence.exists(_, node, grammar) ).map( s => formatter.raw("") )
      case (_, SqT(stmts @ _*)) => stmts.map(Existence.exists(_, node, grammar)).foldLeft(true)({ _ && _ }) match {
        case true => stmts.map(emit(_, node, grammar, formatter))
        case false =>
      }

      case (MetaNodeRule(_, m, _), Repr(name)) => m(name).asInstanceOf[MetaNodeValue].value.map(v => formatter.repr(v)) // TODO !!! replace toString() with the equiv of repr()

      case (_, Emb(left, stmt, right)) => {
        formatter.raw(left)
        emit(stmt, node, grammar, formatter)
        formatter.raw(right)
      }

      case (_, Kw(value)) => formatter.keyword(value)

      case (_, PosKw(initial, rest)) => formatter.stack.top.map(_ match {
        case (0, _) => formatter.keyword(initial)
        case _ => formatter.keyword(rest)
      })

      case (_, Sp()) => formatter.space()

      case (_, Op(value)) => formatter.operator(value)

      case (MetaNodeRule(_, m, _), For(subtree, body)) => {
        formatter.enterFor()
        val elems = m(subtree).asInstanceOf[MetaNodeList]
        for (i <- 0 until elems.list.length) {
          val c = elems.list(i)
          formatter.updateFor("TODO c")
          emit(body, c, grammar, formatter)
        }
        formatter.leaveFor()
      }

      case (MetaNodeRule(_, m, _), ForDel(subtree, body, delimiter)) => {
        formatter.enterFor()
        val elems = m(subtree).asInstanceOf[MetaNodeList]
        for (i <- 0 until elems.list.length) {
          val c = elems.list(i)
          formatter.updateFor("TODO c")
          emit(body, c, grammar, formatter)
          if (i < elems.list.length - 1) {
            formatter.raw(delimiter)
          }
        }
        formatter.leaveFor()
      }

      case (MetaNodeRule(_, m, _), J(subtree, delimiter)) => m.get(subtree).map(st => {
        val elems = st.asInstanceOf[MetaNodeList]
        val n = elems.list.length - 1
        for (i <- 0 to n) {
          emit(grammar, elems.list(i), formatter)
          if (i < n) {
            emit(delimiter, null, grammar, formatter)
          }
        }
      })

      case (MetaNodeRule(_, m, _), JItems(subtree, inner, outer)) => {
        // TODO? python set elems = node.items
        val elems = m(subtree).asInstanceOf[MetaNodeList]
        for (i <- 0 until elems.list.length) {
          val l = elems.list(i).asInstanceOf[MetaNodeList].list
          emit(grammar, l(0), formatter)
          formatter.raw(inner)
          emit(grammar, l(1), formatter)
          if (i < elems.list.length - 1) {
            formatter.raw(outer)
          }
        }
      }

      case (MetaNodeRule(_, m, _), Affix(subtree, prefix, postfix)) =>
        m.get(subtree).map(x => {
          x.asInstanceOf[MetaNodeList].list.map(c => {
            formatter.raw(prefix)
            emit(grammar, c, formatter)
            postfix.map(formatter.raw(_))
          })
        })

      case (_, Indent()) => formatter.indent

      case (_, Dedent()) => formatter.dedent

      case (_, CR()) => formatter.newline(false, node)

      case (_, CRH()) => formatter.newline(true, node)

      case (_, Var()) => emit(grammar, node, formatter)

      case (MetaNodeList(l, _), VarN(n)) => emit(grammar, l(n), formatter)

      case (MetaNodeRule(_, m, _), Arglist()) => {
        // Note: This is far too python-specific
        val argnames = m("argnames").asInstanceOf[MetaNodeList]
        val defaults = m("defaults").asInstanceOf[MetaNodeList]
        val arity = argnames.list.length
        val num_defaults = defaults.list.length
        val num_undefaulted = arity - num_defaults
        for (i <- 0 until argnames.list.length) {
          val flags = m("flags").asInstanceOf[MetaNodeValue].value
          if ((((flags.equals("4")) && i == arity - 1) || ((flags.equals("12")) && i == arity - 2))) {
            formatter.raw("*")
          }
          if ((flags.equals("8") || flags.equals("12")) && i == arity - 1) {
            formatter.raw("**")
          }
          emit(grammar, argnames.list(i), formatter)
          if (i >= num_undefaulted) {
            formatter.raw("=")
            emit(grammar, defaults.list(i - num_undefaulted), formatter)
          }
          if (i < arity - 1) {
            formatter.raw(",")
            formatter.space()
          }
        }
      }
    }

  }

  def emit(grammar: Language, node: MetaNode, formatter: MetaNodeFormatter[_, _]): Unit = {

    // println("emit(grammar, node = " + node + ", formatter)")

    formatter._node2lineno.update(node, node.getLineNo)

    node match {

      case MetaNodeValue(v, _) => v.map(formatter.raw(_))

      case MetaNodeList(l, _) => (0 until l.length)
        .map({ i =>
          {
            emit(grammar, l(i), formatter)
            if (i < (l.length - 1)) {
              formatter.space()
            }
          }
        })

      case MetaNodeRule(r, m, lineno) => {

        formatter.conformTo(node)

        if (formatter.shouldHighlight(node)) {
          formatter.beginSpan()
        }

        emit(grammar.name2rule(r).statement, node, grammar, formatter)

        if (formatter.shouldHighlight(node)) {
          formatter.endSpan("highlight")
        }

      }
    }
  }

}