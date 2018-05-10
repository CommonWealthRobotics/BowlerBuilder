/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package com.neuronrobotics.bowlerbuilder.controller.robotmanager;

import com.google.common.collect.ImmutableList;
import com.google.inject.Singleton;
import com.neuronrobotics.bowlerbuilder.view.cadengine.camera.VirtualCameraDevice;
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

  public ConnectionManager(
      @Nonnull final HBox connectionsHeader, @Nonnull final Accordion accordion) {
    devices = new ArrayList<>();
    this.accordion = accordion;
    paneMap = new HashMap<>();

    final Button disconnectAll = new Button("Disconnect All");
    disconnectAll.setGraphic(AssetFactory.loadIcon("Disconnect-All.png"));
    disconnectAll.setOnAction(
        __ -> {
          final ImmutableList<BowlerAbstractDevice> devicesCopy = ImmutableList.copyOf(devices);
          devicesCopy.forEach(device -> disconnectDevice(accordion, device));
        });

    Platform.runLater(
        () -> {
          connectionsHeader.setPadding(new Insets(5));
          connectionsHeader.setSpacing(5);
          connectionsHeader
              .getChildren()
              .addAll(
                  AssetFactory.loadIcon("Connected-Devices.png"),
                  new Label("Connected Devices"),
                  disconnectAll);
        });

    DeviceManager.addDeviceAddedListener(this);
  }

  @Override
  public void onNewDeviceAdded(@Nonnull final BowlerAbstractDevice device) {
    // Don't add Camera devices
    if (device instanceof VirtualCameraDevice) {
      return;
    }

    devices.add(device);

    final TitledPane pane = new TitledPane();
    pane.setText(device.getScriptingName());

    final Button disconnect = new Button("Disconnect " + device.getScriptingName());
    disconnect.setGraphic(AssetFactory.loadIcon("Disconnect-Device.png"));
    disconnect.setOnAction(__ -> disconnectDevice(accordion, device));

    final HBox content = new HBox(disconnect);
    pane.setContent(content);

    paneMap.put(device, pane);
    Platform.runLater(() -> accordion.getPanes().add(pane));
  }

  @Override
  public void onDeviceRemoved(@Nonnull final BowlerAbstractDevice device) {
    disconnectDevice(accordion, device);
  }

  protected void disconnectDevice(
      @Nonnull final Accordion accordion, @Nonnull final BowlerAbstractDevice device) {
    if (device.isAvailable()) {
      device.disconnect();
    }

    DeviceManager.remove(device);

    // Have to save the pane before the map is cleared because the JavaFx update can happen
    // after
    // the map is cleared
    final TitledPane devicePane = paneMap.get(device);
    Platform.runLater(() -> accordion.getPanes().remove(devicePane));
    devices.remove(device);
    paneMap.remove(device);
  }
}
