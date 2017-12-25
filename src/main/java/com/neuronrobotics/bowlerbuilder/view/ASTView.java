package com.neuronrobotics.bowlerbuilder.view;

import com.neuronrobotics.bowlerbuilder.model.tree.KTree;
import com.neuronrobotics.bowlerbuilder.model.tree.groovy.ast.ASTNode;
import javafx.scene.canvas.Canvas;

/**
 * View for an KTree.
 */
public class ASTView {

  private Canvas canvas;
  private KTree<ASTNode> tree;

  public ASTView(KTree<ASTNode> tree) {
    this.tree = tree;
    canvas = new Canvas(200, 200);
  }

  public void generateView() {
  }

  public Canvas getCanvas() {
    return canvas;
  }

}
