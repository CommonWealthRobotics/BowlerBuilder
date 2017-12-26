package com.neuronrobotics.bowlerbuilder.model.tree.groovy.ast.node;

import com.neuronrobotics.bowlerbuilder.model.tree.groovy.ast.ASTNode;
import com.neuronrobotics.bowlerbuilder.model.tree.groovy.ast.ASTNodeType;

public class CatchNode extends ASTNode {

  private final ASTNode statementNode;

  public CatchNode(ASTNode statementNode) {
    super(ASTNodeType.CatchNode);
    this.statementNode = statementNode;
  }

  public ASTNode getStatementNode() {
    return statementNode;
  }

}
