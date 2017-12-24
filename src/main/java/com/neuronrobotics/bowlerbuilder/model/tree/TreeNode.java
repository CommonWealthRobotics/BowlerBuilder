package com.neuronrobotics.bowlerbuilder.model.tree;

import java.util.ArrayList;
import java.util.List;

/**
 * k-ary tree node.
 *
 * @param <T> data type
 */
public class TreeNode<T> {

  private T data;
  private List<TreeNode<T>> children;
  private TreeNode<T> parent;

  public TreeNode() {
    children = new ArrayList<>();
  }

  public TreeNode(T data) {
    this.data = data;
    children = new ArrayList<>();
  }

  public int getNumberOfChildren() {
    return children.size();
  }

  public boolean hasChildren() {
    return !children.isEmpty();
  }

  public void addChild(T data) {
    TreeNode<T> child = new TreeNode<>(data);
    addChild(child);
  }

  public void addChild(TreeNode<T> child) {
    children.add(child);
    child.setParent(this);
  }

  public void removeChild(TreeNode<T> child) {
    children.remove(child);
  }

  public T getData() {
    return data;
  }

  public List<TreeNode<T>> getChildren() {
    return children;
  }

  public TreeNode<T> getParent() {
    return parent;
  }

  public void setParent(TreeNode<T> parent) {
    this.parent = parent;
  }

}
