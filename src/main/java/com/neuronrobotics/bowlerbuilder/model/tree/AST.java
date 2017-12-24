package com.neuronrobotics.bowlerbuilder.model.tree;

import com.neuronrobotics.bowlerbuilder.model.tree.groovy.ast.ASTNode;

/**
 * k-ary tree of {@link ASTNode}.
 */
public class AST {

  private ASTNode root;

  public AST() {
    this(new ASTNode());
  }

  public AST(String rootName) {
    this(new ASTNode(rootName));
  }

  public AST(ASTNode root) {
    this.root = root;
  }

  public ASTNode getRoot() {
    return root;
  }

}
