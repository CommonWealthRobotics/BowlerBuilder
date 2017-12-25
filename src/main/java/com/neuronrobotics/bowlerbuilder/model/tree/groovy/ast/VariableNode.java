package com.neuronrobotics.bowlerbuilder.model.tree.groovy.ast;

public class VariableNode extends ASTNode {

  private String type;
  private String name;

  public VariableNode(String type, String name) {
    super();
    this.type = type;
    this.name = name;
  }

  public String getType() {
    return type;
  }

  public String getName() {
    return name;
  }

}
