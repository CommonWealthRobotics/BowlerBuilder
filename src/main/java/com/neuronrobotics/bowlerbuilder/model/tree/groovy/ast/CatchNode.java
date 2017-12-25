package com.neuronrobotics.bowlerbuilder.model.tree.groovy.ast;

public class CatchNode extends ASTNode {

  private ASTNode statementNode;

  public CatchNode(ASTNode statementNode) {
    super();
    this.statementNode = statementNode;
  }

  public ASTNode getStatementNode() {
    return statementNode;
  }

}
