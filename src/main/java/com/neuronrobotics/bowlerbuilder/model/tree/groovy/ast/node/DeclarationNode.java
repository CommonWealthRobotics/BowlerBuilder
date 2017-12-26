package com.neuronrobotics.bowlerbuilder.model.tree.groovy.ast.node;

import com.neuronrobotics.bowlerbuilder.model.tree.groovy.ast.ASTNode;
import com.neuronrobotics.bowlerbuilder.model.tree.groovy.ast.ASTNodeType;

public class DeclarationNode extends ASTNode {

  private final ASTNode leftExpression;
  private final ASTNode rightExpression;

  public DeclarationNode(ASTNode leftExpression, ASTNode rightExpression) {
    super(ASTNodeType.DeclarationNode);
    this.leftExpression = leftExpression;
    this.rightExpression = rightExpression;
  }

  public ASTNode getLeftExpression() {
    return leftExpression;
  }

  public ASTNode getRightExpression() {
    return rightExpression;
  }

}
