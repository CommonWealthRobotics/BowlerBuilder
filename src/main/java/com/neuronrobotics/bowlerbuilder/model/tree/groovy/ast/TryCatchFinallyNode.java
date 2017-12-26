package com.neuronrobotics.bowlerbuilder.model.tree.groovy.ast;

import java.util.List;

public class TryCatchFinallyNode extends ASTNode {

  private ASTNode tryNode;
  private List<ASTNode> catchNodes;
  private ASTNode finallyNode;

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
