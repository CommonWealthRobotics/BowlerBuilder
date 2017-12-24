package com.neuronrobotics.bowlerbuilder.model.tree.groovy.ast;

public class MethodNode extends ASTNode {

  private String methodName;
  private int lineNumber;

  public MethodNode(String methodName, int lineNumber) {
    super();
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
