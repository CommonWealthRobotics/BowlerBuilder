package com.neuronrobotics.bowlerbuilder.model.tree.groovy.ast;

public class WhileNode extends ASTNode {

  private String loopBlock;

  public WhileNode(String loopBlock) {
    super();
    this.loopBlock = loopBlock;
  }

  public String getLoopBlock() {
    return loopBlock;
  }

}
