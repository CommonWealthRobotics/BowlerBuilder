package com.neuronrobotics.bowlerbuilder.view.dialog;

import com.google.common.primitives.Ints;
import com.neuronrobotics.bowlerbuilder.view.dialog.util.ValidatedTextField;
import java.util.Set;
import javafx.geometry.HPos;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;

public class AddLinkDialog extends Dialog<String[]> {

  public AddLinkDialog(Set<Integer> takenChannels) {
    super();

    setTitle("Add Link");

    Label linkNameLabel = new Label("Link name");
    GridPane.setHalignment(linkNameLabel, HPos.RIGHT);
    TextField linkNameField = new TextField();
    GridPane.setHalignment(linkNameField, HPos.LEFT);

    Label hwIndexLabel = new Label("Hardware index");
    GridPane.setHalignment(hwIndexLabel, HPos.RIGHT);
    ValidatedTextField hwIndexField = new ValidatedTextField("Invalid number",
        text -> {
          Integer result = Ints.tryParse(text);
          return result != null && !takenChannels.contains(result);
        });
    GridPane.setHalignment(hwIndexField, HPos.LEFT);

    GridPane content = new GridPane();
    content.setHgap(5);
    content.setVgap(5);

    content.add(linkNameLabel, 0, 0);
    content.add(linkNameField, 1, 0);
    content.add(hwIndexLabel, 0, 1);
    content.add(hwIndexField, 1, 1);

    getDialogPane().setContent(content);
    getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

    Button okButton = (Button) getDialogPane().lookupButton(ButtonType.OK);
    okButton.disableProperty().bind(hwIndexField.invalidProperty());
    okButton.setDefaultButton(true);

    setResultConverter(buttonType -> {
      if (buttonType.equals(ButtonType.OK)) {
        return new String[]{linkNameField.getText(), hwIndexField.getText()};
      }

      return null;
    });
  }

}
