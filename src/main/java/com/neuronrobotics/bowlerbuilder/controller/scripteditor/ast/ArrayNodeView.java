package com.neuronrobotics.bowlerbuilder.controller.scripteditor.ast;

import com.neuronrobotics.bowlerbuilder.model.tree.groovy.ast.ArrayNode;
import javafx.scene.control.Label;

public class ArrayNodeView extends ASTNodeView<ArrayNode> {

  public ArrayNodeView(ArrayNode node) {
    super(node);
    pane.getChildren().add(new Label(node.getExpression()));
  }

}
