package com.neuronrobotics.bowlerbuilder.controller;

import com.google.inject.Inject;
import com.neuronrobotics.bowlerbuilder.FxUtil;
import com.neuronrobotics.bowlerbuilder.controller.cadengine.CadEngine;
import eu.mihosoft.vrl.v3d.CSG;
import java.util.Collection;
import java.util.Map;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.SubScene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.shape.MeshView;

public class CADModelViewerController {

  private final CadEngine engine;
  @FXML
  private BorderPane root;
  private boolean axisShowing = true;
  private boolean handShowing = true;

  @Inject
  public CADModelViewerController(CadEngine engine) {
    this.engine = engine;
  }

  @FXML
  protected void initialize() {
    FxUtil.runFX(() -> {
      SubScene subScene = engine.getSubScene();
      subScene.setFocusTraversable(false);
      subScene.widthProperty().bind(root.widthProperty());
      subScene.heightProperty().bind(root.heightProperty());
      AnchorPane.setTopAnchor(subScene, 0.0);
      AnchorPane.setRightAnchor(subScene, 0.0);
      AnchorPane.setLeftAnchor(subScene, 0.0);
      AnchorPane.setBottomAnchor(subScene, 0.0);
    });

    root.setCenter(engine.getView());
    root.setId("cadViewerBorderPane");
  }

  /**
   * Add MeshViews from a CSG.
   *
   * @param csg CSG to add
   */
  public void addCSG(CSG csg) {
    engine.addCSG(csg);
  }

  /**
   * Add MeshViews from all CSGs.
   *
   * @param csgs CSGs to add
   */
  public void addAllCSGs(CSG... csgs) {
    engine.addAllCSGs(csgs);
  }

  /**
   * Add MeshViews from all CSGs.
   *
   * @param csgs List of CSGs to add
   */
  public void addAllCSGs(Collection<CSG> csgs) {
    engine.addAllCSGs(csgs);
  }

  /**
   * Removes all meshes except for the background.
   */
  public void clearMeshes() {
    engine.clearMeshes();
  }

  @FXML
  private void onHomeCamera(ActionEvent actionEvent) {
    engine.homeCamera();
  }

  @FXML
  private void onAxis(ActionEvent actionEvent) {
    axisShowing = !axisShowing;
    engine.axisShowingProperty().setValue(axisShowing);
  }

  @FXML
  private void onHand(ActionEvent actionEvent) {
    handShowing = !handShowing;
    engine.handShowingProperty().setValue(handShowing);
  }

  @FXML
  private void onClearObjects(ActionEvent actionEvent) {
    clearMeshes();
  }

  public Map<CSG, MeshView> getCsgMap() {
    return engine.getCsgMap();
  }

}
