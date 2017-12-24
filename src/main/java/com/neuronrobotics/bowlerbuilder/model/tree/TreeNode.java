package com.neuronrobotics.bowlerbuilder.model.tree;

import java.util.HashSet;
import java.util.Set;

/**
 * k-ary tree node.
 *
 * @param <T> data type
 */
public class TreeNode<T> {

  private T data;
  private Set<TreeNode<T>> children;
  private TreeNode<T> parent;

  public TreeNode() {
    children = new HashSet<>();
  }

  public TreeNode(T data) {
    this.data = data;
    children = new HashSet<>();
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
    if ("root".equals(child.getData())) {
      child.getChildren().forEach(this::addChild);
    } else {
      children.add(child);
      child.setParent(this);
    }
  }

  public void removeChild(TreeNode<T> child) {
    children.remove(child);
  }

  public T getData() {
    return data;
  }

  public Set<TreeNode<T>> getChildren() {
    return children;
  }

  public TreeNode<T> getParent() {
    return parent;
  }

  public void setParent(TreeNode<T> parent) {
    this.parent = parent;
  }

  public void removeChildren() {
    children = null;
  }

}
