package com.neuronrobotics.bowlerbuilder.view.robotmanager;

import com.google.common.primitives.Floats;
import com.neuronrobotics.bowlerstudio.assets.AssetFactory;
import com.neuronrobotics.sdk.addons.kinematics.DHParameterKinematics;
import com.neuronrobotics.sdk.addons.kinematics.math.TransformNR;
import java.util.Optional;
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

public class JogWidget {

  private final VBox view;
  private final GridPane controlPane;
  private final Button plusX;
  private final Button minusX;
  private final Button plusY;
  private final Button minusY;
  private final Button plusZ;
  private final Button minusZ;
  private final Button home;
  private final Button addJoystick;
  private final Button configureJoystick;
  private final TextField speed;
  private final TextField time;

  private DHParameterKinematics limb;

  public JogWidget(DHParameterKinematics limb) {
    this.limb = limb;

    view = new VBox(5);
    controlPane = new GridPane();

    view.setPadding(new Insets(5));
    controlPane.setHgap(5);
    controlPane.setVgap(5);

    plusX = addButton(AssetFactory.loadIcon("Plus-X.png"), "", 1, 0);
    minusX = addButton(AssetFactory.loadIcon("Minus-X.png"), "", 1, 2);

    plusY = addButton(AssetFactory.loadIcon("Plus-Y.png"), "", 0, 1);
    minusY = addButton(AssetFactory.loadIcon("Minus-Y.png"), "", 2, 1);

    plusZ = addButton(AssetFactory.loadIcon("Plus-Z.png"), "", 0, 0);
    plusZ.setOnAction(event -> {
      TransformNR current = limb.getCurrentPoseTarget();
      current.translateZ(25);
      try {
        limb.setDesiredTaskSpaceTransform(current, 2);
      } catch (Exception e) {
        e.printStackTrace();
      }
    });
    minusZ = addButton(AssetFactory.loadIcon("Minus-Z.png"), "", 2, 0);

    home = addButton(AssetFactory.loadIcon("Home.png"), "", 1, 1);

    addJoystick = addButton(AssetFactory.loadIcon("Add-Game-Controller.png"), "", 3, 0);
    configureJoystick = addButton(AssetFactory.loadIcon("Configure-Game-Controller.png"),
        "", 3, 1);

    view.getChildren().add(controlPane);

    speed = new TextField("0.4");
    speed.setMinWidth(Region.USE_COMPUTED_SIZE);

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

    HBox speedBox = new HBox(2, speed, new Label("m/s"));
    speedBox.setAlignment(Pos.CENTER_LEFT);
    HBox.setHgrow(speedBox, Priority.NEVER);

    time = new TextField("0.03");
    time.setMinWidth(Region.USE_COMPUTED_SIZE);
    HBox timeBox = new HBox(2, time, new Label("sec"));
    timeBox.setAlignment(Pos.CENTER_LEFT);
    HBox.setHgrow(timeBox, Priority.NEVER);

    HBox controlsBox = new HBox(5, speedBox, timeBox);
    HBox.setHgrow(controlsBox, Priority.NEVER);
    view.getChildren().add(controlsBox);
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
