package com.neuronrobotics.bowlerbuilder.view.dialog;

import com.google.common.primitives.Ints;
import com.neuronrobotics.bowlerbuilder.model.LimbData;
import com.neuronrobotics.bowlerbuilder.view.dialog.util.ValidatedTextField;
import java.util.Set;
import java.util.stream.Collectors;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.HPos;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.util.Pair;

public class AddLimbDialog extends Dialog<LimbData> {

  private final Set<Integer> takenChannels;
  private final ObservableList<ValidatedTextField> hwIndexFields;

  public AddLimbDialog(String name, Integer numberOfHWIndices, Set<Integer> takenChannels) {
    super();
    this.takenChannels = takenChannels;
    hwIndexFields = FXCollections.observableArrayList();

    setTitle("Add Link");

    Label linkNameLabel = new Label("Link name");
    GridPane.setHalignment(linkNameLabel, HPos.RIGHT);
    TextField linkNameField = new TextField(name);
    GridPane.setHalignment(linkNameField, HPos.LEFT);

    GridPane content = new GridPane();
    content.setHgap(5);
    content.setVgap(5);

    content.add(linkNameLabel, 0, 0);
    content.add(linkNameField, 1, 0);
    for (int i = 1; i <= numberOfHWIndices; i++) {
      Pair<Label, ValidatedTextField> pair = getField();
      hwIndexFields.add(pair.getValue());

      content.add(pair.getKey(), 0, i);
      content.add(pair.getValue(), 1, i);
    }

    getDialogPane().setContent(content);
    getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

    Button okButton = (Button) getDialogPane().lookupButton(ButtonType.OK);
    okButton.disableProperty().bind(Bindings.createBooleanBinding(() -> hwIndexFields.stream()
        .anyMatch(ValidatedTextField::isInvalid), hwIndexFields));
    okButton.setDefaultButton(true);

    setResultConverter(buttonType -> {
      if (buttonType.equals(ButtonType.OK)) {
        return new LimbData(linkNameField.getText(),
            hwIndexFields.stream()
                .map(ValidatedTextField::getText)
                .map(Integer::parseInt)
                .collect(Collectors.toList()));
      }

      return null;
    });
  }

  private Pair<Label, ValidatedTextField> getField() {
    Label hwIndexLabel = new Label("Hardware index");
    GridPane.setHalignment(hwIndexLabel, HPos.RIGHT);
    ValidatedTextField hwIndexField = new ValidatedTextField("Invalid number",
        text -> {
          Integer result = Ints.tryParse(text);
          return result != null && !takenChannels.contains(result) && hwIndexFields.stream()
              .map(ValidatedTextField::getText)
              .filter(index -> !index.isEmpty())
              .map(Integer::parseInt)
              .collect(Collectors.toList())
              .contains(result);
        });
    GridPane.setHalignment(hwIndexField, HPos.LEFT);
    return new Pair<>(hwIndexLabel, hwIndexField);
  }

}
