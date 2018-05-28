package com.neuronrobotics.bowlerbuilder.model.preferences.bowler;

import com.google.common.base.CaseFormat;
import com.google.common.base.Converter;
import com.neuronrobotics.bowlerbuilder.model.preferences.Preference;
import com.neuronrobotics.bowlerbuilder.model.preferences.Preferences;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;
import java.util.Arrays;
import java.util.Objects;

/**
 * From GRIP. See third-party-licenses/GRIP.txt.
 *
 * <p>This needs to be in the same package as all the preferences subclasses of this so the
 * automatic introspection can find them.
 */
public class CustomBeanInfo extends SimpleBeanInfo {
  private final Converter<String, String> caseConverter =
      CaseFormat.LOWER_CAMEL.converterTo(CaseFormat.UPPER_CAMEL);
  private final Class<? extends Preferences> beanClass;

  public CustomBeanInfo(Class<? extends Preferences> beanClass) {
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
                } catch (IntrospectionException e) {
                  e.printStackTrace();
                }
              }

              return null;
            })
        .filter(Objects::nonNull)
        .toArray(PropertyDescriptor[]::new);
  }
}
