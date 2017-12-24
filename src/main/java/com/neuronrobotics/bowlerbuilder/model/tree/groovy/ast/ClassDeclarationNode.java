package com.neuronrobotics.bowlerbuilder.model.tree.groovy.ast;

public class ClassDeclarationNode extends ASTNode {

  private String className;
  private int lineNumber;

  public ClassDeclarationNode(String name, String className, int lineNumber) {
    super(name);
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
