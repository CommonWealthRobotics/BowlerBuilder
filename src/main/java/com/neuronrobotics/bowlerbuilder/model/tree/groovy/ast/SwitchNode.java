package com.neuronrobotics.bowlerbuilder.model.tree.groovy.ast;

import java.util.List;

public class SwitchNode extends ASTNode {

  private ASTNode expressionNode;
  private List<ASTNode> caseNodes;
  private ASTNode defaultNode;

  public SwitchNode(ASTNode expressionNode, List<ASTNode> caseNodes, ASTNode defaultNode) {
    super(ASTNodeType.SwitchNode);
    this.expressionNode = expressionNode;
    this.caseNodes = caseNodes;
    this.defaultNode = defaultNode;
  }

  public ASTNode getExpressionNode() {
    return expressionNode;
  }

  public List<ASTNode> getCaseNodes() {
    return caseNodes;
  }

  public ASTNode getDefaultNode() {
    return defaultNode;
  }

}
