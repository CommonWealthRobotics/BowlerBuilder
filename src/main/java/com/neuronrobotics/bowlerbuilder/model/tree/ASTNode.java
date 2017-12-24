package com.neuronrobotics.bowlerbuilder.model.tree;

import java.util.ArrayList;
import java.util.List;

/**
 * k-ary {@link TreeNode} of {@link String}.
 */
public class ASTNode implements TreeNode<String> {

  protected String data;
  protected List<TreeNode<String>> children;
  protected TreeNode<String> parent;

  public ASTNode() {
    this("root");
  }

  public ASTNode(String name) {
    data = name;
    children = new ArrayList<>();
  }

  @Override
  public void addChild(String name) {
    addChild(new ASTNode(name));
  }

  @Override
  public void addChild(TreeNode<String> child) {
    if ("root".equals(child.getData())) {
      child.getChildren().forEach(this::addChild);
    } else {
      children.add(child);
      child.setParent(this);
    }
  }

  @Override
  public void removeChild(TreeNode<String> child) {
    children.remove(child);
  }

  @Override
  public void removeChildren() {
    children = null;
  }

  @Override
  public String getData() {
    return data;
  }

  @Override
  public List<TreeNode<String>> getChildren() {
    return children;
  }

  public void setChildren(List<TreeNode<String>> children) {
    this.children = children;
  }

  @Override
  public TreeNode<String> getParent() {
    return parent;
  }

  @Override
  public void setParent(TreeNode<String> parent) {
    this.parent = parent;
  }

}
