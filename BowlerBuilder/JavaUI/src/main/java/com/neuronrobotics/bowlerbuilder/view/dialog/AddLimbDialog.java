/*
 * Copyright 2017 Ryan Benasutti
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.bowlerbuilder.view.dialog;

import com.google.common.primitives.Ints;
import com.neuronrobotics.bowlerbuilder.model.LimbData;
import com.neuronrobotics.bowlerbuilder.view.dialog.util.ValidatedTextField;
import java.util.Set;
import java.util.stream.Collectors;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.HPos;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javax.annotation.ParametersAreNonnullByDefault;
import kotlin.Pair;

@ParametersAreNonnullByDefault
public class AddLimbDialog extends Dialog<LimbData> {

  private final Set<Integer> takenChannels;
  private final ObservableList<ValidatedTextField> hwIndexFields;

  /**
   * A {@link Dialog} to add a limb to a creature.
   *
   * @param name limb name
   * @param numberOfHWIndices number of links to add
   * @param takenChannels the hardware channels in use
   */
  public AddLimbDialog(
      final String name, final Integer numberOfHWIndices, final Set<Integer> takenChannels) {
    super();

    if (numberOfHWIndices <= 0) {
      throw new IllegalArgumentException("Must have at least one hardware index available.");
    }

    this.takenChannels = takenChannels;
    hwIndexFields = FXCollections.observableArrayList();

    setTitle("Add Link");

    final Label linkNameLabel = new Label("Link name");
    GridPane.setHalignment(linkNameLabel, HPos.RIGHT);

    final ValidatedTextField linkNameField =
        new ValidatedTextField("Link name cannot be empty", text -> !text.isEmpty());
    linkNameField.setText(name);
    linkNameField.setId("linkNameField");
    GridPane.setHalignment(linkNameField, HPos.LEFT);

    final GridPane content = new GridPane();
    content.setHgap(5);
    content.setVgap(5);

    content.add(linkNameLabel, 0, 0);
    content.add(linkNameField, 1, 0);
    for (int i = 1; i <= numberOfHWIndices; i++) {
      final Pair<Label, ValidatedTextField> pair = getField("hwIndexField" + i);
      hwIndexFields.add(pair.getSecond());

      content.add(pair.getFirst(), 0, i);
      content.add(pair.getSecond(), 1, i);
    }

    getDialogPane().setContent(content);
    getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

    // Have to OR each binding together because binding updating is lazy
    // Creating a binding on the list will not be updated by updating a list element'scale
    // property
    // So OR each one together in a loop
    BooleanBinding binding = null;
    for (final ValidatedTextField field : hwIndexFields) {
      if (binding == null) {
        binding = Bindings.createBooleanBinding(field::isInvalid, field.invalidProperty());
      } else {
        binding = Bindings.or(binding, field.invalidProperty());
      }
    }

    final Button okButton = (Button) getDialogPane().lookupButton(ButtonType.OK);
    okButton.disableProperty().bind(Bindings.or(binding, linkNameField.invalidProperty()));
    okButton.setDefaultButton(true);

    setResultConverter(
        buttonType -> {
          if (buttonType.equals(ButtonType.OK)) {
            return new LimbData(
                linkNameField.getText(),
                hwIndexFields
                    .stream()
                    .map(ValidatedTextField::getText)
                    .map(Integer::parseInt)
                    .collect(Collectors.toList()));
          }

          return null;
        });
  }

  private Pair<Label, ValidatedTextField> getField(final String fieldID) {
    final Label hwIndexLabel = new Label("Hardware index");
    GridPane.setHalignment(hwIndexLabel, HPos.RIGHT);

    final ValidatedTextField hwIndexField =
        new ValidatedTextField(
            "Invalid number",
            text -> {
              final Integer result = Ints.tryParse(text);
              return result != null
                  && !takenChannels.contains(result)
                  && hwIndexFields
                      .stream()
                      .filter(item -> !fieldID.equals(item.getId())) // Filter out
                      // ourselves
                      .map(ValidatedTextField::getText) // Map to text content
                      .filter(index -> !index.isEmpty()) // Integer.parseInt
                      // can'translate
                      // handle an empty
                      // string
                      .map(Integer::parseInt)
                      .noneMatch(item -> item.equals(Integer.parseInt(text)));
            });

    hwIndexField.setId(fieldID);
    GridPane.setHalignment(hwIndexField, HPos.LEFT);
    return new Pair<>(hwIndexLabel, hwIndexField);
  }
}
