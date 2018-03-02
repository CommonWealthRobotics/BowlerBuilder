package com.neuronrobotics.bowlerbuilder.controller.robotmanager.model.limb;

import com.neuronrobotics.bowlerbuilder.view.creatureeditor.JogWidget;
import com.neuronrobotics.sdk.addons.kinematics.DHParameterKinematics;
import java.util.HashMap;
import java.util.Map;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.layout.VBox;

public class MovementTabLimbSelection extends AbstractLimbSelection {

  //Need static store so only one thread controls a limb at a time
  private static final Map<DHParameterKinematics, JogWidget> JOG_WIDGET_MAP = new HashMap<>();
  private final VBox view;

  public MovementTabLimbSelection(final DHParameterKinematics limb) {
    super(limb);

    view = new VBox(5);

    view.setPadding(new Insets(5));

    if (JOG_WIDGET_MAP.containsKey(limb)) {
      view.getChildren().add(JOG_WIDGET_MAP.get(limb).getView());
    } else {
      final JogWidget jogWidget = new JogWidget(limb);
      JOG_WIDGET_MAP.put(limb, jogWidget);
      view.getChildren().add(jogWidget.getView());
    }
  }

  @Override
  public Node getWidget() {
    return view;
  }

  /**
   * Stop the jog thread if there is a jog widget for the limb.
   *
   * @param kinematics limb
   */
  public void stopJogThread(final DHParameterKinematics kinematics) {
    if (JOG_WIDGET_MAP.containsKey(kinematics)) {
      JOG_WIDGET_MAP.get(kinematics).jogThreadRunningProperty().set(false);
    }
  }

  /**
   * Start the jog thread if there is a jog widget for the limb.
   *
   * @param kinematics limb
   */
  public void startJogThread(final DHParameterKinematics kinematics) {
    if (JOG_WIDGET_MAP.containsKey(kinematics)) {
      JOG_WIDGET_MAP.get(kinematics).jogThreadRunningProperty().set(true);
    }
  }

}
