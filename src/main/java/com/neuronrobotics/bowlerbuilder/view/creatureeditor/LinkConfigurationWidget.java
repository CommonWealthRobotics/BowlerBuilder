/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package com.neuronrobotics.bowlerbuilder.view.creatureeditor;

import com.google.common.base.Throwables;
import com.neuronrobotics.bowlerbuilder.LoggerUtilities;
import com.neuronrobotics.bowlerbuilder.view.cadengine.EngineeringUnitsChangeListener;
import com.neuronrobotics.bowlerbuilder.view.cadengine.EngineeringUnitsSliderWidget;
import com.neuronrobotics.bowlerstudio.scripting.ScriptingEngine;
import com.neuronrobotics.bowlerstudio.vitamins.Vitamins;
import com.neuronrobotics.sdk.addons.kinematics.AbstractLink;
import com.neuronrobotics.sdk.addons.kinematics.LinkConfiguration;
import com.neuronrobotics.sdk.addons.kinematics.LinkFactory;
import com.neuronrobotics.sdk.addons.kinematics.LinkType;
import com.neuronrobotics.sdk.addons.kinematics.math.TransformNR;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.eclipse.jgit.api.errors.TransportException;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;

public class LinkConfigurationWidget extends GridPane {

  private static final Logger LOGGER =
      LoggerUtilities.getLogger(LinkConfigurationWidget.class.getSimpleName());
  private final LinkConfiguration conf;
  private AbstractLink activeLink; //NOPMD

