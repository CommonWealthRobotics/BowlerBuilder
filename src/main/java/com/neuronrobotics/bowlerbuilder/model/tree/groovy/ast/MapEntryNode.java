package com.neuronrobotics.bowlerbuilder.model.tree.groovy.ast;

public class MapEntryNode extends ASTNode {

  private ASTNode keyNode;
  private ASTNode entryNode;

  public MapEntryNode(ASTNode keyNode, ASTNode entryNode) {
    super();
    this.keyNode = keyNode;
    this.entryNode = entryNode;
  }

  public ASTNode getKeyNode() {
    return keyNode;
  }

  public ASTNode getEntryNode() {
    return entryNode;
  }

}
