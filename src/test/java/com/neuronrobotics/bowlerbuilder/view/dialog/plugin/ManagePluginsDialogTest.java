package com.neuronrobotics.bowlerbuilder.view.dialog.plugin;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.neuronrobotics.bowlerbuilder.AbstractAutoClosingApplicationTest;
import com.neuronrobotics.bowlerbuilder.controller.plugin.Plugin;
import javafx.collections.FXCollections;
import javafx.scene.control.ListView;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;

public class ManagePluginsDialogTest extends AbstractAutoClosingApplicationTest {

  private ManagePluginsDialog dialog;

  @Override
  public void start(final Stage stage) {
    dialog = new ManagePluginsDialog(FXCollections.observableArrayList());
    stage.setScene(dialog.getDialogPane().getScene());
    stage.show();
  }

  @Test
  void addWidgetTest() {
    dialog.setPlugins(FXCollections.observableArrayList(new Plugin("url", "name")));
    clickOn("OK");

    assertEquals(1, dialog.getResult().size());
  }

  @Test
  void removeWidgetTest() {
    dialog.setPlugins(FXCollections.observableArrayList(new Plugin("url", "name")));
    ((ListView<Plugin>) lookup("#pluginList").query()).getSelectionModel().selectFirst();
    clickOn("#removePlugin").clickOn("OK");

    assertEquals(0, dialog.getResult().size());
  }

}
