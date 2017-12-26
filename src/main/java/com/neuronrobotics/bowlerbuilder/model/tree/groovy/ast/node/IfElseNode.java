package com.neuronrobotics.bowlerbuilder.model.tree.groovy.ast.node;

import com.neuronrobotics.bowlerbuilder.model.tree.groovy.ast.ASTNode;
import com.neuronrobotics.bowlerbuilder.model.tree.groovy.ast.ASTNodeType;

public class IfElseNode extends ASTNode {

  private final String booleanExpression;
  private final ASTNode ifNode;
  private final ASTNode elseNode;

  public IfElseNode(String booleanExpression, ASTNode ifNode, ASTNode elseNode) {
    super(ASTNodeType.IfElseNode);
    this.booleanExpression = booleanExpression;
    this.ifNode = ifNode;
    this.elseNode = elseNode;
  }

  public String getBooleanExpression() {
    return booleanExpression;
  }

  public ASTNode getIfNode() {
    return ifNode;
  }

  public ASTNode getElseNode() {
    return elseNode;
  }

}
