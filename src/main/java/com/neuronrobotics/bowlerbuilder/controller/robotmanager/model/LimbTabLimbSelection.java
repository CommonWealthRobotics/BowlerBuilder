package com.neuronrobotics.bowlerbuilder.controller.robotmanager.model;

import com.neuronrobotics.bowlerstudio.assets.AssetFactory;
import com.neuronrobotics.sdk.addons.kinematics.DHParameterKinematics;
import com.neuronrobotics.sdk.addons.kinematics.LinkConfiguration;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;

public class LimbTabLimbSelection implements Selection {

  private final DHParameterKinematics limb;
  private final ObjectProperty<LinkConfiguration> selectionProperty;
  private final VBox view;
  private final AnchorPane widget;

  public LimbTabLimbSelection(DHParameterKinematics limb) {
    this.limb = limb;

    view = new VBox(5);
    selectionProperty = new SimpleObjectProperty<>();
    widget = new AnchorPane();

    view.setPadding(new Insets(5));

    HBox content = new HBox(5);
    HBox.setHgrow(content, Priority.NEVER);
    content.setAlignment(Pos.CENTER_LEFT);
    content.setPadding(new Insets(5));

    view.getChildren().addAll(getLabel(limb.getScriptingName()), content, widget);

    ScrollPane scrollPane = new ScrollPane();
    HBox.setHgrow(scrollPane, Priority.ALWAYS);
    scrollPane.setVbarPolicy(ScrollBarPolicy.NEVER);
    HBox scrollPaneContent = new HBox(5);
    HBox.setHgrow(scrollPaneContent, Priority.ALWAYS);
    scrollPaneContent.setPadding(new Insets(5));
    scrollPane.setContent(scrollPaneContent);

    limb.getFactory().getLinkConfigurations().forEach(link -> {
      Button linkButton = new Button(link.getName());
      //Set the selection to this link
      linkButton.setOnAction(event -> selectionProperty.set(link));
      scrollPaneContent.getChildren().add(linkButton);
    });

    content.getChildren().add(scrollPane);

    selectionProperty.addListener((observable, oldValue, newValue) -> {
      Button removeLink = new Button();
      removeLink.setGraphic(AssetFactory.loadIcon("Remove-Link.png"));
      VBox vBox = new VBox(5, getLabel(newValue.getName()), removeLink);
      widget.getChildren().setAll(vBox);
    });

    Button addLink;
    addLink = new Button();
    addLink.setGraphic(AssetFactory.loadIcon("Add-Link.png"));

    HBox controlsBox = new HBox(5, addLink);
    controlsBox.setPadding(new Insets(5));
    HBox.setHgrow(controlsBox, Priority.NEVER);
    content.getChildren().add(controlsBox);
  }

  private Label getLabel(String text) {
    Label label = new Label(text);
    label.setFont(Font.font(16));
    return label;
  }

  @Override
  public Node getWidget() {
    return view;
  }

  public DHParameterKinematics getLimb() {
    return limb;
  }

}
