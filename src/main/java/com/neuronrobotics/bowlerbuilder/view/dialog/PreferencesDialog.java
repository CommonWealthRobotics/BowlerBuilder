/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package com.neuronrobotics.bowlerbuilder.view.dialog;

import com.neuronrobotics.bowlerbuilder.model.BeanPropertySheetItem;
import com.neuronrobotics.bowlerbuilder.model.preferences.PreferencesService;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.layout.VBox;
import javax.annotation.Nonnull;
import org.controlsfx.control.PropertySheet;

/**
 * Dialog to show user preferences from a {@link PropertySheet}.
 */
public class PreferencesDialog extends Dialog {

  public PreferencesDialog(@Nonnull final Iterable<PreferencesService> preferencesServices) {
    super();

    final List<PropertySheet> propertySheets = new ArrayList<>();
    preferencesServices.forEach(service -> {
      final List<Property> props = service.getAll().entrySet().stream().map(entry -> {
        ObjectProperty<Serializable> property = new SimpleObjectProperty<>(
            null, entry.getKey(), entry.getValue());
        property.addListener((observableValue, oldVal, newVal) ->
            service.set(entry.getKey(), newVal));
        return property;
      }).collect(Collectors.toList());

      propertySheets.add(new PropertySheet(FXCollections.observableArrayList(
          props.stream().map(BeanPropertySheetItem::new).collect(Collectors.toList())
      )));
    });

    final VBox vBox = new VBox(5);
    propertySheets.forEach(vBox.getChildren()::add);
    getDialogPane().setContent(vBox);
    getDialogPane().getButtonTypes().add(ButtonType.OK);
    getDialogPane().setId("preferencesDialogPane");
  }

}
