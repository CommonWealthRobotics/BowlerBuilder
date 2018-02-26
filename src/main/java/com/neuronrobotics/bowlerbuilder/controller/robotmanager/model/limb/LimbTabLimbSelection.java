package com.neuronrobotics.bowlerbuilder.controller.robotmanager.model.limb;

import com.neuronrobotics.bowlerbuilder.controller.CreatureEditorController;
import com.neuronrobotics.bowlerbuilder.controller.robotmanager.model.link.LimbTabLinkSelection;
import com.neuronrobotics.bowlerbuilder.view.dialog.AddLinkDialog;
import com.neuronrobotics.bowlerstudio.assets.AssetFactory;
import com.neuronrobotics.sdk.addons.kinematics.AbstractKinematicsNR;
import com.neuronrobotics.sdk.addons.kinematics.DHLink;
import com.neuronrobotics.sdk.addons.kinematics.DHParameterKinematics;
import com.neuronrobotics.sdk.addons.kinematics.LinkConfiguration;
import com.neuronrobotics.sdk.addons.kinematics.LinkType;
import com.neuronrobotics.sdk.addons.kinematics.MobileBase;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class LimbTabLimbSelection extends LimbSelection {

  private final ObjectProperty<LinkConfiguration> selectionProperty;
  private final VBox view;
  private final AnchorPane widget;
  private final HBox scrollPaneContent;

  public LimbTabLimbSelection(MobileBase device, DHParameterKinematics limb,
      CreatureEditorController creatureEditorController) {
    super(limb);

    view = new VBox(5);
    selectionProperty = new SimpleObjectProperty<>();
    widget = new AnchorPane();

    view.setPadding(new Insets(5));

    HBox content = new HBox(5);
    HBox.setHgrow(content, Priority.NEVER);
    content.setAlignment(Pos.CENTER_LEFT);
    content.setPadding(new Insets(5));

    view.getChildren().addAll(getTitleLabel(limb.getScriptingName()), content, widget);

    ScrollPane scrollPane = new ScrollPane();
    HBox.setHgrow(scrollPane, Priority.ALWAYS);
    scrollPane.setVbarPolicy(ScrollBarPolicy.NEVER);
    scrollPaneContent = new HBox(5);
    HBox.setHgrow(scrollPaneContent, Priority.ALWAYS);

    scrollPaneContent.setPadding(new Insets(5));
    scrollPaneContent.getChildren().addAll(
        getLinkButtons(limb.getLinkConfigurations()));

    scrollPane.setContent(scrollPaneContent);
    content.getChildren().add(scrollPane);

    selectionProperty.addListener((observable, oldValue, newValue) -> {
      if (newValue != null) {
        widget.getChildren().setAll(
            new LimbTabLinkSelection(null, newValue, limb, this, //NOPMD
                creatureEditorController).getWidget());
      }
    });

    Button addLink = new Button();
    addLink.setGraphic(AssetFactory.loadIcon("Add-Link.png"));
    addLink.setOnAction(event -> {
      AddLinkDialog dialog = new AddLinkDialog(getTakenChannels(device));
      dialog.showAndWait().ifPresent(result -> {
        LinkConfiguration newLink = new LinkConfiguration();
        List<LinkConfiguration> linkConfigurations = limb.getFactory().getLinkConfigurations();

        int numOfLinks = linkConfigurations.size();

        LinkType typeOfLink = linkConfigurations.get(numOfLinks - 1).getTypeEnum();

        if (typeOfLink == null) {
          typeOfLink = LinkType.VIRTUAL;
        }

        newLink.setType(typeOfLink);
        newLink.setTypeString(typeOfLink.toString());
        newLink.setName(result[0]);
        newLink.setHardwareIndex(Integer.parseInt(result[1]));
        newLink.setLinkIndex(limb.getNumberOfLinks());

        limb.addNewLink(newLink, new DHLink(0, 0, 100, 0));

        //Add the new link to the ScrollPane for continuity, the rest of the updating is done by
        //regenerating the menus
        scrollPaneContent.getChildren().setAll(getLinkButtons(limb.getLinkConfigurations()));
        creatureEditorController.regenerateMenus();
      });
    });

    HBox controlsBox = new HBox(5, addLink);
    controlsBox.setPadding(new Insets(5));
    HBox.setHgrow(controlsBox, Priority.NEVER);
    content.getChildren().add(controlsBox);
  }

  private Set<Integer> getTakenChannels(MobileBase device) {
    return device.getAllDHChains().stream().map(AbstractKinematicsNR::getLinkConfigurations)
        .flatMap(Collection::stream)
        .map(LinkConfiguration::getHardwareIndex)
        .collect(Collectors.toSet());
  }

  private List<Button> getLinkButtons(List<LinkConfiguration> configs) {
    return configs.stream().map(config -> {
      Button linkButton = new Button(config.getName());
      //Set the selection to this link
      linkButton.setOnAction(event -> selectionProperty.set(config));
      return linkButton;
    }).collect(Collectors.toList());
  }

  public void clearSelectedWidget() {
    widget.getChildren().clear();
    selectionProperty.set(null);
  }

  public void regenerateLinkButtons() {
    scrollPaneContent.getChildren().setAll(getLinkButtons(limb.getLinkConfigurations()));
  }

  @Override
  public Node getWidget() {
    return view;
  }

  public DHParameterKinematics getLimb() {
    return limb;
  }

}
