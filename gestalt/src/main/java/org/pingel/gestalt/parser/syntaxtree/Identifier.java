//
// Generated by JTB 1.2.2
//

package org.pingel.gestalt.parser.syntaxtree;

/**
 * Grammar production:
 * f0 -> <IDENTIFIER>
 */
public class Identifier implements Node {
   public NodeToken f0;

   public Identifier(NodeToken n0) {
      f0 = n0;
   }

   public void accept(org.pingel.gestalt.parser.visitor.Visitor v) {
      v.visit(this);
   }
   public Object accept(org.pingel.gestalt.parser.visitor.ObjectVisitor v, Object argu) {
      return v.visit(this,argu);
   }
}
