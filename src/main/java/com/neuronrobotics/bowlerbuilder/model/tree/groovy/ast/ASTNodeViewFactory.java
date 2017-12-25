package com.neuronrobotics.bowlerbuilder.model.tree.groovy.ast;

import com.neuronrobotics.bowlerbuilder.controller.scripteditor.ast.ASTNodeView;
import com.neuronrobotics.bowlerbuilder.controller.scripteditor.ast.ClassDeclarationNodeView;

public class ASTNodeViewFactory {

  public ASTNodeView getNodeView(ASTNode node) {
    if (node instanceof ClassDeclarationNode) {
      return new ClassDeclarationNodeView((ClassDeclarationNode) node);
    }

    return null;
  }

}
