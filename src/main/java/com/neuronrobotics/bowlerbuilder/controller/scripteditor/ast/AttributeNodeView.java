package com.neuronrobotics.bowlerbuilder.controller.scripteditor.ast;

import com.neuronrobotics.bowlerbuilder.model.tree.groovy.ast.AttributeNode;
import javafx.scene.control.Label;

public class AttributeNodeView extends ASTNodeView<AttributeNode> {

  public AttributeNodeView(AttributeNode node) {
    super(node);
    pane.getChildren().add(new Label("Attribute"));
  }

}
