package com.neuronrobotics.bowlerbuilder.controller.robotmanager;

import com.google.inject.Singleton;
import com.neuronrobotics.bowlerstudio.assets.AssetFactory;
import com.neuronrobotics.sdk.common.BowlerAbstractDevice;
import com.neuronrobotics.sdk.common.DeviceManager;
import com.neuronrobotics.sdk.common.IDeviceAddedListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.Accordion;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.HBox;
import javax.annotation.Nonnull;

@Singleton
public class ConnectionManager implements IDeviceAddedListener {

  private final List<BowlerAbstractDevice> devices;
  private final Accordion accordion;
  private final Map<BowlerAbstractDevice, TitledPane> paneMap;
  private boolean removingAll; //NOPMD

  public ConnectionManager(@Nonnull final HBox connectionsHeader,
      @Nonnull final Accordion accordion) {
    devices = new ArrayList<>();
    this.accordion = accordion;
    paneMap = new HashMap<>();

    final Button disconnectAll = new Button("Disconnect All");
    disconnectAll.setGraphic(AssetFactory.loadIcon("Disconnect-All.png"));
    disconnectAll.setOnAction(event -> {
      removingAll = true;

      devices.forEach(device -> {
        if (device.isAvailable()) {
          device.disconnect();
        }

        DeviceManager.remove(device);
        Platform.runLater(() -> accordion.getPanes().remove(paneMap.get(device)));
      });

      devices.clear();
      paneMap.clear();
      removingAll = false;
    });

    Platform.runLater(() -> {
      connectionsHeader.setPadding(new Insets(5));
      connectionsHeader.setSpacing(5);
      connectionsHeader.getChildren().addAll(AssetFactory.loadIcon("Connected-Devices.png"),
          new Label("Connected Devices"), disconnectAll);
    });

    DeviceManager.addDeviceAddedListener(this);
  }

  @Override
  public void onNewDeviceAdded(final BowlerAbstractDevice device) {
    devices.add(device);

    final TitledPane pane = new TitledPane();
    pane.setText(device.getScriptingName());

    final HBox content = new HBox();

    final Button disconnect = new Button("Disconnect " + device.getScriptingName());
    disconnect.setGraphic(AssetFactory.loadIcon("Disconnect-Device.png"));

    content.getChildren().add(disconnect);
    pane.setContent(content);

    paneMap.put(device, pane);
    Platform.runLater(() -> accordion.getPanes().add(pane));
  }

  @Override
  public void onDeviceRemoved(final BowlerAbstractDevice device) {
    if (!removingAll) {
      devices.remove(device);
      Platform.runLater(() -> accordion.getPanes().remove(paneMap.get(device)));
    }
  }

}
