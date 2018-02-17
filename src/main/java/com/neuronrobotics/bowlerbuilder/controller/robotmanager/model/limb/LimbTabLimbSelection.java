package com.neuronrobotics.bowlerbuilder.controller.robotmanager.model.limb;

import com.neuronrobotics.bowlerbuilder.controller.robotmanager.model.link.LimbTabLinkSelection;
import com.neuronrobotics.bowlerstudio.assets.AssetFactory;
import com.neuronrobotics.sdk.addons.kinematics.DHLink;
import com.neuronrobotics.sdk.addons.kinematics.DHParameterKinematics;
import com.neuronrobotics.sdk.addons.kinematics.LinkConfiguration;
import com.neuronrobotics.sdk.addons.kinematics.LinkType;
import com.neuronrobotics.sdk.addons.kinematics.MobileBase;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class LimbTabLimbSelection extends LimbSelection {

  private final ObjectProperty<LinkConfiguration> selectionProperty;
  private final VBox view;
  private final AnchorPane widget;

  public LimbTabLimbSelection(MobileBase device, DHParameterKinematics limb) {
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

    selectionProperty.addListener((observable, oldValue, newValue) ->
        widget.getChildren().setAll(
            new LimbTabLinkSelection(null, newValue).getWidget())); //NOPMD

    Button addLink = new Button();
    addLink.setGraphic(AssetFactory.loadIcon("Add-Link.png"));
    addLink.setOnAction(event -> {
      TextInputDialog dialog = new TextInputDialog();
      dialog.setTitle("Add a new link");
      dialog.setHeaderText("Set the name for the link");
      dialog.setContentText("Name: ");
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
        newLink.setName(result);

        getNextChannel(device, newLink);
        limb.addNewLink(newLink, new DHLink(0, 0, 100, 0));
      });
    });

    HBox controlsBox = new HBox(5, addLink);
    controlsBox.setPadding(new Insets(5));
    HBox.setHgrow(controlsBox, Priority.NEVER);
    content.getChildren().add(controlsBox);
  }

  private static void getNextChannel(MobileBase base, LinkConfiguration confOfChannel) {
    Map<String, HashMap<Integer, Boolean>> deviceMap = new HashMap<>();

    for (DHParameterKinematics dh : base.getAllDHChains()) {
      for (LinkConfiguration conf : dh.getLinkConfigurations()) {
        Map<Integer, Boolean> channelMap;
        deviceMap.computeIfAbsent(conf.getDeviceScriptingName(), k -> new HashMap<>());
        channelMap = deviceMap.get(conf.getDeviceScriptingName());
        channelMap.put(conf.getHardwareIndex(), true);

        conf.getSlaveLinks().forEach(sl -> {
          Map<Integer, Boolean> slaveChannelMap;
          deviceMap.computeIfAbsent(sl.getDeviceScriptingName(), k -> new HashMap<>());
          slaveChannelMap = deviceMap.get(sl.getDeviceScriptingName());
          slaveChannelMap.put(sl.getHardwareIndex(), true);
        });
      }
    }

    deviceMap.forEach((key, value) -> {
      for (int i = 0; i < 24; i++) { //TODO: Why 24? From the DyIO?
        if (value.get(i) == null) {
          confOfChannel.setDeviceScriptingName(key);
          confOfChannel.setHardwareIndex(i);
          return;
        }
      }
    });

    throw new RuntimeException("No channels are available on given devices");
  }

  @Override
  public Node getWidget() {
    return view;
  }

  public DHParameterKinematics getLimb() {
    return limb;
  }

}
