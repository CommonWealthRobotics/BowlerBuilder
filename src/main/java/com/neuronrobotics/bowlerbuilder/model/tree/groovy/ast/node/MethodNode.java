package com.neuronrobotics.bowlerbuilder.model.tree.groovy.ast.node;

import com.neuronrobotics.bowlerbuilder.model.tree.groovy.ast.ASTNode;
import com.neuronrobotics.bowlerbuilder.model.tree.groovy.ast.ASTNodeType;

public class MethodNode extends ASTNode {

  private final String methodName;
  private final int lineNumber;

  public MethodNode(String methodName, int lineNumber) {
    super(ASTNodeType.MethodNode);
    this.methodName = methodName;
    this.lineNumber = lineNumber;
  }

  public String getMethodName() {
    return methodName;
  }

  public int getLineNumber() {
    return lineNumber;
  }

}
