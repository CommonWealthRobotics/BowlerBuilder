package com.neuronrobotics.bowlerbuilder.model.tree.groovy.ast;

import java.util.List;

public class BlockNode extends ASTNode {

  private List<ASTNode> nodes;

  public BlockNode(List<ASTNode> nodes) {
    super();
    this.nodes = nodes;
  }

  public List<ASTNode> getNodes() {
    return nodes;
  }

}
