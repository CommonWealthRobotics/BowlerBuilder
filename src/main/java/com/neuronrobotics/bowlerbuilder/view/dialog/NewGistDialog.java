package com.neuronrobotics.bowlerbuilder.view.dialog;

import com.neuronrobotics.bowlerbuilder.FxUtil;
import com.neuronrobotics.bowlerbuilder.GistUtilities;
import com.neuronrobotics.bowlerbuilder.view.dialog.util.ValidatedTextField;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;

public class NewGistDialog extends Dialog<List<String>> {

  private final ValidatedTextField nameField;
  private final TextField descField;
  private final CheckBox publicBox;

  public NewGistDialog() {
    super();

    nameField = new ValidatedTextField("Invalid File Name", name ->
        GistUtilities.isValidCodeFileName(name).isPresent());
    descField = new TextField();
    publicBox = new CheckBox();

    nameField.setId("nameField");
    descField.setId("descField");
    publicBox.setId("publicBox");

    publicBox.setSelected(true);

    setTitle("New Gist");

    GridPane pane = new GridPane();
    pane.setId("newGistRoot");
    pane.setAlignment(Pos.CENTER);
    pane.setHgap(5);
    pane.setVgap(5);

    pane.add(new Label("Name"), 0, 0);
    pane.add(new Label("Description"), 0, 1);
    pane.add(new Label("Public"), 0, 2);
    pane.add(nameField, 1, 0);
    pane.add(descField, 1, 1);
    pane.add(publicBox, 1, 2);

    getDialogPane().setContent(pane);
    getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

    FxUtil.runFX(nameField::requestFocus);

    Button addButton = (Button) getDialogPane().lookupButton(ButtonType.OK);
    addButton.disableProperty().bind(Bindings.createBooleanBinding(() ->
            !(!nameField.invalidProperty().getValue()
                && !descField.getText().isEmpty()),
        nameField.invalidProperty(),
        descField.textProperty()));
    addButton.setDefaultButton(true);

    setResultConverter(buttonType -> {
      if (buttonType.equals(ButtonType.OK)) {
        List<String> data = new ArrayList<>();
        Collections.addAll(data,
            nameField.getText(),
            descField.getText());
        return data;
      }
      return null;
    });
  }

  public String getName() {
    return nameField.getText();
  }

  public String getDescription() {
    return descField.getText();
  }

  public boolean getIsPublic() {
    return publicBox.isSelected();
  }

  public boolean isInvalidName() {
    return nameField.invalidProperty().get();
  }

  public ReadOnlyBooleanProperty invalidNameProperty() {
    return nameField.invalidProperty();
  }

}
