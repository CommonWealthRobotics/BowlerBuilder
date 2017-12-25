package com.neuronrobotics.bowlerbuilder.controller.scripteditor.ast;

import com.neuronrobotics.bowlerbuilder.model.tree.groovy.ast.ASTNode;
import com.neuronrobotics.bowlerbuilder.model.tree.groovy.ast.ClassDeclarationNode;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;

public class ClassDeclarationNodeView implements ASTNodeView {

  private ClassDeclarationNode node;
  private AnchorPane view;

  public ClassDeclarationNodeView(ClassDeclarationNode node) {
    this.node = node;
    view = new AnchorPane();
    view.getChildren().add(new Label(node.getClassName()));
  }

  @Override
  public Node getView() {
    return view;
  }

  @Override
  public ASTNode getASTNode() {
    return node;
  }
}
