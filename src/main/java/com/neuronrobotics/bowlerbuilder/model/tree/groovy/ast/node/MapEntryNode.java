package com.neuronrobotics.bowlerbuilder.model.tree.groovy.ast.node;

import com.neuronrobotics.bowlerbuilder.model.tree.groovy.ast.ASTNode;
import com.neuronrobotics.bowlerbuilder.model.tree.groovy.ast.ASTNodeType;

public class MapEntryNode extends ASTNode {

  private ASTNode keyNode;
  private ASTNode entryNode;

  public MapEntryNode(ASTNode keyNode, ASTNode entryNode) {
    super(ASTNodeType.MapEntryNode);
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
