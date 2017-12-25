package com.neuronrobotics.bowlerbuilder.model.tree.groovy.ast;

public class DoWhileNode extends ASTNode {

  private String loopBlock;

  public DoWhileNode(String loopBlock) {
    super();
    this.loopBlock = loopBlock;
  }

  public String getLoopBlock() {
    return loopBlock;
  }

}
