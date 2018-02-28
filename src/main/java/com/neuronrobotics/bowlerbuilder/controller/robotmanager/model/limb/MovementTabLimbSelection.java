package com.neuronrobotics.bowlerbuilder.controller.robotmanager.model.limb;

import com.neuronrobotics.bowlerbuilder.view.creatureeditor.JogWidget;
import com.neuronrobotics.sdk.addons.kinematics.DHParameterKinematics;
import java.util.HashMap;
import java.util.Map;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.layout.VBox;

public class MovementTabLimbSelection extends LimbSelection {

  //Need static store so only one thread controls a limb at a time
  private static final Map<DHParameterKinematics, JogWidget> jogWidgetMap = new HashMap<>();
  private final VBox view;

  public MovementTabLimbSelection(DHParameterKinematics limb) {
    super(limb);

    view = new VBox(5);

    view.setPadding(new Insets(5));

    if (jogWidgetMap.containsKey(limb)) {
      view.getChildren().add(jogWidgetMap.get(limb).getView());
    } else {
      JogWidget jogWidget = new JogWidget(limb);
      jogWidgetMap.put(limb, jogWidget);
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
   * @param dh limb
   */
  public void stopJogThread(DHParameterKinematics dh) {
    if (jogWidgetMap.containsKey(dh)) {
      jogWidgetMap.get(dh).jogThreadRunningProperty().set(false);
    }
  }

  /**
   * Start the jog thread if there is a jog widget for the limb.
   *
   * @param dh limb
   */
  public void startJogThread(DHParameterKinematics dh) {
    if (jogWidgetMap.containsKey(dh)) {
      jogWidgetMap.get(dh).jogThreadRunningProperty().set(true);
    }
  }

}
