package com.neuronrobotics.bowlerbuilder.controller.robotmanager.model.link;

import com.neuronrobotics.bowlerstudio.assets.AssetFactory;
import com.neuronrobotics.sdk.addons.kinematics.DHLink;
import com.neuronrobotics.sdk.addons.kinematics.LinkConfiguration;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class LimbTabLinkSelection extends LinkSelection {

  private final VBox vBox;

  public LimbTabLinkSelection(DHLink dhLink, LinkConfiguration configuration) {
    super(dhLink, configuration);
    vBox = new VBox(5);

    Button removeLink = new Button();
    removeLink.setGraphic(AssetFactory.loadIcon("Remove-Link.png"));

    Button addSlaveToLink = new Button();
    addSlaveToLink.setGraphic(AssetFactory.loadIcon("Slave-Links.png"));
    addSlaveToLink.setOnAction(event -> {
      //TODO: Add a new slave to the link
    });

    HBox slaves = new HBox();
    configuration.getSlaveLinks().forEach(conf -> {
      Button slaveButton = new Button(conf.getName());

      slaveButton.setOnAction(event -> {
        Button removeSlave = new Button();
        removeSlave.setGraphic(AssetFactory.loadIcon("Remove-Link.png"));
        removeSlave.setOnAction(event1 -> {
          //TODO: Remove this link as a slave
        });

        vBox.getChildren().add(removeSlave);
      });
    });

    vBox.getChildren().addAll(getTitleLabel(configuration.getName()),
        new VBox(5, new HBox(5, removeLink, addSlaveToLink), slaves));
  }

  @Override
  public Node getWidget() {
    return vBox;
  }

}
