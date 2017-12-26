package com.neuronrobotics.bowlerbuilder.model.tree;

import java.util.List;

/**
 * k-ary tree node.
 */
public interface TreeNode<T, D> {

  /**
   * Add the node to the child list.
   *
   * @param child node to add
   */
  void addChild(T child);

  /**
   * Remove a child from the list.
   *
   * @param child node to remove
   */
  void removeChild(T child);

  /**
   * Remove all children.
   */
  void removeChildren();

  /**
   * Get the list of children.
   *
   * @return children
   */
  List<T> getChildren();

  /**
   * Get the parent node.
   *
   * @return parent node
   */
  T getParent();

  /**
   * Set the parent node.
   *
   * @param parent new parent
   */
  void setParent(T parent);

  /**
   * Get the data held by this node.
   *
   * @return data
   */
  D getData();

}
