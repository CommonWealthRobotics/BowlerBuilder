package com.neuronrobotics.bowlerbuilder.controller.scripteditor.ast;

import com.neuronrobotics.bowlerbuilder.model.tree.groovy.ast.ASTNode;
import javafx.scene.Node;

/**
 * Interface for viewing an {@link ASTNode}.
 */
public interface ASTNodeView {

  /**
   * Get the view for this {@link ASTNode}.
   *
   * @return view
   */
  Node getView();

  /**
   * Get the underlying {@link ASTNode}.
   *
   * @return internal node
   */
  ASTNode getASTNode();

}
