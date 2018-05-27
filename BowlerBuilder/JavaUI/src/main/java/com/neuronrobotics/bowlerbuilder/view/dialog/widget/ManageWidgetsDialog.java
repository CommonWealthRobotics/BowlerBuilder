/*
 * Copyright 2017 Ryan Benasutti
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.bowlerbuilder.view.dialog.widget;

import com.neuronrobotics.bowlerbuilder.controller.plugin.Plugin;
import com.neuronrobotics.bowlerbuilder.view.dialog.plugin.AddPluginDialog;
import java.util.List;
import java.util.Optional;
import javafx.collections.ObservableList;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class ManageWidgetsDialog extends Dialog<List<Plugin>> {

  private final ListView<Plugin> widgetListView;

  /**
   * A {@link Dialog} to manage the installed widgets.
   *
   * @param widgets list of widgets
   */
  public ManageWidgetsDialog(final ObservableList<Plugin> widgets) {
    super();

    widgetListView = new ListView<>(widgets);
    widgetListView.setId("widgetList");

    final Button addWidgetButton = new Button("Add Widget");
    addWidgetButton.setId("addWidget");
    addWidgetButton.setOnAction(
        event -> {
          final AddPluginDialog dialog = new AddPluginDialog();
          final Optional<Boolean> result = dialog.showAndWait();
          if (result.isPresent() && result.get()) {
            widgets.add(new Plugin(dialog.getSource(), dialog.getDisplayName()));
          }
        });

    final Button removeWidgetButton = new Button("Remove Widget");
    removeWidgetButton.setId("removeWidget");
    removeWidgetButton.setOnAction(
        event ->
            widgetListView
                .getSelectionModel()
                .getSelectedItems()
                .forEach(widgetListView.getItems()::remove));

    final HBox controlBox = new HBox(5, addWidgetButton, removeWidgetButton);

    final VBox view = new VBox(5, controlBox, widgetListView);

    setTitle("Manage Widgets");
    getDialogPane().setContent(view);
    getDialogPane().getButtonTypes().add(ButtonType.OK);
    getDialogPane().setId("manageWidgetsDialogPane");

    setResultConverter(buttonType -> widgetListView.getItems());
  }

  public void setWidgets(final ObservableList<Plugin> widgets) {
    widgetListView.setItems(widgets);
  }
}
