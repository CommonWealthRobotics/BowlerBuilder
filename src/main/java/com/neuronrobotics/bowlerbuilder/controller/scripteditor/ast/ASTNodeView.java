package com.neuronrobotics.bowlerbuilder.controller.scripteditor.ast;

import javafx.scene.Node;

/**
 * Interface for viewing an {@link com.neuronrobotics.bowlerbuilder.model.tree.groovy.ast.ASTNode}.
 */
public interface ASTNodeView {

  /**
   * Get the view for this {@link com.neuronrobotics.bowlerbuilder.model.tree.groovy.ast.ASTNode}.
   *
   * @return view
   */
  Node getView();

}
