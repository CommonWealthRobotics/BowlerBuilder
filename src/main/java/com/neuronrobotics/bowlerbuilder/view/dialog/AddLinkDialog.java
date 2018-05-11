/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package com.neuronrobotics.bowlerbuilder.view.dialog;

import com.google.common.primitives.Ints;
import com.neuronrobotics.bowlerbuilder.view.dialog.util.ValidatedTextField;
import java.util.Set;
import javafx.beans.binding.Bindings;
import javafx.geometry.HPos;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class AddLinkDialog extends Dialog<String[]> {

  /**
   * A {@link Dialog} to add a {@link com.neuronrobotics.sdk.addons.kinematics.LinkConfiguration}.
   *
   * @param takenChannels the hardware channels in use
   */
  public AddLinkDialog(@Nonnull final Set<Integer> takenChannels) {
    super();

    setTitle("Add Link");

    final Label linkNameLabel = new Label("Link name");
    GridPane.setHalignment(linkNameLabel, HPos.RIGHT);
    final ValidatedTextField linkNameField =
        new ValidatedTextField("Link name cannot be empty", text -> !text.isEmpty());
    linkNameField.setId("linkNameField");
    GridPane.setHalignment(linkNameField, HPos.LEFT);

    final Label hwIndexLabel = new Label("Hardware index");
    GridPane.setHalignment(hwIndexLabel, HPos.RIGHT);
    final ValidatedTextField hwIndexField =
        new ValidatedTextField(
            "Invalid number",
            text -> {
              final Integer result = Ints.tryParse(text);
              return result != null && !takenChannels.contains(result);
            });
    hwIndexField.setId("hwIndexField");
    GridPane.setHalignment(hwIndexField, HPos.LEFT);

    final GridPane content = new GridPane();
    content.setHgap(5);
    content.setVgap(5);

    content.add(linkNameLabel, 0, 0);
    content.add(linkNameField, 1, 0);
    content.add(hwIndexLabel, 0, 1);
    content.add(hwIndexField, 1, 1);

    getDialogPane().setContent(content);
    getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

    final Button okButton = (Button) getDialogPane().lookupButton(ButtonType.OK);
    okButton
        .disableProperty()
        .bind(Bindings.or(linkNameField.invalidProperty(), hwIndexField.invalidProperty()));
    okButton.setDefaultButton(true);

    setResultConverter(
        buttonType -> {
          if (buttonType.equals(ButtonType.OK)) {
            return new String[] {linkNameField.getText(), hwIndexField.getText()};
          }

          return null;
        });
  }
}
