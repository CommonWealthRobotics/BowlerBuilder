/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.bowlerbuilder.model.preferences.bowler;

import com.google.common.base.CaseFormat;
import com.google.common.base.Converter;
import com.google.common.base.Throwables;
import com.neuronrobotics.bowlerbuilder.LoggerUtilities;
import com.neuronrobotics.bowlerbuilder.model.preferences.Preference;
import com.neuronrobotics.bowlerbuilder.model.preferences.Preferences;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;
import java.util.Arrays;
import java.util.Objects;
import java.util.logging.Logger;

/**
 * From GRIP. See third-party-licenses/GRIP.txt.
 *
 * <p>This needs to be in the same package as all the preferences subclasses of this so the
 * automatic introspection can find them.
 */
public class CustomBeanInfo extends SimpleBeanInfo {

  private static final Logger LOGGER =
      LoggerUtilities.getLogger(CustomBeanInfo.class.getSimpleName());
  private final Converter<String, String> caseConverter =
      CaseFormat.LOWER_CAMEL.converterTo(CaseFormat.UPPER_CAMEL);
  private final Class<? extends Preferences> beanClass;

  public CustomBeanInfo(final Class<? extends Preferences> beanClass) {
    super();
    this.beanClass = beanClass;
  }

  @Override
  public PropertyDescriptor[] getPropertyDescriptors() {
    return Arrays.stream(beanClass.getDeclaredFields())
        .map(
            field -> {
              final String property = field.getName();
              final Preference preference = field.getAnnotation(Preference.class);

              if (preference != null) {
                try {
                  final PropertyDescriptor descriptor =
                      new PropertyDescriptor(
                          property,
                          beanClass,
                          "get" + caseConverter.convert(property),
                          "set" + caseConverter.convert(property));

                  descriptor.setDisplayName(preference.name());
                  descriptor.setShortDescription(preference.description());
                  return descriptor;
                } catch (final IntrospectionException e) {
                  LOGGER.warning(
                      "Failed to create a PropertyDescriptor for preference: "
                          + property
                          + "\n"
                          + Throwables.getStackTraceAsString(e));
                }
              }

              return null;
            })
        .filter(Objects::nonNull)
        .toArray(PropertyDescriptor[]::new);
  }
}
