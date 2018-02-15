package com.neuronrobotics.bowlerbuilder.controller;

import com.google.common.base.Throwables;
import com.google.inject.Inject;
import com.neuronrobotics.bowlerbuilder.FxUtil;
import com.neuronrobotics.bowlerbuilder.LoggerUtilities;
import com.neuronrobotics.bowlerbuilder.controller.robotmanager.model.Selection;
import com.neuronrobotics.bowlerbuilder.controller.robotmanager.model.limb.ConfigTabLimbSelection;
import com.neuronrobotics.bowlerbuilder.controller.robotmanager.model.limb.LimbTabLimbSelection;
import com.neuronrobotics.bowlerbuilder.controller.robotmanager.model.limb.MovementTabLimbSelection;
import com.neuronrobotics.bowlerbuilder.controller.robotmanager.model.link.ConfigTabLinkSelection;
import com.neuronrobotics.bowlerbuilder.controller.robotmanager.model.link.MovementTabLinkSelection;
import com.neuronrobotics.bowlerstudio.assets.AssetFactory;
import com.neuronrobotics.bowlerstudio.creature.MobileBaseCadManager;
import com.neuronrobotics.sdk.addons.kinematics.DHLink;
import com.neuronrobotics.sdk.addons.kinematics.DHParameterKinematics;
import com.neuronrobotics.sdk.addons.kinematics.LinkConfiguration;
import com.neuronrobotics.sdk.addons.kinematics.MobileBase;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import org.controlsfx.control.Notifications;

public class CreatureLabController {

  private static final Logger logger =
      LoggerUtilities.getLogger(CreatureLabController.class.getSimpleName());
  private final AnchorPane limbWidget;
  private final AnchorPane movementWidget;
  private final AnchorPane configWidget;
  private final ObjectProperty<Selection> selectionProperty;
  private final ObjectProperty<AnchorPane> selectedWidgetPane;
  @FXML
  private ProgressIndicator cadProgress;
  @FXML
  private CheckBox autoRegenCAD;
  @FXML
  private Button regenCADButton;
  @FXML
  private Button genPrintableCAD;
  @FXML
  private Button genKinSTL;
  @FXML
  private TabPane creatureTabPane;
  @FXML
  private Tab limbTab;
  @FXML
  private Tab movementTab;
  @FXML
  private Tab configTab;
  private MobileBase device;
  private MobileBaseCadManager cadManager;

  @Inject
  public CreatureLabController() {
    limbWidget = new AnchorPane();
    movementWidget = new AnchorPane();
    configWidget = new AnchorPane();
    selectionProperty = new SimpleObjectProperty<>();
    selectedWidgetPane = new SimpleObjectProperty<>();
  }

  @FXML
  protected void initialize() {
    selectedWidgetPane.set(limbWidget); //Limb widget to start

    //Change the widget pane new widgets go into when the user changes tabs
    creatureTabPane.getSelectionModel().selectedItemProperty()
        .addListener((observable, oldValue, newValue) -> {
          if (newValue == limbTab) {
            selectedWidgetPane.set(limbWidget);
          } else if (newValue == movementTab) {
            selectedWidgetPane.set(movementWidget);
          } else if (newValue == configTab) {
            selectedWidgetPane.set(configWidget);
          }
        });

    //Fill the widget pane with the widget for the selection
    selectionProperty.addListener((observable, oldValue, newValue) ->
        selectedWidgetPane.get().getChildren().setAll(newValue.getWidget()));

    limbTab.setGraphic(AssetFactory.loadIcon("creature.png"));
    limbTab.setStyle("-fx-padding: 5px;");
    movementTab.setGraphic(AssetFactory.loadIcon("Move-Limb.png"));
    movementTab.setStyle("-fx-padding: 5px;");
    configTab.setGraphic(AssetFactory.loadIcon("Advanced-Configuration.png"));
    configTab.setStyle("-fx-padding: 5px;");

    regenCADButton.setGraphic(AssetFactory.loadIcon("Generate-Cad.png"));
    genPrintableCAD.setGraphic(AssetFactory.loadIcon("Printable-Cad.png"));
    genKinSTL.setGraphic(AssetFactory.loadIcon("Printable-Cad.png"));
  }

