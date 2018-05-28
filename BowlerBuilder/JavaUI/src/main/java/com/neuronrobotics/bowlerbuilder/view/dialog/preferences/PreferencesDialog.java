/*
 * Copyright 2017 Ryan Benasutti
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.bowlerbuilder.view.dialog.preferences;

import com.neuronrobotics.bowlerbuilder.model.preferences.Preferences;
import com.neuronrobotics.bowlerbuilder.model.preferences.PreferencesService;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Separator;
import javafx.scene.layout.VBox;
import javax.annotation.ParametersAreNonnullByDefault;
import org.controlsfx.control.PropertySheet;
import org.controlsfx.property.BeanPropertyUtils;

/**
 * Dialog to show user preferences from a {@link PropertySheet}.
 *
 * <p>Based on GRIP. See third-party-licenses/GRIP.txt.
 */
@ParametersAreNonnullByDefault
public class PreferencesDialog extends Dialog<List<Preferences>> {

  /**
   * A {@link Dialog} to edit preferences.
   *
   * @param preferencesServices the preferences to edit
   */
  public PreferencesDialog(
      final List<PreferencesService<? extends Preferences>> preferencesServices) {
    super();

    final List<Preferences> preferences =
        preferencesServices
            .stream()
            .map(PreferencesService::getCurrentPreferencesOrDefault)
            .collect(Collectors.toList());

    final List<Node> nodes = new ArrayList<>();
    preferences
        .stream()
        .sequential()
        .map(item -> new CustomPropertySheet(BeanPropertyUtils.getProperties(item)))
        .forEach(
            sheet -> {
              nodes.add(sheet);
              nodes.add(new Separator());
            });

    if (nodes.get(nodes.size() - 1) instanceof Separator) {
      nodes.remove(nodes.size() - 1);
    }

    final VBox vBox = new VBox(5);
    vBox.getChildren().addAll(nodes);

    getDialogPane().setContent(vBox);
    getDialogPane().getButtonTypes().addAll(ButtonType.CANCEL, ButtonType.OK);
    getDialogPane().setId("preferencesDialogPane");

    setResultConverter(
        buttonType -> {
          if (ButtonType.OK.equals(buttonType)) {
            return preferences;
          }

          return null;
        });
  }

  private static class CustomPropertySheet extends PropertySheet {
    public CustomPropertySheet(ObservableList<Item> items) {
      super(items);
      setMode(Mode.NAME);
      setModeSwitcherVisible(false);
      setSearchBoxVisible(false);
    }
  }
}
