package com.neuronrobotics.bowlerbuilder.model.tree.groovy.ast.node;

import com.neuronrobotics.bowlerbuilder.model.tree.groovy.ast.ASTNode;
import com.neuronrobotics.bowlerbuilder.model.tree.groovy.ast.ASTNodeType;
import java.util.List;

public class TryCatchFinallyNode extends ASTNode {

  private final ASTNode tryNode;
  private final List<ASTNode> catchNodes;
  private final ASTNode finallyNode;

  public TryCatchFinallyNode(ASTNode tryNode, List<ASTNode> catchNodes, ASTNode finallyNode) {
    super(ASTNodeType.TryCatchFinallyNode);
    this.tryNode = tryNode;
    this.catchNodes = catchNodes;
    this.finallyNode = finallyNode;
  }

  public ASTNode getTryNode() {
    return tryNode;
  }

  public List<ASTNode> getCatchNodes() {
    return catchNodes;
  }

  public ASTNode getFinallyNode() {
    return finallyNode;
  }

}
