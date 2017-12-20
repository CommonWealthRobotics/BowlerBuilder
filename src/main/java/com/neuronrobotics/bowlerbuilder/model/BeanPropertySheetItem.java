package com.neuronrobotics.bowlerbuilder.model;

import java.util.Optional;
import javafx.beans.property.Property;
import javafx.beans.value.ObservableValue;
import org.controlsfx.control.PropertySheet;

/**
 * PropertySheet.Item that reads/writes from/to a bean.
 */
public class BeanPropertySheetItem implements PropertySheet.Item {

  private Property prop;

  public BeanPropertySheetItem(Property prop) {
    this.prop = prop;
  }

  @Override
  public Class<?> getType() {
    return prop.getValue().getClass();
  }

  @Override
  public String getCategory() {
    return "";
  }

  @Override
  public String getName() {
    return prop.getName();
  }

  @Override
  public String getDescription() {
    return "";
  }

  @Override
  public Object getValue() {
    return prop.getValue();
  }

  @Override
  public void setValue(Object value) {
    prop.setValue(value); //Type validation is handled at the UI level
  }

  @Override
  public Optional<ObservableValue<? extends Object>> getObservableValue() {
    return Optional.empty();
  }

}
