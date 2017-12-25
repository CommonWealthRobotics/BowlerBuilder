package com.neuronrobotics.bowlerbuilder.view;

import com.google.inject.Inject;
import com.neuronrobotics.bowlerbuilder.controller.scripteditor.ast.ASTNodeView;
import com.neuronrobotics.bowlerbuilder.model.tree.KTree;
import com.neuronrobotics.bowlerbuilder.model.tree.groovy.ast.ASTNode;
import com.neuronrobotics.bowlerbuilder.model.tree.groovy.ast.ASTNodeViewFactory;
import javafx.scene.canvas.Canvas;

/**
 * View for an KTree.
 */
public class ASTView {

  private Canvas canvas;
  private ASTNodeViewFactory factory;

  @Inject
  public ASTView(ASTNodeViewFactory factory) {
    this.factory = factory;
    canvas = new Canvas(200, 200);
  }

  public void generateView(KTree<ASTNode> tree) {
    ASTNodeView rootView = factory.getNodeView(tree.getRoot());
  }

  public Canvas getCanvas() {
    return canvas;
  }

}
