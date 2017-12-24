package com.neuronrobotics.bowlerbuilder.model.tree;

import java.util.List;

public class AST extends KTree<String> {

  public AST() {
    super();
  }

  public AST(String rootData) {
    super(rootData);
  }

  public AST(TreeNode<String> root) {
    super(root);
  }

  public void optimizeTree() {
    optimizeTree(root);
  }

//  private void optimizeTree(TreeNode<String> node) {
//    if ("root".equals(node.getData())) {
//      if (node.getParent() == null) {
//        for (TreeNode<String> child : node.getChildren()) {
//          root.addChild(child);
//        }
//        root.removeChild(node);
//      } else {
//        for (TreeNode<String> child : node.getChildren()) {
//          node.getParent().addChild(child);
//        }
//        node.getParent().removeChild(node);
//      }
//
//      for (TreeNode<String> stringTreeNode : node.getChildren()) {
//        optimizeTree(stringTreeNode);
//      }
//    }
//  }

  private void optimizeTree(TreeNode<String> node) {
    if ("root".equals(node.getData())) {
      TreeNode<String> parent = node.getParent();

      if (parent == null) {
        parent = root;
      }

      List<TreeNode<String>> children = node.getChildren();

      for (TreeNode<String> stringTreeNode : children) {
        stringTreeNode.setParent(parent);
      }

      for (TreeNode<String> child : children) {
        parent.addChild(child);
      }

      node.getChildren().forEach(this::optimizeTree);
    }
  }

}
