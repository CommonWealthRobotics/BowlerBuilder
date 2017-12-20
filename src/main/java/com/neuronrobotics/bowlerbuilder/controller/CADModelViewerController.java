package com.neuronrobotics.bowlerbuilder.controller;

import com.neuronrobotics.bowlerbuilder.controller.cadengine.BowlerStudio3dEngine;
import eu.mihosoft.vrl.v3d.CSG;
import java.util.Arrays;
import java.util.Collection;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.SubScene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.shape.Rectangle;

public class CADModelViewerController {

  private final BowlerStudio3dEngine engine = new BowlerStudio3dEngine();
  @FXML
  private BorderPane root;
  private boolean axisShowing = true;
  private boolean handShowing = true;

  @FXML
  protected void initialize() {
    Platform.runLater(() -> {
      SubScene subScene = engine.getSubScene();
      subScene.setFocusTraversable(false);
      subScene.widthProperty().bind(root.widthProperty());
      subScene.heightProperty().bind(root.heightProperty());
      AnchorPane.setTopAnchor(subScene, 0.0);
      AnchorPane.setRightAnchor(subScene, 0.0);
      AnchorPane.setLeftAnchor(subScene, 0.0);
      AnchorPane.setBottomAnchor(subScene, 0.0);
    });

    final Rectangle engineClip = new Rectangle();
    engine.setClip(engineClip);
    engine.layoutBoundsProperty().addListener((observable, oldValue, newValue) -> {
      engineClip.setWidth(newValue.getWidth());
      engineClip.setHeight(newValue.getHeight());
    });

    root.setCenter(engine);
    root.setId("cadViewerBorderPane");
  }

  /**
   * Add MeshViews from a CSG.
   *
   * @param csg CSG to add
   */
  public void addCSG(CSG csg) {
    csg.toJavaFXMesh(null).getAsMeshViews().forEach(mesh -> engine.addCSG(csg));
  }

  /**
   * Add MeshViews from all CSGs.
   *
   * @param csgs CSGs to add
   */
  public void addAllCSGs(CSG... csgs) {
    Arrays.stream(csgs).forEach(this::addCSG);
  }

  /**
   * Add MeshViews from all CSGs.
   *
   * @param csgs List of CSGs to add
   */
  public void addAllCSGs(Collection<CSG> csgs) {
    csgs.forEach(this::addCSG);
  }

  /**
   * Removes all meshes except for the background.
   */
  public void clearMeshes() {
    engine.clearMeshViews();
  }

  @FXML
  private void onHomeCamera(ActionEvent actionEvent) {
    engine.homeCamera();
  }

  @FXML
  private void onAxis(ActionEvent actionEvent) {
    if (axisShowing) {
      engine.hideAxis();
    } else {
      engine.showAxis();
    }

    axisShowing = !axisShowing;
  }

  @FXML
  private void onHand(ActionEvent actionEvent) {
    if (handShowing) {
      engine.hideHand();
    } else {
      engine.showHand();
    }

    handShowing = !handShowing;
  }

  @FXML
  private void onClearObjects(ActionEvent actionEvent) {
    clearMeshes();
  }

  public BowlerStudio3dEngine getEngine() {
    return engine;
  }

}
