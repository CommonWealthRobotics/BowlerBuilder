package com.neuronrobotics.bowlerbuilder.model.tree.groovy.ast;

import com.neuronrobotics.bowlerbuilder.model.tree.TreeNode;
import java.util.LinkedList;
import java.util.List;

/**
 * k-ary {@link TreeNode} of {@link String}.
 */
public class ASTNode implements TreeNode<ASTNode, String> {

  protected String data;
  protected ASTNodeType type;
  protected List<ASTNode> children;
  protected ASTNode parent;

  public ASTNode(ASTNodeType type) {
    this("root", type);
  }

  public ASTNode(String name, ASTNodeType type) {
    data = name;
    this.type = type;
    children = new LinkedList<>();
  }

  public void addChild(String name, ASTNodeType type) {
    addChild(new ASTNode(name, type));
  }

  @Override
  public void addChild(ASTNode child) {
    if ("root".equals(child.getData())) {
      child.getChildren().forEach(this::addChild);
    } else {
      children.add(child);
      child.setParent(this);
    }
  }

  @Override
  public void removeChild(ASTNode child) {
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

  public ASTNodeType getType() {
    return type;
  }

  @Override
  public List<ASTNode> getChildren() {
    return children;
  }

  public void setChildren(List<ASTNode> children) {
    this.children = children;
  }

  @Override
  public ASTNode getParent() {
    return parent;
  }

  @Override
  public void setParent(ASTNode parent) {
    this.parent = parent;
  }

}
