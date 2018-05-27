/*
 * Copyright 2017 Ryan Benasutti
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.bowlerbuilder.view.dialog.widget;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.neuronrobotics.bowlerbuilder.AbstractAutoClosingApplicationTest;
import com.neuronrobotics.bowlerbuilder.plugin.Plugin;
import javafx.collections.FXCollections;
import javafx.scene.control.ListView;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;

public class ManageWidgetsDialogTest extends AbstractAutoClosingApplicationTest {

  private ManageWidgetsDialog dialog;

  @Override
  public void start(final Stage stage) {
    dialog = new ManageWidgetsDialog(FXCollections.observableArrayList());
    stage.setScene(dialog.getDialogPane().getScene());
    stage.show();
  }

  @Test
  void addWidgetTest() {
    dialog.setWidgets(FXCollections.observableArrayList(new Plugin("url", "name")));
    clickOn("OK");

    assertEquals(1, dialog.getResult().size());
  }

  @Test
  void removeWidgetTest() {
    dialog.setWidgets(FXCollections.observableArrayList(new Plugin("url", "name")));
    ((ListView<Plugin>) lookup("#widgetList").query()).getSelectionModel().selectFirst();
    clickOn("#removeWidget").clickOn("OK");

    assertEquals(0, dialog.getResult().size());
  }
}