  /**
   * Fill the CreatureLab tabs with menus for a {@link MobileBase}.
   *
   * @param device {@link MobileBase} to load menus from
   * @param cadManager {@link MobileBaseCadManager} to trigger CAD regens to
   */
  public void generateMenus(MobileBase device, MobileBaseCadManager cadManager) {
    this.device = device;
    this.cadManager = cadManager;

    autoRegenCAD.selectedProperty().addListener((observable, oldValue, newValue) ->
        cadManager.setAutoRegen(newValue));
    cadManager.setAutoRegen(autoRegenCAD.isSelected());

    generateLimbTab();
    generateMovementTab();
    generateConfigTab();
  }

  private void generateLimbTab() {
    VBox limbSelector = new VBox(10);
    limbSelector.getChildren().addAll(
        getLimbTabLimbHBox(AssetFactory.loadIcon("Load-Limb-Legs.png"),
            AssetFactory.loadIcon("Add-Leg.png"), device.getLegs()),
        getLimbTabLimbHBox(AssetFactory.loadIcon("Load-Limb-Arms.png"),
            AssetFactory.loadIcon("Add-Arm.png"), device.getAppendages()),
        getLimbTabLimbHBox(AssetFactory.loadIcon("Load-Limb-Steerable-Wheels.png"),
            AssetFactory.loadIcon("Add-Steerable-Wheel.png"), device.getSteerable()),
        getLimbTabLimbHBox(AssetFactory.loadIcon("Load-Limb-Fixed-Wheels.png"),
            AssetFactory.loadIcon("Add-Fixed-Wheel.png"), device.getDrivable()));

    VBox content = new VBox(10);
    content.getChildren().addAll(limbSelector, limbWidget);

    FxUtil.runFX(() -> limbTab.setContent(getScrollPane(content)));
  }

  private void generateMovementTab() {
    VBox limbSelector = new VBox(10);
    limbSelector.getChildren().addAll(
        getMovementTabLimbHBox(AssetFactory.loadIcon("Load-Limb-Legs.png"),
            device.getLegs()),
        getMovementTabLimbHBox(AssetFactory.loadIcon("Load-Limb-Arms.png"),
            device.getAppendages()),
        getMovementTabLimbHBox(AssetFactory.loadIcon("Load-Limb-Steerable-Wheels.png"),
            device.getSteerable()),
        getMovementTabLimbHBox(AssetFactory.loadIcon("Load-Limb-Fixed-Wheels.png"),
            device.getDrivable()));

    VBox content = new VBox(10);
    content.getChildren().addAll(limbSelector, movementWidget);

    FxUtil.runFX(() -> movementTab.setContent(getScrollPane(content)));
  }

  private void generateConfigTab() {
    VBox limbSelector = new VBox(10);
    limbSelector.getChildren().addAll(
        getConfigTabLimbHBox(AssetFactory.loadIcon("Load-Limb-Legs.png"),
            device.getLegs()),
        getConfigTabLimbHBox(AssetFactory.loadIcon("Load-Limb-Arms.png"),
            device.getAppendages()),
        getConfigTabLimbHBox(AssetFactory.loadIcon("Load-Limb-Steerable-Wheels.png"),
            device.getSteerable()),
        getConfigTabLimbHBox(AssetFactory.loadIcon("Load-Limb-Fixed-Wheels.png"),
            device.getDrivable()));

    VBox content = new VBox(10);
    content.getChildren().addAll(limbSelector, configWidget);

    FxUtil.runFX(() -> configTab.setContent(getScrollPane(content)));
  }

  private ScrollPane getScrollPane(Node node) {
    ScrollPane pane = new ScrollPane(node);
    pane.setFitToWidth(true);
    pane.setPadding(new Insets(5));
    return pane;
  }

