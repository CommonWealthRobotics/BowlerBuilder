/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package com.neuronrobotics.bowlerbuilder.view.dialog.plugin;

import com.neuronrobotics.bowlerbuilder.controller.plugin.Plugin;
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
public class ManagePluginsDialog extends Dialog<List<Plugin>> {

  private final ListView<Plugin> pluginListView;

  /**
   * A {@link Dialog} to manage the installed plugins.
   *
   * @param plugins list of plugins
   */
  public ManagePluginsDialog(final ObservableList<Plugin> plugins) {
    super();

    pluginListView = new ListView<>(plugins);
    pluginListView.setId("pluginList");

    final Button addWidgetButton = new Button("Add Plugin");
    addWidgetButton.setId("addPlugin");
    addWidgetButton.setOnAction(
        event -> {
          final AddPluginDialog dialog = new AddPluginDialog();
          final Optional<Boolean> result = dialog.showAndWait();
          if (result.isPresent() && result.get()) {
            plugins.add(new Plugin(dialog.getSource(), dialog.getDisplayName()));
          }
        });

    final Button removeWidgetButton = new Button("Remove Plugin");
    removeWidgetButton.setId("removePlugin");
    removeWidgetButton.setOnAction(
        event ->
            pluginListView
                .getSelectionModel()
                .getSelectedItems()
                .forEach(pluginListView.getItems()::remove));

    final HBox controlBox = new HBox(5, addWidgetButton, removeWidgetButton);

    final VBox view = new VBox(5, controlBox, pluginListView);

    setTitle("Manage Plugins");
    getDialogPane().setContent(view);
    getDialogPane().getButtonTypes().add(ButtonType.OK);
    getDialogPane().setId("managePluginsDialogPane");

    setResultConverter(buttonType -> pluginListView.getItems());
  }

  public void setPlugins(final ObservableList<Plugin> plugins) {
    pluginListView.setItems(plugins);
  }
}
