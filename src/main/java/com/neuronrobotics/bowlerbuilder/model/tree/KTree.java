package com.neuronrobotics.bowlerbuilder.model.tree;

import java.util.LinkedList;
import java.util.Queue;

/**
 * k-ary tree.
 *
 * @param <T> node type
 */
public class KTree<T extends TreeNode<T, ?>> {

  private final T root;

  public KTree(T root) {
    this.root = root;
  }

  /**
   * Get the height of this tree.
   *
   * @return height
   */
  public int height() {
    if (root == null) {
      return 0;
    }

    final Queue<T> nodes = new LinkedList<>();
    nodes.add(root);
    int height = 0;

    while (true) {
      int count = nodes.size();
      if (count == 0) {
        return height;
      }

      height++;

      while (count > 0) {
        T newNode = nodes.remove();
        nodes.addAll(newNode.getChildren());
        count--;
      }
    }
  }

  /**
   * Get the number of children this node has, including the children's children.
   *
   * @return number of children
   */
  public int numberOfChildren() {
    if (root == null) {
      return 0;
    }

    final Queue<T> nodes = new LinkedList<>();
    nodes.add(root);
    final int[] count = {0};

    while (nodes.size() > 0) {
      nodes.remove().getChildren().forEach(child -> {
        nodes.add(child);
        count[0]++;
      });
    }

    return count[0];
  }

  public T getRoot() {
    return root;
  }

}
