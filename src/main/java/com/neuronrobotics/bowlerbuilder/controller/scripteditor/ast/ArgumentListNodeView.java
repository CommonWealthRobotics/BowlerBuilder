package com.neuronrobotics.bowlerbuilder.controller.scripteditor.ast;

import com.neuronrobotics.bowlerbuilder.model.tree.groovy.ast.ArgumentListNode;
import javafx.scene.control.Label;

public class ArgumentListNodeView extends ASTNodeView<ArgumentListNode> {

  public ArgumentListNodeView(ArgumentListNode node) {
    super(node);
    pane.getChildren().add(new Label("Argument List"));
  }

}
