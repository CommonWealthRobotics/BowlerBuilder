package com.neuronrobotics.bowlerbuilder.controller;

import com.google.inject.Inject;
import com.neuronrobotics.bowlerbuilder.FxUtil;
import com.neuronrobotics.bowlerbuilder.LoggerUtilities;
import com.neuronrobotics.bowlerbuilder.controller.robotmanager.model.LimbSelection;
import com.neuronrobotics.bowlerbuilder.controller.robotmanager.model.LinkSelection;
import com.neuronrobotics.bowlerbuilder.controller.robotmanager.model.Selection;
import com.neuronrobotics.bowlerstudio.assets.AssetFactory;
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
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TreeItem;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class CreatureLabController {

  private static final Logger logger =
      LoggerUtilities.getLogger(CreatureLabController.class.getSimpleName());

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
  private final AnchorPane limbWidget;
  private ObjectProperty<Selection> selectionProperty;

  @Inject
  public CreatureLabController() {
    limbWidget = new AnchorPane();
    selectionProperty = new SimpleObjectProperty<>();
    selectionProperty.addListener((observable, oldValue, newValue) ->
        limbWidget.getChildren().setAll(newValue.getWidget()));
  }

  @FXML
  protected void initialize() {
    limbTab.setGraphic(AssetFactory.loadIcon("creature.png"));
    limbTab.setStyle("-fx-padding: 5px;");
    movementTab.setGraphic(AssetFactory.loadIcon("Move-Limb.png"));
    movementTab.setStyle("-fx-padding: 5px;");
  }

  public void generateMenus(MobileBase device) {
    VBox limbSelector = new VBox(10);
    limbSelector.getChildren().addAll(
        getLimbHBox(AssetFactory.loadIcon("Load-Limb-Legs.png"),
            AssetFactory.loadIcon("Add-Leg.png"), device.getLegs()),
        getLimbHBox(AssetFactory.loadIcon("Load-Limb-Arms.png"),
            AssetFactory.loadIcon("Add-Arm.png"), device.getAppendages()),
        getLimbHBox(AssetFactory.loadIcon("Load-Limb-Steerable-Wheels.png"),
            AssetFactory.loadIcon("Add-Steerable-Wheel.png"), device.getSteerable()),
        getLimbHBox(AssetFactory.loadIcon("Load-Limb-Fixed-Wheels.png"),
            AssetFactory.loadIcon("Add-Fixed-Wheel.png"), device.getDrivable()));

    VBox limbContent = new VBox(10);
    limbContent.getChildren().addAll(limbSelector, limbWidget);

    FxUtil.runFX(() -> limbTab.setContent(limbContent));
  }

  private HBox getLimbHBox(ImageView icon, ImageView addIcon, List<DHParameterKinematics> limbs) {
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
      limbButton.setOnAction(event -> selectionProperty.set(new LimbSelection(limb)));
      vBox.getChildren().add(limbButton);

      HBox hBoxInner = new HBox(5);
      limb.getFactory().getLinkConfigurations().forEach(link -> {
        Button linkButton = new Button(link.getName());
        //Set the selection to this link
        linkButton.setOnAction(event -> selectionProperty.set(new LinkSelection(link)));
        hBoxInner.getChildren().add(linkButton);
      });
      vBox.getChildren().add(hBoxInner);

      scrollPaneContent.getChildren().add(vBox);
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

  private TreeItem<String> loadLimbs(TreeItem<String> root, List<DHParameterKinematics> drivable) {
    drivable.forEach(limb -> root.getChildren().add(loadSingleLimb(limb)));
    return root;
  }

  private TreeItem<String> loadSingleLimb(DHParameterKinematics dh) {
    TreeItem<String> dhItem = new TreeItem<>(dh.getScriptingName(),
        AssetFactory.loadIcon("Move-Limb.png"));

    int i = 0;
    for (LinkConfiguration conf : dh.getFactory().getLinkConfigurations()) {
      dhItem.getChildren().add(loadSingleLink(conf, dh, i++));
    }

    TreeItem<String> placeLimb = new TreeItem<>("Move Root Of Limb",
        AssetFactory.loadIcon("Design-Parameter-Adjustment.png"));
    dhItem.getChildren().add(placeLimb);

    TreeItem<String> addLink = new TreeItem<>("Add Link",
        AssetFactory.loadIcon("Add-Link.png"));
    dhItem.getChildren().add(addLink);

    TreeItem<String> advanced = new TreeItem<>("Advanced Configuration",
        AssetFactory.loadIcon("Advanced-Configuration.png"));
    dhItem.getChildren().add(advanced);

    TreeItem<String> remove = new TreeItem<>("Remove " + dh.getScriptingName(),
        AssetFactory.loadIcon("Remove-Limb.png"));
    dhItem.getChildren().add(remove);

    return dhItem;
  }

  private TreeItem<String> loadSingleLink(LinkConfiguration conf,
      DHParameterKinematics dh, Integer index) {
    TreeItem<String> link = new TreeItem<>(conf.getName(),
        AssetFactory.loadIcon("Move-Single-Motor.png"));

    DHLink dhLink = dh.getChain().getLinks().get(index);
    //    LinkSliderWidget linkSliderWidget = new LinkSliderWidget(index, dhLink, device);

    TreeItem<String> design = new TreeItem<>("Design Parameters " + conf.getName(),
        AssetFactory.loadIcon("Design-Parameter-Adjustment.png"));
    link.getChildren().add(design);

    TreeItem<String> hwConfig = new TreeItem<>("Hardware Config " + conf.getName(),
        AssetFactory.loadIcon("Hardware-Config.png"));
    link.getChildren().add(hwConfig);

    TreeItem<String> slaves = new TreeItem<>("Slaves to " + conf.getName(),
        AssetFactory.loadIcon("Slave-Links.png"));
    TreeItem<String> addSlaves = new TreeItem<>("Add Slave to " + conf.getName(),
        AssetFactory.loadIcon("Add-Slave-Links.png"));
    slaves.getChildren().add(addSlaves);
    link.getChildren().add(slaves);

    TreeItem<String> remove = new TreeItem<>("Remove " + conf.getName(),
        AssetFactory.loadIcon("Remove-Link.png"));
    link.getChildren().add(remove);

    return link;
  }

  public ProgressIndicator getCadProgress() {
    return cadProgress;
  }

}