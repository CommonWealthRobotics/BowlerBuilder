package com.neuronrobotics.bowlerbuilder.model.tree;

/**
 * k-ary tree.
 */
public class KTree<T extends TreeNode> {

  private final T root;

  public KTree(T root) {
    this.root = root;
  }

  public T getRoot() {
    return root;
  }

}
