package com.neuronrobotics.bowlerbuilder.model.tree.groovy.ast;

public class WhileNode extends ASTNode {

  private ASTNode loopBlockNode;

  public WhileNode(ASTNode loopBlockNode) {
    super();
    this.loopBlockNode = loopBlockNode;
  }

  public ASTNode getLoopBlockNode() {
    return loopBlockNode;
  }

}
