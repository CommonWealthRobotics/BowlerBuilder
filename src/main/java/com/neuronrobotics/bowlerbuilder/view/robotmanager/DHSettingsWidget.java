package com.neuronrobotics.bowlerbuilder.view.robotmanager;

import com.neuronrobotics.bowlerbuilder.FxUtil;
import com.neuronrobotics.bowlerbuilder.controller.cadengine.view.EngineeringUnitsChangeListener;
import com.neuronrobotics.bowlerbuilder.controller.cadengine.view.EngineeringUnitsSliderWidget;
import com.neuronrobotics.sdk.addons.kinematics.DHLink;
import com.neuronrobotics.sdk.addons.kinematics.DHParameterKinematics;
import javafx.scene.Group;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;

public class DHSettingsWidget extends Group implements EngineeringUnitsChangeListener {

  private DHLink dhLink;
  private EngineeringUnitsSliderWidget delta;
  private EngineeringUnitsSliderWidget theta;
  private EngineeringUnitsSliderWidget alpha;
  private EngineeringUnitsSliderWidget radius;
  private DHParameterKinematics device2;
  private EngineeringUnitsChangeListener externalListener;

  public DHSettingsWidget(DHLink dhLink, DHParameterKinematics device2,
                          EngineeringUnitsChangeListener externalListener) {
    this.dhLink = dhLink;
    this.device2 = device2;

    this.externalListener = externalListener;

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

    GridPane gridPane = new GridPane();
    gridPane.add(new Text("Delta (Height)"), 0, 0);
    gridPane.add(delta, 1, 0);

    gridPane.add(new Text("Radius (Length)"), 0, 1);
    gridPane.add(radius, 1, 1);

    gridPane.add(new Text("Theta"), 0, 2);
    gridPane.add(theta, 1, 2);

    gridPane.add(new Text("Alpha"), 0, 3);
    gridPane.add(alpha, 1, 3);

    getChildren().add(gridPane);
  }

  @Override
  public void onSliderMoving(EngineeringUnitsSliderWidget source, double newAngleDegrees) {
    dhLink.setTheta(Math.toRadians(theta.getValue()));
    dhLink.setAlpha(Math.toRadians(alpha.getValue()));
    dhLink.setRadius(radius.getValue());
    dhLink.setDelta(delta.getValue());

    if (externalListener != null) {
      externalListener.onSliderMoving(source, newAngleDegrees);
    }

    //this calls the render update function attached as the on joint space update
    double[] joint = device2.getCurrentJointSpaceVector();
    device2.getChain().getChain(joint);
    FxUtil.runFX(() -> device2.onJointSpaceUpdate(device2, joint));
  }

  @Override
  public void onSliderDoneMoving(EngineeringUnitsSliderWidget source, double newAngleDegrees) {
    if (externalListener != null) {
      externalListener.onSliderDoneMoving(source, newAngleDegrees);
    }
  }
}
