/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package com.neuronrobotics.bowlerbuilder.controller.robotmanager;

import com.google.inject.Inject;
import com.neuronrobotics.bowlerbuilder.model.LinkData;
import com.neuronrobotics.sdk.addons.kinematics.DHLink;
import com.neuronrobotics.sdk.addons.kinematics.DHParameterKinematics;
import com.neuronrobotics.sdk.addons.kinematics.LinkConfiguration;
import com.neuronrobotics.sdk.addons.kinematics.MobileBase;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class LimbLinkLayoutController extends LimbLayoutController {

  private final ObjectProperty<Optional<LinkData>> linkSelection;

  @Inject
  public LimbLinkLayoutController(final MobileBase device) {
    super(device);
    linkSelection = new SimpleObjectProperty<>();
  }

  @Override
  protected void addButtons(final HBox hBox, final List<DHParameterKinematics> limbs) {
    hBox.getChildren()
        .addAll(
            limbs
                .stream()
                .map(
                    limb -> {
                      final Button limbButton = new Button(limb.getScriptingName());
                      limbButton.setOnAction(event -> limbSelection.set(Optional.of(limb)));

                      final VBox content = new VBox(5, limbButton);
                      content.setAlignment(Pos.CENTER);

                      final HBox hBoxInner = new HBox(5);
                      final List<DHLink> links = limb.getChain().getLinks();
                      for (int i = 0; i < links.size(); i++) {
                        final DHLink link = links.get(i);
                        final LinkConfiguration configuration = limb.getLinkConfiguration(i);
                        final Integer finalI = i; // Needed for lambda

                        final Button linkButton = new Button(configuration.getName());
                        linkButton.setOnAction(
                            event -> {
                              linkSelection.set(
                                  Optional.of(
                                      new LinkData(limb, finalI, link, configuration, limb)));
                              limbSelection.set(Optional.empty());
                            });
                        hBoxInner.getChildren().add(linkButton);
                      }

                      content.getChildren().add(hBoxInner);
                      return content;
                    })
                .collect(Collectors.toList()));
  }

  public Optional<LinkData> getLinkSelection() {
    return linkSelection.get();
  }

  public ObjectProperty<Optional<LinkData>> linkSelectionProperty() {
    return linkSelection;
  }
}
