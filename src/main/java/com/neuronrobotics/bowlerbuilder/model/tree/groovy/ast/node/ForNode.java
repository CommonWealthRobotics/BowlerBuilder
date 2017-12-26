package com.neuronrobotics.bowlerbuilder.model.tree.groovy.ast.node;

import com.neuronrobotics.bowlerbuilder.model.tree.groovy.ast.ASTNode;
import com.neuronrobotics.bowlerbuilder.model.tree.groovy.ast.ASTNodeType;

public class ForNode extends ASTNode {

  private final ASTNode collectionNode;
  private final ASTNode loopBlockNode;

  public ForNode(ASTNode collectionNode, ASTNode loopBlockNode) {
    super(ASTNodeType.ForNode);
    this.collectionNode = collectionNode;
    this.loopBlockNode = loopBlockNode;
  }

  public ASTNode getCollectionNode() {
    return collectionNode;
  }

  public ASTNode getLoopBlockNode() {
    return loopBlockNode;
  }

}
