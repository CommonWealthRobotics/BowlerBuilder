package com.neuronrobotics.bowlerbuilder.model.tree.groovy.ast;

public class DoWhileNode extends ASTNode {

  private ASTNode loopBlockNode;

  public DoWhileNode(ASTNode loopBlockNode) {
    this.loopBlockNode = loopBlockNode;
  }

  public ASTNode getLoopBlockNode() {
    return loopBlockNode;
  }

}