  public LinkConfigurationWidget(@Nonnull final LinkConfiguration configuration,
      @Nonnull final LinkFactory factory,
      @Nullable final EngineeringUnitsSliderWidget setpointSlider) {
    conf = configuration;
    activeLink = factory.getLink(conf);
    getColumnConstraints().add(new ColumnConstraints(150)); // column 1 is 75 wide
    getColumnConstraints().add(new ColumnConstraints(200)); // column 2 is 300 wide
    getColumnConstraints().add(new ColumnConstraints(200)); // column 2 is 300 wide
    setHgap(20);

    final TextField mass = new TextField(getFormatted(conf.getMassKg()));
    mass.setOnAction(event -> {
      conf.setMassKg(Double.parseDouble(mass.getText()));
      activeLink.setTargetEngineeringUnits(0);
      activeLink.flush(0);
    });

    final TransformNR currentCentroid = conf.getCenterOfMassFromCentroid();
    final TextField massx = new TextField(getFormatted(currentCentroid.getX()));
    massx.setOnAction(event -> {
      currentCentroid.setX(Double.parseDouble(massx.getText()));
      conf.setCenterOfMassFromCentroid(currentCentroid);
      activeLink.setTargetEngineeringUnits(0);
      activeLink.flush(0);
    });

    final TextField massy = new TextField(getFormatted(currentCentroid.getY()));
    massy.setOnAction(event -> {
      currentCentroid.setY(Double.parseDouble(massy.getText()));
      conf.setCenterOfMassFromCentroid(currentCentroid);
      activeLink.setTargetEngineeringUnits(0);
      activeLink.flush(0);
    });

    final TextField massz = new TextField(getFormatted(currentCentroid.getZ()));
    massz.setOnAction(event -> {
      currentCentroid.setZ(Double.parseDouble(massz.getText()));
      conf.setCenterOfMassFromCentroid(currentCentroid);
      activeLink.setTargetEngineeringUnits(0);
      activeLink.flush(0);
    });

    final TextField scale = new TextField(getFormatted(conf.getScale()));
    scale.setOnAction(event -> {
      conf.setScale(Double.parseDouble(scale.getText()));
      activeLink.setTargetEngineeringUnits(0);
      activeLink.flush(0);
    });

    final Button editShaft = new Button("Edit " + conf.getShaftSize());
    editShaft.setOnAction(event -> LoggerUtilities.newLoggingThread(LOGGER, () -> {
      try {
        final String type = conf.getShaftType();
        final String sizeID = conf.getShaftSize();
        edit(type, sizeID, Vitamins.getConfiguration(type, sizeID));
      } catch (Exception e) {
        LOGGER.warning("Could not edit Vitamin configuration.\n"
            + Throwables.getStackTraceAsString(e));
      }
    }).start());

    final Button newShaft = new Button("New " + conf.getShaftType());
    newShaft.setOnAction(event -> {
      final TextInputDialog dialog = new TextInputDialog("New Size");
      dialog.setTitle("Wizard for new " + conf.getShaftType());
      dialog.setHeaderText("Enter the Side ID for a new " + conf.getShaftType());
      dialog.setContentText("Size: ");

      // Traditional way to get the response value.
      final Optional<String> result = dialog.showAndWait();
      result.ifPresent(size -> {
        // Create the custom dialog.
        final String type = conf.getShaftType();

        LoggerUtilities.newLoggingThread(LOGGER, () -> {
          try {
            test(type);
            Vitamins.newVitamin(size, type);
            edit(type, size, Vitamins.getConfiguration(type, conf.getShaftSize()));
          } catch (Exception e) {
            LOGGER.warning("Could not set new size.\n" + Throwables.getStackTraceAsString(e));
          }
        }).start();
      });
    });

    final ComboBox<String> shaftSize = new ComboBox<>();
    for (final String s : Vitamins.listVitaminSizes(conf.getShaftType())) {
      shaftSize.getItems().add(s);
    }

    shaftSize.setOnAction(event -> {
      conf.setShaftSize(shaftSize.getSelectionModel().getSelectedItem());
      newShaft.setText("New " + conf.getShaftType());
      editShaft.setText("Edit " + conf.getShaftSize());
    });

    shaftSize.getSelectionModel().select(conf.getShaftSize());

    final ComboBox<String> shaftType = new ComboBox<>();
    for (final String vitaminsType : Vitamins.listVitaminTypes()) {
      final HashMap<String, Object> meta = Vitamins.getMeta(vitaminsType);
      if (meta != null && meta.containsKey("shaft")) {
        shaftType.getItems().add(vitaminsType);
      }
    }

    shaftType.setOnAction(event -> {
      conf.setShaftType(shaftType.getSelectionModel().getSelectedItem());
      shaftSize.getItems().clear();

      for (final String s : Vitamins.listVitaminSizes(conf.getShaftType())) {
        shaftSize.getItems().add(s);
      }

      newShaft.setText("New " + conf.getShaftType());
      editShaft.setText("Edit " + conf.getShaftSize());
    });

    shaftType.getSelectionModel().select(conf.getShaftType());

    // Actuator editing
    final Button editHardware = new Button("Edit " + conf.getElectroMechanicalSize());
    editHardware.setOnAction(event -> LoggerUtilities.newLoggingThread(LOGGER, () -> {
      try {
        final String type = conf.getElectroMechanicalType();
        final String sizeID = conf.getElectroMechanicalSize();
        edit(type, sizeID, Vitamins.getConfiguration(type, sizeID));
      } catch (Exception e) {
        LOGGER.log(Level.WARNING, "Could not edit Vitamin configuration.\n"
            + Throwables.getStackTraceAsString(e));
      }
    }).start());
    final Button newHardware = new Button("New " + conf.getElectroMechanicalType());
    newHardware.setOnAction(event -> {
      final TextInputDialog dialog = new TextInputDialog("New Size");
      dialog.setTitle("Wizard for new " + conf.getElectroMechanicalType());
      dialog.setHeaderText("Enter the Side ID for a new " + conf.getElectroMechanicalType());
      dialog.setContentText("Size: ");

      // Traditional way to get the response value.
      final Optional<String> result = dialog.showAndWait();
      result.ifPresent(size -> {
        // Create the custom dialog.
        final String type = conf.getElectroMechanicalType();
        LoggerUtilities.newLoggingThread(LOGGER, () -> {
          try {
            test(type);
            Vitamins.newVitamin(size, type);
            edit(type, size, Vitamins.getConfiguration(type, conf.getElectroMechanicalSize()));
          } catch (Exception e) {
            LOGGER.warning("Could not set new size.\n" + Throwables.getStackTraceAsString(e));
          }
        }).start();
      });
    });

    final ComboBox<String> emHardwareSize = new ComboBox<>();
    for (final String s : Vitamins.listVitaminSizes(conf.getElectroMechanicalType())) {
      emHardwareSize.getItems().add(s);
    }
    emHardwareSize.setOnAction(event -> {
      conf.setElectroMechanicalSize(emHardwareSize.getSelectionModel().getSelectedItem());
      newHardware.setText("New " + conf.getElectroMechanicalType());
      editHardware.setText("Edit " + conf.getElectroMechanicalSize());
    });
    emHardwareSize.getSelectionModel().select(conf.getElectroMechanicalSize());

    final ComboBox<String> emHardwareType = new ComboBox<>();
    for (final String vitaminsType : Vitamins.listVitaminTypes()) {
      final HashMap<String, Object> meta = Vitamins.getMeta(vitaminsType);
      if (meta != null && meta.containsKey("actuator")) {
        emHardwareType.getItems().add(vitaminsType);
      }
    }
    emHardwareType.setOnAction(event -> {
      conf.setElectroMechanicalType(emHardwareType.getSelectionModel().getSelectedItem());
      emHardwareSize.getItems().clear();
      for (final String s : Vitamins.listVitaminSizes(conf.getElectroMechanicalType())) {
        emHardwareSize.getItems().add(s);
      }
      newHardware.setText("New " + conf.getElectroMechanicalType());
      editHardware.setText("Edit " + conf.getElectroMechanicalSize());
    });
    emHardwareType.getSelectionModel().select(conf.getElectroMechanicalType());

    final TextField deviceName = new TextField(configuration.getDeviceScriptingName());
    deviceName.setOnAction(event -> {
      conf.setDeviceScriptingName(deviceName.getText());
      factory.refreshHardwareLayer(conf);
      activeLink = factory.getLink(conf);
      LOGGER.log(Level.INFO, "Link device to " + conf.getDeviceScriptingName());
    });

    add(new Text("Scale To Degrees "), 0, 0);
    add(scale, 1, 0);
    add(new Text("(unitless)"), 2, 0);

    final EngineeringUnitsSliderWidget lowerBound = new EngineeringUnitsSliderWidget(
        new EngineeringUnitsChangeListener() {

          @Override
          public void onSliderMoving(final EngineeringUnitsSliderWidget source,
              final double newAngleDegrees) {
            conf.setLowerLimit(newAngleDegrees);
            final double eng;
            if (conf.getScale() > 0) {
              eng = activeLink.getMinEngineeringUnits();
            } else {
              eng = activeLink.getMaxEngineeringUnits();
            }

            activeLink.setTargetEngineeringUnits(eng);
            activeLink.flush(0);

            if (setpointSlider != null) {
              setpointSlider.setLowerBound(eng);
            }
          }

          @Override
          public void onSliderDoneMoving(final EngineeringUnitsSliderWidget source,
              final double newAngleDegrees) {
            activeLink.setTargetEngineeringUnits(0);
            activeLink.flush(0);
          }
        }, 0, 255, conf.getLowerLimit(), 150, "device units", true);

    final EngineeringUnitsSliderWidget upperBound = new EngineeringUnitsSliderWidget(
        new EngineeringUnitsChangeListener() {
          @Override
          public void onSliderMoving(final EngineeringUnitsSliderWidget source,
              final double newAngleDegrees) {
            conf.setUpperLimit(newAngleDegrees);
            final double eng;
            if (conf.getScale() < 0) {
              eng = activeLink.getMinEngineeringUnits();
            } else {
              eng = activeLink.getMaxEngineeringUnits();
            }

            activeLink.setTargetEngineeringUnits(eng);
            activeLink.flush(0);
            if (setpointSlider != null) {
              setpointSlider.setLowerBound(eng);
            }
          }

          @Override
          public void onSliderDoneMoving(final EngineeringUnitsSliderWidget source,
              final double newAngleDegrees) {
            activeLink.setTargetEngineeringUnits(0);
            activeLink.flush(0);
          }
        }, 0, 255, conf.getUpperLimit(), 150, "device units", true);

    final EngineeringUnitsSliderWidget zero = new EngineeringUnitsSliderWidget(
        new EngineeringUnitsChangeListener() {
          @Override
          public void onSliderMoving(final EngineeringUnitsSliderWidget source,
              final double newAngleDegrees) {
            conf.setStaticOffset(newAngleDegrees);
            activeLink.setTargetEngineeringUnits(0);
            activeLink.flush(0);
          }

          @Override
          public void onSliderDoneMoving(final EngineeringUnitsSliderWidget source,
              final double newAngleDegrees) {
            //Don'translate need ot implement
          }
        }, conf.getLowerLimit(), conf.getUpperLimit(), conf.getStaticOffset(),
        150, "device units", true);

    final ComboBox<String> channel = new ComboBox<>();
    for (int i = 0; i < 24; i++) {
      channel.getItems().add(Integer.toString(i));
    }

    channel.setOnAction(event -> {
      conf.setHardwareIndex(Integer.parseInt(channel.getSelectionModel().getSelectedItem()));
      factory.refreshHardwareLayer(conf);
      activeLink = factory.getLink(conf);
      LOGGER.log(Level.INFO, "Link channel changed to " + conf.getTypeString());
    });

    channel.getSelectionModel().select(conf.getHardwareIndex());

    final ComboBox<String> comboBox = new ComboBox<>();
    for (final LinkType type : LinkType.values()) {
      comboBox.getItems().add(type.getName());
    }

    comboBox.setOnAction(event -> {
      conf.setType(LinkType.fromString(comboBox.getSelectionModel().getSelectedItem()));
      LOGGER.log(Level.INFO, "Link type changed to " + conf.getTypeString());
    });

    comboBox.getSelectionModel().select(conf.getTypeString());

    add(new Text("Zero Degrees Value"), 0, 1);
    add(zero, 1, 1);

    add(new Text("Upper bound"), 0, 2);
    add(upperBound, 1, 2);

    add(new Text("Lower bound"), 0, 3);
    add(lowerBound, 1, 3);

    add(new Text("Link Type"), 0, 4);
    add(comboBox, 1, 4);
    add(new Text("Link Hardware Index"), 0, 5);
    add(channel, 1, 5);

    add(new Text("Device Scripting Name"), 0, 6);
    add(deviceName, 1, 6);

    add(new Text("Mass"), 0, 7);
    add(mass, 1, 7);

    add(new Text("Mass Centroid x"), 0, 8);
    add(massx, 1, 8);

    add(new Text("Mass Centroid y"), 0, 9);
    add(massy, 1, 9);
    add(new Text("Mass Centroid z"), 0, 10);
    add(massz, 1, 10);
    // link hardware
    add(new Text("Hardware Type"), 0, 11);
    add(emHardwareType, 1, 11);
    add(new Text("Hardware Size"), 0, 12);
    add(emHardwareSize, 1, 12);
    add(editHardware, 2, 12);
    add(newHardware, 1, 13);

    // link shaft
    add(new Text("Shaft Type"), 0, 14);
    add(shaftType, 1, 14);
    add(new Text("Shaft Size"), 0, 15);
    add(shaftSize, 1, 15);
    add(editShaft, 2, 15);
    add(newShaft, 1, 16);
  }

