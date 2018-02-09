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

public class ManagePluginsDialog extends Dialog<List<Plugin>> {

  private final ListView<Plugin> pluginListView;

  public ManagePluginsDialog(ObservableList<Plugin> plugins) {
    super();

    pluginListView = new ListView<>(plugins);
    pluginListView.setId("pluginList");

    Button addWidgetButton = new Button("Add Plugin");
    addWidgetButton.setId("addPlugin");
    addWidgetButton.setOnAction(event -> {
      AddPluginDialog dialog = new AddPluginDialog();
      final Optional<Boolean> result = dialog.showAndWait();
      if (result.isPresent() && result.get()) {
        plugins.add(new Plugin(dialog.getSource(), dialog.getDisplayName()));
      }
    });

    Button removeWidgetButton = new Button("Remove Plugin");
    removeWidgetButton.setId("removePlugin");
    removeWidgetButton.setOnAction(event ->
        pluginListView.getSelectionModel().getSelectedItems()
            .forEach(pluginListView.getItems()::remove));

    HBox controlBox = new HBox(5, addWidgetButton, removeWidgetButton);

    VBox view = new VBox(5, controlBox, pluginListView);

    setTitle("Manage Plugins");
    getDialogPane().setContent(view);
    getDialogPane().getButtonTypes().add(ButtonType.OK);
    getDialogPane().setId("managePluginsDialogPane");

    setResultConverter(buttonType -> pluginListView.getItems());
  }

  public void setPlugins(ObservableList<Plugin> plugins) {
    pluginListView.setItems(plugins);
  }

}
