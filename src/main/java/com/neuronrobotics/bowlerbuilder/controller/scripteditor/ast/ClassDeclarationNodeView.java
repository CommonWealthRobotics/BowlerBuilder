package com.neuronrobotics.bowlerbuilder.controller.scripteditor.ast;

import com.neuronrobotics.bowlerbuilder.model.tree.groovy.ast.ClassDeclarationNode;
import javafx.scene.control.Label;

public class ClassDeclarationNodeView extends ASTNodeView<ClassDeclarationNode> {

  public ClassDeclarationNodeView(ClassDeclarationNode node) {
    super(node);
    pane.getChildren().add(new Label(node.getClassName()));
  }

}
