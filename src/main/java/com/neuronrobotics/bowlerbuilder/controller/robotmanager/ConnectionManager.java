package com.neuronrobotics.bowlerbuilder.controller.robotmanager;

import com.google.inject.Singleton;
import com.neuronrobotics.bowlerbuilder.FxUtil;
import com.neuronrobotics.bowlerstudio.assets.AssetFactory;
import com.neuronrobotics.sdk.common.BowlerAbstractDevice;
import com.neuronrobotics.sdk.common.DeviceManager;
import com.neuronrobotics.sdk.common.IDeviceAddedListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javafx.geometry.Insets;
import javafx.scene.control.Accordion;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.HBox;

@Singleton
public class ConnectionManager implements IDeviceAddedListener {

  private final List<BowlerAbstractDevice> devices;
  private final Accordion accordion;
  private final Map<BowlerAbstractDevice, TitledPane> paneMap;

  public ConnectionManager(HBox connectionsHeader, Accordion accordion) {
    devices = new ArrayList<>();
    this.accordion = accordion;
    paneMap = new HashMap<>();

    Button disconnectAll = new Button("Disconnect All");
    disconnectAll.setGraphic(AssetFactory.loadIcon("Disconnect-All.png"));
    disconnectAll.setOnAction(event -> devices.forEach(device -> {
      if (device.isAvailable()) {
        device.disconnect();
      }

      DeviceManager.remove(device);
    }));

    FxUtil.runFX(() -> {
      connectionsHeader.setPadding(new Insets(5));
      connectionsHeader.setSpacing(5);
      connectionsHeader.getChildren().addAll(AssetFactory.loadIcon("Connected-Devices.png"),
          new Label("Connected Devices"), disconnectAll);
    });

    DeviceManager.addDeviceAddedListener(this);
  }

  @Override
  public void onNewDeviceAdded(BowlerAbstractDevice device) {
    devices.add(device);

    TitledPane pane = new TitledPane();
    pane.setText(device.getScriptingName());

    HBox content = new HBox();

    Button disconnect = new Button("Disconnect " + device.getScriptingName());
    disconnect.setGraphic(AssetFactory.loadIcon("Disconnect-Device.png"));

    content.getChildren().add(disconnect);
    pane.setContent(content);

    paneMap.put(device, pane);
    FxUtil.runFX(() -> accordion.getPanes().add(pane));
  }

  @Override
  public void onDeviceRemoved(BowlerAbstractDevice device) {
    devices.remove(device);
    FxUtil.runFX(() -> accordion.getPanes().remove(paneMap.get(device)));
  }

}
