package com.neuronrobotics.bowlerbuilder.controller;

import com.google.common.base.Throwables;

import com.neuronrobotics.bowlerbuilder.LoggerUtilities;

import eu.mihosoft.vrl.v3d.CSG;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.embed.swing.JFXPanel;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Point3D;
import javafx.scene.DepthTest;
import javafx.scene.Group;
import javafx.scene.PerspectiveCamera;
import javafx.scene.SceneAntialiasing;
import javafx.scene.SubScene;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.PickResult;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.CullFace;
import javafx.scene.shape.DrawMode;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.Rectangle;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Transform;
import javafx.scene.transform.Translate;
import javafx.stage.FileChooser;

public class CADModelViewerController implements Initializable {

  @FXML
  private BorderPane root;
  @FXML
  private Button homeCameraButton;

  private final BowlerStudio3dEngine engine = new BowlerStudio3dEngine();


  public CADModelViewerController() {

  }

  @Override
  public void initialize(URL location, ResourceBundle resources) {
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

    root.setCenter(engine);
    root.setId("cadViewerBorderPane");
  }

  /**
   * Add a MeshView to the scene graph.
   *
   * @param mesh MeshView to add
   * @param csg CSG object the MeshView is contained in (used for exporting)
   */
  public void addMeshView(MeshView mesh, CSG csg) {
    mesh.setMaterial(new PhongMaterial(Color.RED));
    mesh.setDrawMode(DrawMode.FILL);
    mesh.setDepthTest(DepthTest.ENABLE);
    mesh.setCullFace(CullFace.BACK);

    mesh.setOnMouseClicked(mouseEvent -> {
      if (mouseEvent.getButton().equals(MouseButton.SECONDARY)) {
        ContextMenu menu = new ContextMenu();
        menu.setAutoHide(true);

        //Wireframe/Solid draw toggle
        MenuItem wireframe;

        //Set the title of the MenuItem to the opposite of the current draw
        if (mesh.getDrawMode().equals(DrawMode.LINE)) {
          wireframe = new MenuItem("Show As Solid");
        } else {
          wireframe = new MenuItem("Show As Wireframe");
        }

        //Set the onAction of the MenuItem to flip the draw state
        wireframe.setOnAction(actionEvent -> {
          if (mesh.getDrawMode().equals(DrawMode.FILL)) {
            mesh.setDrawMode(DrawMode.LINE);
            wireframe.setText("Show As Solid");
          } else {
            mesh.setDrawMode(DrawMode.FILL);
            wireframe.setText("Show As Wireframe");
          }
        });

        MenuItem exportSTL = new MenuItem("Export as STL");
        exportSTL.setOnAction(event -> {
          FileChooser chooser = new FileChooser();
          File save = chooser.showSaveDialog(root.getScene().getWindow());
          if (save != null) {
            if (!save.getPath().endsWith(".stl")) {
              save = new File(save.getAbsolutePath() + ".stl");
            }

            CSG readyCSG = csg.prepForManufacturing();
            try {
              FileUtils.write(save, readyCSG.toStlString());
            } catch (IOException e) {
              LoggerUtilities.getLogger().log(Level.SEVERE,
                  "Could not write CSG STL String.\n" + Throwables.getStackTraceAsString(e));
            }
          }
        });

        menu.getItems().addAll(wireframe, exportSTL);
        //Need to set the root as mesh.getScene().getWindow() so setAutoHide() works when we
        //right-click somewhere else
        mesh.setOnContextMenuRequested(event ->
            menu.show(mesh.getScene().getWindow(), event.getScreenX(), event.getScreenY()));
      }
    });

    engine.getCsgMap().put(csg, mesh);
    engine.getRoot().getChildren().add(mesh);
  }

  /**
   * Add MeshViews from a CSG.
   *
   * @param csg CSG to add
   */
  public void addMeshesFromCSG(CSG csg) {
    csg.toJavaFXMesh(null).getAsMeshViews().forEach(mesh -> addMeshView(mesh, csg));
  }

  /**
   * Add MeshViews from all CSGs.
   *
   * @param csgs CSGs to add
   */
  public void addMeshesFromAllCSG(CSG... csgs) {
    Arrays.stream(csgs).forEach(this::addMeshesFromCSG);
  }

  /**
   * Add MeshViews from all CSGs.
   *
   * @param csgs List of CSGs to add
   */
  public void addMeshesFromAllCSG(List<CSG> csgs) {
    csgs.forEach(this::addMeshesFromCSG);
  }

  /**
   * Removes all meshes except for the background.
   */
  public void clearMeshes() {
    engine.getCsgMap().clear();
    ObservableList<Node> children = engine.getRoot().getChildren();
    for (Node node : children) {
      if (node instanceof MeshView) {
        children.remove(node);
      }
    }
  }

  @FXML
  private void onHomeCamera(ActionEvent actionEvent) {
//    homeCamera();
  }

}
