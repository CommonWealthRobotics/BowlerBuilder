package com.neuronrobotics.bowlerbuilder.view.dialog;

import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import org.controlsfx.control.PropertySheet;

/**
 * Dialog to show user preferences from a {@link PropertySheet}.
 */
public class PreferencesDialog extends Dialog {

  public PreferencesDialog(PropertySheet propertySheet) {
    super();

    getDialogPane().setContent(propertySheet);
    getDialogPane().setId("preferencesDialogPane");
    getDialogPane().getButtonTypes().addAll(ButtonType.OK);
  }

}
