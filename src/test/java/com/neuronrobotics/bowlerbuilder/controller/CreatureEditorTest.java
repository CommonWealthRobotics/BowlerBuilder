/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package com.neuronrobotics.bowlerbuilder.controller;

import com.google.inject.Guice;
import com.neuronrobotics.bowlerbuilder.AbstractAutoClosingApplicationTest;
import com.neuronrobotics.bowlerbuilder.view.tab.CreatureLabTab;
import java.io.IOException;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.stage.Stage;

public class CreatureEditorTest extends AbstractAutoClosingApplicationTest {

  @Override
  public void start(final Stage stage) throws IOException {
    final FXMLLoader loader = new FXMLLoader(CreatureLabTab.class.getResource(
        "/com/neuronrobotics/bowlerbuilder/view/CreatureEditor.fxml"),
        null,
        null,
        Guice.createInjector()::getInstance);
    final Node content = loader.load();
    stage.setScene(content.getScene());
    stage.show();
  }

}