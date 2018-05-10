/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package com.neuronrobotics.bowlerbuilder.model;

import java.util.Optional;
import javafx.beans.property.Property;
import javafx.beans.value.ObservableValue;
import org.controlsfx.control.PropertySheet;

/** PropertySheet.Item that reads/writes from/to a bean. */
public class BeanPropertySheetItem implements PropertySheet.Item {

  private final Property prop;

  public BeanPropertySheetItem(final Property prop) {
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
  public void setValue(final Object value) {
    prop.setValue(value); // Type validation is handled at the UI level
  }

  @Override
  public Optional<ObservableValue<? extends Object>> getObservableValue() {
    return Optional.empty();
  }
}
