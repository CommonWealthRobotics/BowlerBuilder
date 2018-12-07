/*
 * Copyright 2017 Ryan Benasutti
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.bowlerbuilder.view.creatureeditor;

import com.google.common.base.Throwables;
import com.google.common.primitives.Floats;
import com.neuronrobotics.bowlerbuilder.LoggerUtilities;
import com.neuronrobotics.bowlerstudio.assets.AssetFactory;
import com.neuronrobotics.sdk.addons.kinematics.DHParameterKinematics;
import com.neuronrobotics.sdk.addons.kinematics.math.TransformNR;
import com.neuronrobotics.sdk.util.ThreadUtil;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class JogWidget {

  private static final Logger LOGGER = LoggerUtilities.getLogger(JogWidget.class.getSimpleName());
  private final VBox view;
  private final GridPane controlPane;
  private final DHParameterKinematics limb;
  private final JogThread jogThread;
  private final BooleanProperty jogThreadRunning;

  /**
   * A widget to move a limb around.
   *
   * @param limb the limb to move
   */
  public JogWidget(final DHParameterKinematics limb) {
    view = new VBox(5);
    controlPane = new GridPane();
    this.limb = limb;
    jogThread = new JogThread();

    jogThreadRunning = new SimpleBooleanProperty(false);
    jogThreadRunning.addListener(
        (observable, oldValue, newValue) -> {
          if (newValue) {
            try {
              jogThread.start();
            } catch (final IllegalThreadStateException ignored) {
              LOGGER.finer("Tried to start jog thread which is already started.");
            }
          }
        });

    view.setPadding(new Insets(5));
    controlPane.setHgap(5);
    controlPane.setVgap(5);

    final Label title = new Label(limb.getScriptingName());
    title.setFont(Font.font(16));
    view.getChildren().add(title);

    final TextField increment = new TextField("0.4");
    increment.setMinWidth(Region.USE_COMPUTED_SIZE);

    final HBox speedBox = new HBox(2, increment, new Label("m/scale"));
    speedBox.setAlignment(Pos.CENTER_LEFT);
    HBox.setHgrow(speedBox, Priority.NEVER);

    final TextField time = new TextField("0.03");
    time.setMinWidth(Region.USE_COMPUTED_SIZE);
    final HBox timeBox = new HBox(2, time, new Label("sec"));
    timeBox.setAlignment(Pos.CENTER_LEFT);
    HBox.setHgrow(timeBox, Priority.NEVER);

    final Button plusX = addButton(AssetFactory.loadIcon("Plus-X.png"), "", 1, 0);
    plusX.setOnMousePressed(
        event -> {
          jogThread.setxStep(Double.parseDouble(increment.getText()));
          jogThread.setTimeStep(Double.parseDouble(time.getText()));
        });
    plusX.setOnMouseReleased(
        event -> {
          jogThread.setxStep(0);
          jogThread.setTimeStep(Double.parseDouble(time.getText()));
        });
    final Button minusX = addButton(AssetFactory.loadIcon("Minus-X.png"), "", 1, 2);
    minusX.setOnMousePressed(
        event -> {
          jogThread.setxStep(-Double.parseDouble(increment.getText()));
          jogThread.setTimeStep(Double.parseDouble(time.getText()));
        });
    minusX.setOnMouseReleased(
        event -> {
          jogThread.setxStep(0);
          jogThread.setTimeStep(Double.parseDouble(time.getText()));
        });

    final Button plusY = addButton(AssetFactory.loadIcon("Plus-Y.png"), "", 0, 1);
    plusY.setOnMousePressed(
        event -> {
          jogThread.setyStep(Double.parseDouble(increment.getText()));
          jogThread.setTimeStep(Double.parseDouble(time.getText()));
        });
    plusY.setOnMouseReleased(
        event -> {
          jogThread.setyStep(0);
          jogThread.setTimeStep(Double.parseDouble(time.getText()));
        });
    final Button minusY = addButton(AssetFactory.loadIcon("Minus-Y.png"), "", 2, 1);
    minusY.setOnMousePressed(
        event -> {
          jogThread.setyStep(-Double.parseDouble(increment.getText()));
          jogThread.setTimeStep(Double.parseDouble(time.getText()));
        });
    minusY.setOnMouseReleased(
        event -> {
          jogThread.setyStep(0);
          jogThread.setTimeStep(Double.parseDouble(time.getText()));
        });

    final Button plusZ = addButton(AssetFactory.loadIcon("Plus-Z.png"), "", 0, 0);
    plusZ.setOnMousePressed(
        event -> {
          jogThread.setzStep(Double.parseDouble(increment.getText()));
          jogThread.setTimeStep(Double.parseDouble(time.getText()));
        });
    plusZ.setOnMouseReleased(
        event -> {
          jogThread.setzStep(0);
          jogThread.setTimeStep(Double.parseDouble(time.getText()));
        });
    final Button minusZ = addButton(AssetFactory.loadIcon("Minus-Z.png"), "", 2, 0);
    minusZ.setOnMousePressed(
        event -> {
          jogThread.setzStep(-Double.parseDouble(increment.getText()));
          jogThread.setTimeStep(Double.parseDouble(time.getText()));
        });
    minusZ.setOnMouseReleased(
        event -> {
          jogThread.setzStep(0);
          jogThread.setTimeStep(Double.parseDouble(time.getText()));
        });

    final Button home = addButton(AssetFactory.loadIcon("Home.png"), "", 1, 1);
    home.setOnAction(
        event -> {
          try {
            limb.setDesiredTaskSpaceTransform(limb.calcHome(), 0);
          } catch (final Exception e) {
            LOGGER.log(
                Level.WARNING,
                "Could not set task space transform.\n" + Throwables.getStackTraceAsString(e));
          }
        });

    /*Button addJoystick = addButton(AssetFactory.loadIcon("Add-Game-Controller.png"), "", 3, 0);
    Button configureJoystick = addButton(AssetFactory.loadIcon("Configure-Game-Controller.png"),
        "", 3, 1);*/

    view.getChildren().add(controlPane);

    final HBox controlsBox = new HBox(5, speedBox, timeBox);
    HBox.setHgrow(controlsBox, Priority.NEVER);
    view.getChildren().add(controlsBox);

    // Home so the limb is in the position it appears to be in when it first generates
    jogThread.setHome(true);
  }

  /**
   * Add a button to the GridPane and return it.
   *
   * @param icon Graphic
   * @param text Text
   * @param xCol GridPane x coordinate
   * @param yCol GridPane y coordinate
   * @return Button
   */
  private Button addButton(
      final ImageView icon, final String text, final Integer xCol, final Integer yCol) {
    final Button out = new Button(text);
    out.setGraphic(icon);
    controlPane.add(out, xCol, yCol);
    return out;
  }

  private Optional<Float> validateAsFloatingPoint(final String rawNumber) {
    return Optional.ofNullable(Floats.tryParse(rawNumber));
  }

  public Node getView() {
    return view;
  }

  public Boolean isJogThreadRunning() {
    return jogThreadRunning.get();
  }

  public BooleanProperty jogThreadRunningProperty() {
    return jogThreadRunning;
  }

  private class JogThread extends Thread {

    private double xStep;
    private double yStep;
    private double zStep;
    private double timeStep = 0.4;
    private boolean home = true;

    public JogThread() {
      super();
    }

    @Override
    public void run() {
      while (true) {
        if (jogThreadRunning.get()) {
          if (home) {
            try {
              xStep = 0;
              yStep = 0;
              zStep = 0;
              home = false;
              limb.setDesiredTaskSpaceTransform(limb.calcHome(), 0);
            } catch (final Exception e) {
              home = false;
              LOGGER.log(
                  Level.WARNING,
                  "Could not set task space transform while homing.\n" + Throwables.getStackTraceAsString(e));
            }
          } else {
            final TransformNR current = limb.getCurrentPoseTarget().copy();

            current.translateX(xStep);
            current.translateY(yStep);
            current.translateZ(zStep);

            if (xStep != 0 || yStep != 0 || zStep != 0) {
                try {
                    limb.setDesiredTaskSpaceTransform(current, timeStep);
                } catch (final Exception e) {
                    LOGGER.log(
                            Level.WARNING,
                            "Could not set task space transform while moving.\n" + Throwables.getStackTraceAsString(e));
                }
            }
          }

          ThreadUtil.wait((int) (timeStep * 1000));
        } else {
          ThreadUtil.wait(100);
        }
      }
    }

    public void setxStep(final double xStep) {
      this.xStep = xStep;
    }

    public void setyStep(final double yStep) {
      this.yStep = yStep;
    }

    public void setzStep(final double zStep) {
      this.zStep = zStep;
    }

    public void setTimeStep(final double timeStep) {
      this.timeStep = timeStep;
    }

    public void setHome(final boolean home) {
      this.home = home;
    }
  }
}
