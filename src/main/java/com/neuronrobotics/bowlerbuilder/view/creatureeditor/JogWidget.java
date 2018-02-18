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

public class JogWidget {

  private static final Logger logger =
      LoggerUtilities.getLogger(JogWidget.class.getSimpleName());
  private final VBox view;
  private final GridPane controlPane;
  private final DHParameterKinematics limb;
  private final JogThread jogThread;

  public JogWidget(DHParameterKinematics limb) {
    view = new VBox(5);
    controlPane = new GridPane();
    this.limb = limb;
    jogThread = new JogThread();
    jogThread.start();

    view.setPadding(new Insets(5));
    controlPane.setHgap(5);
    controlPane.setVgap(5);

    Label title = new Label(limb.getScriptingName());
    title.setFont(Font.font(16));
    view.getChildren().add(title);

    TextField increment = new TextField("0.4");
    increment.setMinWidth(Region.USE_COMPUTED_SIZE);

    /*ValidationSupport validator = new ValidationSupport();
    validator.setValidationDecorator(
        new StyleClassValidationDecoration("text-field-error",
            "text-field-warning"));
    validator.registerValidator(speed, false, (control, value) -> {
      if (value instanceof String) {
        return ValidationResult.fromMessageIf(control,
            "Invalid number",
            Severity.ERROR,
            !validateAsFloatingPoint((String) value).isPresent());
      }

      return ValidationResult.fromMessageIf(control,
          "Invalid number",
          Severity.ERROR,
          false);
    });*/ //TODO: This shrinks the application on bad input for some reason

    HBox speedBox = new HBox(2, increment, new Label("m/s"));
    speedBox.setAlignment(Pos.CENTER_LEFT);
    HBox.setHgrow(speedBox, Priority.NEVER);

    TextField time = new TextField("0.03");
    time.setMinWidth(Region.USE_COMPUTED_SIZE);
    HBox timeBox = new HBox(2, time, new Label("sec"));
    timeBox.setAlignment(Pos.CENTER_LEFT);
    HBox.setHgrow(timeBox, Priority.NEVER);

    Button plusX = addButton(AssetFactory.loadIcon("Plus-X.png"), "", 1, 0);
    plusX.setOnMousePressed(event -> {
      jogThread.setDx(Double.parseDouble(increment.getText()));
      jogThread.setDt(Double.parseDouble(time.getText()));
    });
    plusX.setOnMouseReleased(event -> {
      jogThread.setDx(0);
      jogThread.setDt(Double.parseDouble(time.getText()));
    });
    Button minusX = addButton(AssetFactory.loadIcon("Minus-X.png"), "", 1, 2);

    Button plusY = addButton(AssetFactory.loadIcon("Plus-Y.png"), "", 0, 1);
    Button minusY = addButton(AssetFactory.loadIcon("Minus-Y.png"), "", 2, 1);

    Button plusZ = addButton(AssetFactory.loadIcon("Plus-Z.png"), "", 0, 0);
    Button minusZ = addButton(AssetFactory.loadIcon("Minus-Z.png"), "", 2, 0);

    Button home = addButton(AssetFactory.loadIcon("Home.png"), "", 1, 1);
    home.setOnAction(event -> {
      try {
        limb.setDesiredTaskSpaceTransform(limb.calcHome(), 0);
      } catch (Exception e) {
        logger.log(Level.WARNING, "Could not set task space transform.\n"
            + Throwables.getStackTraceAsString(e));
      }
    });

    Button addJoystick = addButton(AssetFactory.loadIcon("Add-Game-Controller.png"), "", 3, 0);
    Button configureJoystick = addButton(AssetFactory.loadIcon("Configure-Game-Controller.png"),
        "", 3, 1);

    view.getChildren().add(controlPane);

    HBox controlsBox = new HBox(5, speedBox, timeBox);
    HBox.setHgrow(controlsBox, Priority.NEVER);
    view.getChildren().add(controlsBox);

    //Home so the limb is in the position it appears to be in when it first generates
    jogThread.setHome(true);
  }

  private class JogThread extends Thread {

    private double dx = 0, dy = 0, dz = 0, dt = 0.4;
    private boolean home = true;

    public JogThread() {
      super();
    }

    @Override
    public void run() {
      while (true) {
        if (home) {
          try {
            dx = 0;
            dy = 0;
            dz = 0;
            home = false;
            limb.setDesiredTaskSpaceTransform(limb.calcHome(), 0);
          } catch (Exception e) {
            logger.log(Level.WARNING, "Could not set task space transform.\n"
                + Throwables.getStackTraceAsString(e));
          }
        } else {
          TransformNR current = limb.getCurrentPoseTarget().copy();

          current.translateX(dx);
          current.translateY(dy);
          current.translateZ(dz);

          try {
            limb.setDesiredTaskSpaceTransform(current, dt * 1000);
          } catch (Exception e) {
            logger.log(Level.WARNING, "Could not set task space transform.\n"
                + Throwables.getStackTraceAsString(e));
          }
        }

        ThreadUtil.wait((int) (dt * 1000));
      }
    }

    public void setDx(double dx) {
      this.dx = dx;
    }

    public void setDy(double dy) {
      this.dy = dy;
    }

    public void setDz(double dz) {
      this.dz = dz;
    }

    public void setDt(double dt) {
      this.dt = dt;
    }

    public void setHome(boolean home) {
      this.home = home;
    }

  }

  /**
   * Add a button to the GridPane and return it.
   *
   * @param icon Graphic
   * @param text Text
   * @param x GridPane x coordinate
   * @param y GridPane y coordinate
   * @return Button
   */
  private Button addButton(ImageView icon, String text, int x, int y) {
    Button out = new Button(text);
    out.setGraphic(icon);
    controlPane.add(out, x, y);
    return out;
  }

  private Optional<Float> validateAsFloatingPoint(String rawNumber) {
    return Optional.ofNullable(Floats.tryParse(rawNumber));
  }

  public Node getView() {
    return view;
  }

}
