package com.neuronrobotics.bowlerbuilder.model.tree.groovy.ast.node;

import com.neuronrobotics.bowlerbuilder.model.tree.groovy.ast.ASTNode;
import com.neuronrobotics.bowlerbuilder.model.tree.groovy.ast.ASTNodeType;
import java.util.List;

public class SwitchNode extends ASTNode {

  private final ASTNode expressionNode;
  private final List<ASTNode> caseNodes;
  private final ASTNode defaultNode;

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
