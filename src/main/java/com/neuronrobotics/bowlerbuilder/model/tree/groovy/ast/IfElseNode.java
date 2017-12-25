package com.neuronrobotics.bowlerbuilder.model.tree.groovy.ast;

public class IfElseNode extends ASTNode {

  private String booleanExpression;
  private ASTNode ifNode;
  private ASTNode elseNode;

  public IfElseNode(String booleanExpression, ASTNode ifNode, ASTNode elseNode) {
    super();
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
