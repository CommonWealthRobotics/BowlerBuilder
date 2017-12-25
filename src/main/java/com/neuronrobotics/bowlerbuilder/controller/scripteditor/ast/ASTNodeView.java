package com.neuronrobotics.bowlerbuilder.controller.scripteditor.ast;

import com.neuronrobotics.bowlerbuilder.model.tree.groovy.ast.ASTNode;
import javafx.scene.Node;
import javafx.scene.layout.AnchorPane;

/**
 * Interface for viewing an {@link ASTNode}.
 */
public abstract class ASTNodeView<T extends ASTNode> {

  protected T node;
  protected AnchorPane pane;

  public ASTNodeView(T node) {
    this.node = node;
    pane = new AnchorPane();
  }

  /**
   * Get the view for this {@link ASTNode}.
   *
   * @return view
   */
  public Node getView() {
    return pane;
  }

  /**
   * Get the underlying {@link ASTNode}.
   *
   * @return internal node
   */
  public T getASTNode() {
    return node;
  }

}
