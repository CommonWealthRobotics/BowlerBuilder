package com.neuronrobotics.bowlerbuilder.view.dialog;

import com.google.common.primitives.Ints;
import com.neuronrobotics.bowlerbuilder.view.dialog.util.ValidatedTextField;
import java.util.Set;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class AddLinkDialog extends Dialog<String[]> {

  public AddLinkDialog(Set<Integer> takenChannels) {
    super();

    setTitle("Add Link");

    Label linkNameLabel = new Label("Link name");
    TextField linkNameField = new TextField();
    HBox nameHBox = new HBox(5, linkNameLabel, linkNameField);
    nameHBox.setAlignment(Pos.CENTER_LEFT);

    Label hwIndexLabel = new Label("Hardware index");
    ValidatedTextField hwIndexField = new ValidatedTextField("Invalid number",
        text -> {
          Integer result = Ints.tryParse(text);
          return result != null && !takenChannels.contains(result);
        });
    HBox indexHBox = new HBox(5, hwIndexLabel, hwIndexField);
    indexHBox.setAlignment(Pos.CENTER_LEFT);

    VBox content = new VBox(5, nameHBox, indexHBox);
    content.setAlignment(Pos.CENTER_LEFT);
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
