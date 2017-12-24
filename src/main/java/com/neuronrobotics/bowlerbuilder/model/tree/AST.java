package com.neuronrobotics.bowlerbuilder.model.tree;

public class AST extends KTree<String> {

  public AST() {
    super();
  }

  public AST(String rootData) {
    super(rootData);
  }

  public AST(TreeNode<String> root) {
    super(root);
  }

}
