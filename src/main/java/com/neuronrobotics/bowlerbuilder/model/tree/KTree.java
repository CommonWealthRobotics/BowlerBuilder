package com.neuronrobotics.bowlerbuilder.model.tree;

/**
 * k-ary tree.
 * @param <T> data type
 */
public class KTree<T> {

  protected TreeNode<T> root;

  public KTree() {
    root = new TreeNode<>();
  }

  public KTree(T rootData) {
    root = new TreeNode<>(rootData);
  }

  public KTree(TreeNode<T> root) {
    this.root = root;
  }

  public TreeNode<T> getRoot() {
    return root;
  }

  public void setRoot(TreeNode<T> root) {
    this.root = root;
  }

}
