/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.bowlerbuilder.controller.robotmanager;

import com.google.common.base.Throwables;
import com.google.inject.Inject;
import com.neuronrobotics.bowlerbuilder.LoggerUtilities;
import com.neuronrobotics.bowlerstudio.assets.AssetFactory;
import com.neuronrobotics.sdk.addons.kinematics.DHParameterKinematics;
import com.neuronrobotics.sdk.addons.kinematics.MobileBase;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class LimbLayoutController {

  private static final Logger LOGGER =
      LoggerUtilities.getLogger(LimbLayoutController.class.getSimpleName());
  protected final MobileBase device;
  final ObjectProperty<Optional<DHParameterKinematics>> limbSelection;
  @FXML private HBox legHBox;
  @FXML private ImageView legIcon;
  @FXML private HBox legPaneHBox;
  @FXML private HBox armHBox;
  @FXML private ImageView armIcon;
  @FXML private HBox armPaneHBox;
  @FXML private HBox steerableHBox;
  @FXML private ImageView steerableIcon;
  @FXML private HBox steerablePaneHBox;
  @FXML private HBox fixedHBox;
  @FXML private ImageView fixedIcon;
  @FXML private HBox fixedPaneHBox;

  @Inject
  public LimbLayoutController(final MobileBase device) {
    this.device = device;
    limbSelection = new SimpleObjectProperty<>();
  }

  @FXML
  protected void initialize() {
    try {
      legIcon.setImage(AssetFactory.loadAsset("Load-Limb-Legs.png"));
      armIcon.setImage(AssetFactory.loadAsset("Load-Limb-Arms.png"));
      steerableIcon.setImage(AssetFactory.loadAsset("Load-Limb-Steerable-Wheels.png"));
      fixedIcon.setImage(AssetFactory.loadAsset("Load-Limb-Fixed-Wheels.png"));
    } catch (final Exception e) {
      LOGGER.warning("Could not load icon assets.\n" + Throwables.getStackTraceAsString(e));
    }

    addButtons(legPaneHBox, device.getLegs());
    addButtons(armPaneHBox, device.getAppendages());
    addButtons(steerablePaneHBox, device.getSteerable());
    addButtons(fixedPaneHBox, device.getDrivable());
  }

  /**
   * Add a button for each limb to the HBox. The button changes the selected limb.
   *
   * @param hBox HBox to add buttons to
   * @param limbs limbs to generate buttons from
   */
  protected void addButtons(final HBox hBox, final List<DHParameterKinematics> limbs) {
    hBox.getChildren()
        .addAll(
            limbs
                .stream()
                .map(
                    limb -> {
                      final Button button = new Button(limb.getScriptingName());
                      button.setOnAction(event -> limbSelection.set(Optional.of(limb)));
                      return button;
                    })
                .collect(Collectors.toList()));
  }

  public void addToLegHBox(final Node node) {
    legHBox.getChildren().add(node);
  }

  public void addToArmHBox(final Node node) {
    armHBox.getChildren().add(node);
  }

  public void addToSteerableHBox(final Node node) {
    steerableHBox.getChildren().add(node);
  }

  public void addToFixedHBox(final Node node) {
    fixedHBox.getChildren().add(node);
  }

  public Optional<DHParameterKinematics> getLimbSelection() {
    return limbSelection.get();
  }

  public ReadOnlyObjectProperty<Optional<DHParameterKinematics>> limbSelectionProperty() {
    return limbSelection;
  }
}
