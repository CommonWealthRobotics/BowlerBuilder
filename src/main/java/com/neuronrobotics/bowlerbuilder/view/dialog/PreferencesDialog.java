package com.neuronrobotics.bowlerbuilder.view.dialog;

import com.neuronrobotics.bowlerbuilder.model.BeanPropertySheetItem;
import com.neuronrobotics.bowlerbuilder.model.preferences.PreferencesService;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.layout.VBox;
import org.controlsfx.control.PropertySheet;

/**
 * Dialog to show user preferences from a {@link PropertySheet}.
 */
public class PreferencesDialog extends Dialog {

  public PreferencesDialog(final Collection<PreferencesService> preferencesServices) {
    super();

    final List<PropertySheet> propertySheets = new ArrayList<>();
    preferencesServices.forEach(service -> {
      final List<Property> props = service.getAll().entrySet().stream().map(entry -> {
        ObjectProperty<Serializable> property = new SimpleObjectProperty<>(
            null, entry.getKey(), entry.getValue());
        property.addListener((observableValue, o, t1) -> service.set(entry.getKey(), t1));
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
