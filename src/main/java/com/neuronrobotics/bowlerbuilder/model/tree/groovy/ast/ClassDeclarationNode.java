package com.neuronrobotics.bowlerbuilder.model.tree.groovy.ast;

public class ClassDeclarationNode extends ASTNode {

  private String className;
  private int lineNumber;

  public ClassDeclarationNode(String className, int lineNumber) {
    super();
    this.className = className;
    this.lineNumber = lineNumber;
  }

  public String getClassName() {
    return className;
  }

  public int getLineNumber() {
    return lineNumber;
  }
}
