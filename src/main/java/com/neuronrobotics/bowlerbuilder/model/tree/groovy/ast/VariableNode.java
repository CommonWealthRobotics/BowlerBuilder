package com.neuronrobotics.bowlerbuilder.model.tree.groovy.ast;

public class VariableNode extends ASTNode {

  private ASTNode typeNode;
  private ASTNode nameNode;

  public VariableNode(ASTNode typeNode, ASTNode nameNode) {
    super();
    this.typeNode = typeNode;
    this.nameNode = nameNode;
  }

  public ASTNode getTypeNode() {
    return typeNode;
  }

  public ASTNode getNameNode() {
    return nameNode;
  }

}
