package com.neuronrobotics.bowlerbuilder.model.tree.groovy.ast.node;

import com.neuronrobotics.bowlerbuilder.model.tree.groovy.ast.ASTNode;
import com.neuronrobotics.bowlerbuilder.model.tree.groovy.ast.ASTNodeType;
import java.util.List;

public class BlockNode extends ASTNode {

  private List<ASTNode> nodes;

  public BlockNode(List<ASTNode> nodes) {
    super(ASTNodeType.BlockNode);
    this.nodes = nodes;
  }

  public List<ASTNode> getNodes() {
    return nodes;
  }

}
