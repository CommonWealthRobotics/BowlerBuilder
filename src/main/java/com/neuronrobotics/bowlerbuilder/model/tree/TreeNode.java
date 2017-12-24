package com.neuronrobotics.bowlerbuilder.model.tree;

import java.util.List;

/**
 * k-ary tree node.
 */
public interface TreeNode<T> {

  /**
   * Construct a new node with the data and add it to the child list.
   *
   * @param data data for new node
   */
  void addChild(T data);

  /**
   * Add the node to the child list.
   *
   * @param child node to add
   */
  void addChild(TreeNode<T> child);

  /**
   * Remove a child from the list.
   *
   * @param child node to remove
   */
  void removeChild(TreeNode<T> child);

  /**
   * Remove all children.
   */
  void removeChildren();

  /**
   * Get the list of children.
   *
   * @return children
   */
  List<TreeNode<T>> getChildren();

  /**
   * Get the parent node.
   *
   * @return parent node
   */
  TreeNode<T> getParent();

  /**
   * Set the parent node.
   *
   * @param parent new parent
   */
  void setParent(TreeNode<T> parent);

  /**
   * Get the data held by this node.
   *
   * @return data
   */
  T getData();

}
