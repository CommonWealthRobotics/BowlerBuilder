package com.neuronrobotics.bowlerbuilder.model.tree.groovy.ast;

public class CaseNode extends ASTNode {

  private ASTNode statementNode;

  public CaseNode(ASTNode statementNode) {
    super();
    this.statementNode = statementNode;
  }

  public ASTNode getStatementNode() {
    return statementNode;
  }

}
