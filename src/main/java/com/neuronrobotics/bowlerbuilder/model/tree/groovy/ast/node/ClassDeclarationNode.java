package com.neuronrobotics.bowlerbuilder.model.tree.groovy.ast.node;

import com.neuronrobotics.bowlerbuilder.model.tree.groovy.ast.ASTNode;
import com.neuronrobotics.bowlerbuilder.model.tree.groovy.ast.ASTNodeType;

public class ClassDeclarationNode extends ASTNode {

  private String className;
  private int lineNumber;

  public ClassDeclarationNode(String className, int lineNumber) {
    super(ASTNodeType.ClassDeclarationNode);
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