  private HBox getLimbTabLimbHBox(ImageView icon, ImageView addIcon,
      List<DHParameterKinematics> limbs) {
    HBox hBox = new HBox(5);
    HBox.setHgrow(hBox, Priority.NEVER);
    hBox.setAlignment(Pos.CENTER_LEFT);
    hBox.setPadding(new Insets(5));

    hBox.getChildren().add(icon);

    ScrollPane scrollPane = new ScrollPane();
    HBox.setHgrow(scrollPane, Priority.ALWAYS);
    scrollPane.setVbarPolicy(ScrollBarPolicy.NEVER);
    HBox scrollPaneContent = new HBox(5);
    HBox.setHgrow(scrollPaneContent, Priority.ALWAYS);
    scrollPaneContent.setPadding(new Insets(5));
    scrollPane.setContent(scrollPaneContent);

    limbs.forEach(limb -> {
      Button limbButton = new Button(limb.getScriptingName());
      //Set the selection to this limb
      limbButton
          .setOnAction(event -> selectionProperty.set(new LimbTabLimbSelection(limb)));
      scrollPaneContent.getChildren().add(limbButton);
    });

    hBox.getChildren().add(scrollPane);

    HBox addRemoveLinkHBox = new HBox(5);
    HBox.setHgrow(addRemoveLinkHBox, Priority.NEVER);
    addRemoveLinkHBox.setAlignment(Pos.CENTER_RIGHT);

    Button addLinkButton = new Button();
    addLinkButton.setGraphic(addIcon);
    Button removeLinkButton = new Button();
    removeLinkButton.setGraphic(AssetFactory.loadIcon("Remove-Limb.png"));
    addRemoveLinkHBox.getChildren().addAll(addLinkButton, removeLinkButton);

    hBox.getChildren().add(addRemoveLinkHBox);

    return hBox;
  }

  private HBox getMovementTabLimbHBox(ImageView icon,
      List<DHParameterKinematics> limbs) {
    HBox hBox = new HBox(5);
    HBox.setHgrow(hBox, Priority.NEVER);
    hBox.setAlignment(Pos.CENTER_LEFT);
    hBox.setPadding(new Insets(5));

    hBox.getChildren().add(icon);

    ScrollPane scrollPane = new ScrollPane();
    HBox.setHgrow(scrollPane, Priority.ALWAYS);
    scrollPane.setVbarPolicy(ScrollBarPolicy.NEVER);
    HBox scrollPaneContent = new HBox(5);
    HBox.setHgrow(scrollPaneContent, Priority.ALWAYS);
    scrollPaneContent.setPadding(new Insets(5));
    scrollPane.setContent(scrollPaneContent);

    limbs.forEach(limb -> {
      VBox vBox = new VBox(5);
      vBox.setPadding(new Insets(5));
      //Shaded background to denote ownership of links to limb
      vBox.setStyle("-fx-background-color: rgba(185, 185, 185, 0.51);");
      vBox.setAlignment(Pos.CENTER);

      Button limbButton = new Button(limb.getScriptingName());
      //Set the selection to this limb
      limbButton.setOnAction(
          event -> selectionProperty.set(new MovementTabLimbSelection(limb)));
      vBox.getChildren().add(limbButton);

      HBox hBoxInner = new HBox(5);
      List<DHLink> links = limb.getChain().getLinks();
      for (int i = 0; i < links.size(); i++) {
        final DHLink link = links.get(i);
        final LinkConfiguration configuration = limb.getLinkConfiguration(i);
        final int finalI = i; //For lambda

        Button linkButton = new Button(configuration.getName());
        //Set the selection to this link
        linkButton.setOnAction(event ->
            selectionProperty.set(new MovementTabLinkSelection(finalI, link, configuration, limb)));
        hBoxInner.getChildren().add(linkButton);
      }
      vBox.getChildren().add(hBoxInner);

      scrollPaneContent.getChildren().add(vBox);
    });

    hBox.getChildren().add(scrollPane);

    return hBox;
  }