  private void test(@Nonnull final String type) throws IOException {
    LOGGER.log(Level.FINEST, "Running test with: " + type);

    try {
      Vitamins.saveDatabase(type);
    } catch (final TransportException e) {
      final GitHub github = ScriptingEngine.getGithub();
      final GHRepository repo = github.getUser("madhephaestus")
          .getRepository("Hardware-Dimensions");
      final GHRepository forked = repo.fork();
      LOGGER.log(Level.FINE, "Vitamins forked to " + forked.getGitTransportUrl());
      Vitamins.setGitRepoDatabase("https://github.com/"
          + github.getMyself().getLogin()
          + "/Hardware-Dimensions.git");
    } catch (final Exception e) {
      LOGGER.warning("Could not save Vitamin database.\n" + Throwables.getStackTraceAsString(e));
    }
  }

  private void edit(@Nonnull final String type, @Nonnull final String vitaminID,
      @Nonnull final Map<String, Object> startingConf)
      throws IOException {
    LOGGER.log(Level.INFO, "Configuration for " + conf.getElectroMechanicalSize());
    LOGGER.log(Level.INFO, "Saving " + vitaminID);

    test(type);

    Platform.runLater(() -> {
      final Alert dialog = new Alert(AlertType.CONFIRMATION);
      dialog.setTitle("Edit Hardware Wizard");
      dialog.setHeaderText("Update the hardare configurations");

      // Create the username and password labels and fields.
      final GridPane grid = new GridPane();
      grid.setHgap(10);
      grid.setVgap(10);
      grid.setPadding(new Insets(20, 150, 10, 10));

      final Map<String, TextField> valueFields = new HashMap<>();

      int row = 0;
      for (final Map.Entry<String, Object> entry : startingConf.entrySet()) {
        final TextField username = new TextField(); //NOPMD
        username.setText(entry.getValue().toString());
        grid.add(new Label(entry.getKey()), 0, row); //NOPMD
        grid.add(username, 1, row);
        valueFields.put(entry.getKey(), username);
        row++;
      }

      dialog.getDialogPane().setContent(grid);
      final Optional<ButtonType> result = dialog.showAndWait();
      if (result.isPresent() && result.get() == ButtonType.OK) {
        LoggerUtilities.newLoggingThread(LOGGER, () -> {
          for (final Entry<String, TextField> entry : valueFields.entrySet()) {
            final String key = entry.getKey();
            final String value = entry.getValue().getText();

            try {
              Vitamins.setParameter(type, vitaminID, key, value);
            } catch (Exception e) {
              LOGGER.warning("Could not set Vitamin parameter with: "
                  + type + ", " + vitaminID + ", " + key + ", " + value + "\n"
                  + Throwables.getStackTraceAsString(e));
            }
          }
          try {
            Vitamins.saveDatabase(type);
          } catch (Exception e) {
            LOGGER.warning("Could not save vitamin database with: " + type + "\n"
                + Throwables.getStackTraceAsString(e));
          }
        }).start();
      }
    });

  }

  private String getFormatted(final double value) {
    return String.format("%4.3f%n", (double) value);
  }

}
