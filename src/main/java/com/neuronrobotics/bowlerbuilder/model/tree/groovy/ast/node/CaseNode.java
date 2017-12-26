package com.neuronrobotics.bowlerbuilder.model.tree.groovy.ast.node;

import com.neuronrobotics.bowlerbuilder.model.tree.groovy.ast.ASTNode;
import com.neuronrobotics.bowlerbuilder.model.tree.groovy.ast.ASTNodeType;

public class CaseNode extends ASTNode {

  private final ASTNode statementNode;

  public CaseNode(ASTNode statementNode) {
    super(ASTNodeType.CaseNode);
    this.statementNode = statementNode;
  }

  public ASTNode getStatementNode() {
    return statementNode;
  }

}
