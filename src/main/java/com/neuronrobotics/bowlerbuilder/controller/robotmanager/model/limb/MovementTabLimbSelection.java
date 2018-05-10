/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package com.neuronrobotics.bowlerbuilder.controller.robotmanager.model.limb;

import com.neuronrobotics.bowlerbuilder.view.creatureeditor.JogWidget;
import com.neuronrobotics.sdk.addons.kinematics.DHParameterKinematics;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.layout.VBox;
import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class MovementTabLimbSelection extends AbstractLimbSelection {

  // Need static store so only one thread controls a limb at a time
  private static final Map<DHParameterKinematics, JogWidget> JOG_WIDGET_MAP =
      new ConcurrentHashMap<>();
  private final VBox view;

  /**
   * Limb selection in the movement tab.
   *
   * @param limb the limb
   */
  public MovementTabLimbSelection(final DHParameterKinematics limb) {
    super(limb);

    view = new VBox(5);

    view.setPadding(new Insets(5));

    if (JOG_WIDGET_MAP.containsKey(limb)) {
      view.getChildren().add(JOG_WIDGET_MAP.get(limb).getView());
    } else {
      final JogWidget jogWidget = new JogWidget(limb);
      jogWidget.jogThreadRunningProperty().set(true);
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
