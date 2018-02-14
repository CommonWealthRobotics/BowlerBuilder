package com.neuronrobotics.bowlerbuilder.controller;

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
import java.util.List;
import java.util.logging.Logger;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
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

}
