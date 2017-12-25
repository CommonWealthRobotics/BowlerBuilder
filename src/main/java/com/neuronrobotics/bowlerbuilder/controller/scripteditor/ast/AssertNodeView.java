package com.neuronrobotics.bowlerbuilder.controller.scripteditor.ast;

import com.neuronrobotics.bowlerbuilder.model.tree.groovy.ast.AssertNode;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class AssertNodeView extends ASTNodeView<AssertNode> {

  public AssertNodeView(AssertNode node) {
    super(node);
    pane.getChildren().add(new VBox(5,
        new Label(node.getBooleanExpression()),
        new Label(node.getMessageExpression())));
  }

}
