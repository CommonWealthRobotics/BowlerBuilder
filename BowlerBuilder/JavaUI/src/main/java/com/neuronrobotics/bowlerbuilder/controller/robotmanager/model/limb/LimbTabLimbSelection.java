/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.bowlerbuilder.controller.robotmanager.model.limb;

import com.neuronrobotics.bowlerbuilder.BowlerKernelUtilities;
import com.neuronrobotics.bowlerbuilder.controller.CreatureEditorController;
import com.neuronrobotics.bowlerbuilder.controller.robotmanager.model.link.LimbTabLinkSelection;
import com.neuronrobotics.bowlerbuilder.view.dialog.AddLinkDialog;
import com.neuronrobotics.bowlerstudio.assets.AssetFactory;
import com.neuronrobotics.sdk.addons.kinematics.DHLink;
import com.neuronrobotics.sdk.addons.kinematics.DHParameterKinematics;
import com.neuronrobotics.sdk.addons.kinematics.LinkConfiguration;
import com.neuronrobotics.sdk.addons.kinematics.LinkType;
import com.neuronrobotics.sdk.addons.kinematics.MobileBase;
import java.util.List;
import java.util.stream.Collectors;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class LimbTabLimbSelection extends AbstractLimbSelection {

  private final ObjectProperty<LinkConfiguration> selectionProperty;
  private final VBox view;
  private final AnchorPane widget;
  private final HBox scrollPaneContent;

  /**
   * Limb selection in the script tab.
   *
   * @param device the device the limb is attached to
   * @param limb the limb
   * @param creatureEditorController the controller the tab is in
   */
  public LimbTabLimbSelection(
      final MobileBase device,
      final DHParameterKinematics limb,
      final CreatureEditorController creatureEditorController) {
    super(limb);

    view = new VBox(5);
    selectionProperty = new SimpleObjectProperty<>();
    widget = new AnchorPane();

    view.setPadding(new Insets(5));

    final HBox content = new HBox(5);
    HBox.setHgrow(content, Priority.NEVER);
    content.setAlignment(Pos.CENTER_LEFT);
    content.setPadding(new Insets(5));

    view.getChildren().addAll(getTitleLabel(limb.getScriptingName()), content, widget);

    final ScrollPane scrollPane = new ScrollPane();
    HBox.setHgrow(scrollPane, Priority.ALWAYS);
    scrollPane.setVbarPolicy(ScrollBarPolicy.NEVER);
    scrollPaneContent = new HBox(5);
    HBox.setHgrow(scrollPaneContent, Priority.ALWAYS);

    scrollPaneContent.setPadding(new Insets(5));
    scrollPaneContent.getChildren().addAll(getLinkButtons(limb.getLinkConfigurations()));

    scrollPane.setContent(scrollPaneContent);
    content.getChildren().add(scrollPane);

    selectionProperty.addListener(
        (observable, oldValue, newValue) -> {
          if (newValue != null) {
            widget
                .getChildren()
                .setAll(
                    new LimbTabLinkSelection(null, newValue, limb, this, creatureEditorController)
                        .getWidget()); // NOPMD
          }
        });

    final Button addLink = new Button();
    addLink.setGraphic(AssetFactory.loadIcon("Add-Link.png"));
    addLink.setTooltip(new Tooltip("Add Link"));
    addLink.setOnAction(
        event -> {
          final AddLinkDialog dialog =
              new AddLinkDialog(BowlerKernelUtilities.getTakenHardwareChannels(device));
          dialog
              .showAndWait()
              .ifPresent(
                  result -> {
                    addLinkToLimb(limb, result[0], Integer.parseInt(result[1]));
                    creatureEditorController.regenerateMenus();
                  });
        });

    final Button removeLimbButton = new Button();
    removeLimbButton.setGraphic(AssetFactory.loadIcon("Remove-Limb.png"));
    removeLimbButton.setTooltip(new Tooltip("Remove Limb"));
    removeLimbButton.setOnAction(
        event -> {
          device.getLegs().remove(limb);
          device.getAppendages().remove(limb);
          device.getSteerable().remove(limb);
          device.getDrivable().remove(limb);

          creatureEditorController.clearWidget();
          creatureEditorController.regenerateMenus();
          creatureEditorController.regenCAD();
        });

    final HBox controlsBox = new HBox(5, addLink, removeLimbButton);
    controlsBox.setPadding(new Insets(5));
    HBox.setHgrow(controlsBox, Priority.NEVER);
    content.getChildren().add(controlsBox);
  }

  /**
   * Adds a link to the given limb (side-effects the limb).
   *
   * @param limb limb to add to
   * @param linkName the new link name
   * @param hardwareIndex the new link hardware index
   */
  private void addLinkToLimb(
      final DHParameterKinematics limb, final String linkName, final int hardwareIndex) {
    final LinkConfiguration newLink = new LinkConfiguration();
    final List<LinkConfiguration> linkConfigurations = limb.getFactory().getLinkConfigurations();

    final int numOfLinks = linkConfigurations.size();

    LinkType typeOfLink = linkConfigurations.get(numOfLinks - 1).getTypeEnum();

    if (typeOfLink == null) {
      typeOfLink = LinkType.VIRTUAL;
    }

    newLink.setType(typeOfLink);
    newLink.setTypeString(typeOfLink.toString());
    newLink.setName(linkName);
    newLink.setHardwareIndex(hardwareIndex);
    newLink.setLinkIndex(limb.getNumberOfLinks());

    limb.addNewLink(newLink, new DHLink(0, 0, 100, 0));

    // Add the new link to the ScrollPane for continuity, the rest of the updating is done by
    // regenerating the menus
    scrollPaneContent.getChildren().setAll(getLinkButtons(limb.getLinkConfigurations()));
  }

  private List<Button> getLinkButtons(final List<LinkConfiguration> configs) {
    return configs
        .stream()
        .map(
            config -> {
              final Button linkButton = new Button(config.getName());
              // Set the selection to this link
              linkButton.setOnAction(event -> selectionProperty.set(config));
              return linkButton;
            })
        .collect(Collectors.toList());
  }

  public void clearSelectedWidget() {
    widget.getChildren().clear();
    selectionProperty.set(null);
  }

  public void regenerateLinkButtons() {
    scrollPaneContent.getChildren().setAll(getLinkButtons(limb.getLinkConfigurations()));
  }

  @Nonnull
  @Override
  public Node getWidget() {
    return view;
  }

  public DHParameterKinematics getLimb() {
    return limb;
  }
}