  private HBox getConfigTabLimbHBox(ImageView icon,
      List<DHParameterKinematics> limbs) {
    HBox hBox = new HBox(5);
    HBox.setHgrow(hBox, Priority.NEVER);
    hBox.setAlignment(Pos.CENTER_LEFT);
    hBox.setPadding(new Insets(5));

    hBox.getChildren().add(icon);

    ScrollPane scrollPane = new ScrollPane();
    HBox.setHgrow(scrollPane, Priority.ALWAYS);
    scrollPane.setVbarPolicy(ScrollBarPolicy.NEVER);
    HBox scrollPaneContent = new HBox(5);
    HBox.setHgrow(scrollPaneContent, Priority.ALWAYS);
    scrollPaneContent.setPadding(new Insets(5));
    scrollPane.setContent(scrollPaneContent);

    limbs.forEach(limb -> {
      VBox vBox = new VBox(5);
      vBox.setPadding(new Insets(5));
      //Shaded background to denote ownership of links to limb
      vBox.setStyle("-fx-background-color: rgba(185, 185, 185, 0.51);");
      vBox.setAlignment(Pos.CENTER);

      Button limbButton = new Button(limb.getScriptingName());
      //Set the selection to this limb
      limbButton.setOnAction(
          event -> selectionProperty.set(new ConfigTabLimbSelection(limb, device, cadManager)));
      vBox.getChildren().add(limbButton);

      HBox hBoxInner = new HBox(5);
      List<DHLink> links = limb.getChain().getLinks();
      for (int i = 0; i < links.size(); i++) {
        final DHLink link = links.get(i);
        final LinkConfiguration configuration = limb.getLinkConfiguration(i);
        final int finalI = i; //For lambda

        Button linkButton = new Button(configuration.getName());
        //Set the selection to this link
        linkButton.setOnAction(event ->
            selectionProperty
                .set(new ConfigTabLinkSelection(finalI, link, configuration, limb, cadManager)));
        hBoxInner.getChildren().add(linkButton);
      }
      vBox.getChildren().add(hBoxInner);

      scrollPaneContent.getChildren().add(vBox);
    });

    hBox.getChildren().add(scrollPane);

    return hBox;
  }

  public ProgressIndicator getCadProgress() {
    return cadProgress;
  }

  @FXML
  private void onRegenCAD(ActionEvent actionEvent) {
    if (cadManager != null) {
      cadManager.generateCad(); //TODO: Always regen CAD regardless of auto regen flag
    }
  }

  @FXML
  private void onGenPrintableCAD(ActionEvent actionEvent) {
    genSTLs(device, cadManager, false);
  }

  @FXML
  private void onGenKinSTL(ActionEvent actionEvent) {
    genSTLs(device, cadManager, true);
  }

  /**
   * Show a {@link DirectoryChooser} to pick a save directory and then generate and save STL files
   * for the given {@link MobileBase} and {@link MobileBaseCadManager}.
   *
   * @param device creature to gen STLs for
   * @param cadManager CAD manager to gen STLs with
   * @param isKinematic whether to gen kinematic STLs
   */
  public void genSTLs(MobileBase device, MobileBaseCadManager cadManager,
      boolean isKinematic) {
    File defaultStlDir = new File(System.getProperty("user.home") + "/bowler-workspace/STL/");
    if (!defaultStlDir.exists()) {
      if (!defaultStlDir.mkdirs()) {
        logger.log(Level.WARNING, "Could not create default directory to save STL files.");
        return;
      }
    }

    FxUtil.runFX(() -> {
      DirectoryChooser chooser = new DirectoryChooser();
      chooser.setTitle("Select Output Directory For STL files");

      chooser.setInitialDirectory(defaultStlDir);
      File baseDirForFiles = chooser.showDialog(creatureTabPane.getScene().getWindow());
      if (baseDirForFiles == null) {
        logger.log(Level.INFO, "No directory selected. Not saving STL files.");
        return;
      }

      LoggerUtilities.newLoggingThread(logger, () -> {
        try {
          List<File> files = cadManager.generateStls(device, baseDirForFiles, isKinematic);

          FxUtil.runFX(() ->
              Notifications.create()
                  .title("STL Export Success")
                  .text("All STL files for the creature generated at:\n"
                      + files.get(0).getAbsolutePath())
                  .showInformation());
        } catch (IOException e) {
          logger.log(Level.WARNING, "Could not generate STL files to save in: "
              + baseDirForFiles.getAbsolutePath()
              + "\n" + Throwables.getStackTraceAsString(e));

          FxUtil.runFX(() ->
              Notifications.create()
                  .title("STL Export Failure")
                  .text("Could not generate STL files.")
                  .showError());
        } catch (RuntimeException e) {
          if (e.getMessage().contains("IgenerateBed")) {
            logger.log(Level.INFO, "Cannot generate STL files because the supplied CAD manager "
                + "does not implement the IgenerateBed interface.\n"
                + Throwables.getStackTraceAsString(e));
          } else {
            logger.log(Level.WARNING, Throwables.getStackTraceAsString(e));
          }

          FxUtil.runFX(() ->
              Notifications.create()
                  .title("STL Export Failure")
                  .text("Could not generate STL files.")
                  .showError());
        }
      }).start();
    });
  }

}
