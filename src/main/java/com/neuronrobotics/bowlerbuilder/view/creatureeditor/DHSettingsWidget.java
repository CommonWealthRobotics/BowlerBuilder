package com.neuronrobotics.bowlerbuilder.view.creatureeditor;

import com.neuronrobotics.bowlerbuilder.view.cadengine.EngineeringUnitsChangeListener;
import com.neuronrobotics.bowlerbuilder.view.cadengine.EngineeringUnitsSliderWidget;
import com.neuronrobotics.sdk.addons.kinematics.DHLink;
import com.neuronrobotics.sdk.addons.kinematics.DHParameterKinematics;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javax.annotation.Nonnull;

public class DHSettingsWidget implements EngineeringUnitsChangeListener {

  private final VBox vBox;
  private final EngineeringUnitsChangeListener externalListener;
  private final DHLink dhLink;
  private final EngineeringUnitsSliderWidget delta;
  private final EngineeringUnitsSliderWidget theta;
  private final EngineeringUnitsSliderWidget alpha;
  private final EngineeringUnitsSliderWidget radius;
  private final DHParameterKinematics device2;

  public DHSettingsWidget(@Nonnull final String title, @Nonnull final DHLink dhLink,
      @Nonnull final DHParameterKinematics device2,
      @Nonnull final EngineeringUnitsChangeListener externalListener) {
    this.dhLink = dhLink;
    this.device2 = device2;
    this.externalListener = externalListener;

    final Label titleLabel = new Label(title);
    titleLabel.setFont(Font.font(16));
    titleLabel.setPadding(new Insets(5));
    vBox = new VBox(5);
    vBox.getChildren().add(titleLabel);

    delta = new EngineeringUnitsSliderWidget(this,
        0,
        200,
        dhLink.getDelta(),
        180, "mm");

    theta = new EngineeringUnitsSliderWidget(this,
        -180,
        180,
        Math.toDegrees(dhLink.getTheta()),
        180, "degrees");

    radius = new EngineeringUnitsSliderWidget(this,
        0,
        200,
        dhLink.getRadius(),
        180, "mm");

    alpha = new EngineeringUnitsSliderWidget(this,
        -180,
        180,
        Math.toDegrees(dhLink.getAlpha()),
        180, "degrees");

    final GridPane gridPane = new GridPane();
    gridPane.setAlignment(Pos.CENTER_LEFT);
    gridPane.setPadding(new Insets(5));

    gridPane.add(getSliderLabel("Delta (Height)"), 0, 0);
    gridPane.add(delta, 1, 0);

    gridPane.add(getSliderLabel("Radius (Length)"), 0, 1);
    gridPane.add(radius, 1, 1);

    gridPane.add(getSliderLabel("Theta"), 0, 2);
    gridPane.add(theta, 1, 2);

    gridPane.add(getSliderLabel("Alpha"), 0, 3);
    gridPane.add(alpha, 1, 3);

    vBox.getChildren().add(gridPane);
  }

  private Label getSliderLabel(@Nonnull final String text) {
    final Label out = new Label(text);
    out.setPadding(new Insets(0, 5, 0, 0));
    return out;
  }

  @Override
  public void onSliderMoving(final EngineeringUnitsSliderWidget source,
      final double newAngleDegrees) {
    dhLink.setTheta(Math.toRadians(theta.getValue()));
    dhLink.setAlpha(Math.toRadians(alpha.getValue()));
    dhLink.setRadius(radius.getValue());
    dhLink.setDelta(delta.getValue());

    if (externalListener != null) {
      externalListener.onSliderMoving(source, newAngleDegrees);
    }

    //this calls the render update function attached as the on joint space update
    final double[] joint = device2.getCurrentJointSpaceVector();
    device2.getChain().getChain(joint);
    Platform.runLater(() -> device2.onJointSpaceUpdate(device2, joint));
  }

  @Override
  public void onSliderDoneMoving(final EngineeringUnitsSliderWidget source,
      final double newAngleDegrees) {
    if (externalListener != null) {
      externalListener.onSliderDoneMoving(source, newAngleDegrees);
    }
  }

  public Node getView() {
    return vBox;
  }

}
