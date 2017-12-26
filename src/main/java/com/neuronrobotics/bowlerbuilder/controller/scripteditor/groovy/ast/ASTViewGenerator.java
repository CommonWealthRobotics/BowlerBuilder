package com.neuronrobotics.bowlerbuilder.controller.scripteditor.groovy.ast;

import com.neuronrobotics.bowlerbuilder.model.tree.KTree;
import com.neuronrobotics.bowlerbuilder.model.tree.groovy.ast.ASTNode;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.shape.Line;

public class ASTViewGenerator {

  public Node generateView(KTree<ASTNode> ast) {
    return generateView(ast.getRoot());
  }

  private Group generateView(ASTNode root) {
    Group view = new Group();
    Node rootView = makeViewForNode(root);
    view.getChildren().add(rootView);

    root.getChildren().forEach(child -> {
      Node childView = makeViewForNode(child);
      Line connection = makeConnection(rootView, childView);
      Node subViews = layoutNode(rootView, generateView(child));
      view.getChildren().add(childView);
      view.getChildren().add(connection);
      view.getChildren().add(subViews);
    });

    return view;
  }

  private Node layoutNode(Node parent, Node child) {
    child.setTranslateY(parent.getTranslateY() + 10);
    return child;
  }

  private Node makeViewForNode(ASTNode node) {
    return new AnchorPane(new Label("test"));
  }

  private Line makeConnection(Node parent, Node child) {
    return new Line(
        parent.getLayoutX(), parent.getLayoutY(),
        child.getLayoutX(), child.getLayoutY());
  }

}
