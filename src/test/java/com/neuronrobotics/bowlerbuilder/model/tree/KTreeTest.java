package com.neuronrobotics.bowlerbuilder.model.tree;

import static org.junit.Assert.assertEquals;

import com.neuronrobotics.bowlerbuilder.model.tree.groovy.ast.ASTNode;
import com.neuronrobotics.bowlerbuilder.model.tree.groovy.ast.ASTNodeType;
import org.junit.jupiter.api.Test;

public class KTreeTest {

  @Test
  void height0Test() {
    KTree<ASTNode> tree = new KTree<>(null);
    assertEquals(0, tree.height());
  }

  @Test
  void height1Test() {
    KTree<ASTNode> tree = new KTree<>(new ASTNode(ASTNodeType.RootNode));
    assertEquals(1, tree.height());
  }

  @Test
  void height2Test() {
    ASTNode root = new ASTNode(ASTNodeType.RootNode);
    root.addChild("child 1", ASTNodeType.ArrayNode);
    KTree<ASTNode> tree = new KTree<>(root);
    assertEquals(2, tree.height());
  }

  @Test
  void height3Test() {
    ASTNode root = new ASTNode(ASTNodeType.RootNode);
    ASTNode child1 = new ASTNode("child 1", ASTNodeType.ArrayNode);
    root.addChild(child1);
    root.addChild("child 2", ASTNodeType.ArrayNode);
    child1.addChild("child 3", ASTNodeType.ArrayNode);
    KTree<ASTNode> tree = new KTree<>(root);
    assertEquals(3, tree.height());
  }

  @Test
  void numChildren0Test() {
    KTree<ASTNode> tree = new KTree<>(null);
    assertEquals(0, tree.numberOfChildren());
  }

  @Test
  void numChildren0Test2() {
    KTree<ASTNode> tree = new KTree<>(new ASTNode(ASTNodeType.RootNode));
    assertEquals(0, tree.numberOfChildren());
  }

  @Test
  void numChildren1Test() {
    ASTNode root = new ASTNode(ASTNodeType.RootNode);
    root.addChild("child 1", ASTNodeType.ArrayNode);
    KTree<ASTNode> tree = new KTree<>(root);
    assertEquals(1, tree.numberOfChildren());
  }

  @Test
  void numChildren4Test() {
    ASTNode root = new ASTNode(ASTNodeType.RootNode);
    ASTNode child1 = new ASTNode("child 1", ASTNodeType.ArrayNode);
    root.addChild(child1);
    root.addChild("child 2", ASTNodeType.ArrayNode);
    child1.addChild("child 3", ASTNodeType.ArrayNode);
    KTree<ASTNode> tree = new KTree<>(root);
    assertEquals(3, tree.numberOfChildren());
  }

}
