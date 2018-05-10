/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package com.neuronrobotics.bowlerbuilder;

import com.google.inject.Guice;
import com.google.inject.Injector;
import java.io.IOException;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

public class BowlerBuilderTest extends AbstractAutoClosingApplicationTest {

  @Override
  public void start(final Stage stage) throws IOException {
    final Injector injector = Guice.createInjector();
    final FXMLLoader loader =
        new FXMLLoader(
            getClass().getResource("/com/neuronrobotics/bowlerbuilder/MainWindow.fxml"),
            null,
            null,
            injector::getInstance);

    final Pane mainWindow = loader.load();
    stage.setScene(new Scene(mainWindow));
    stage.show();
  }

  /*@Test
  void openScratchpadTest() {
    clickOn("3D CAD").clickOn("Scratchpad");

    assertTrue(lookup("#fileEditorRoot").tryQuery().isPresent());
  }

  @Test
  void openHelp() {
    clickOn("Help").clickOn("Editor Guide");

    assertTrue(lookup("#editorGuide").tryQuery().isPresent());
  }

  @Test
  void openPreferences() {
    clickOn("File").clickOn("Preferences");

    assertTrue(lookup("#preferencesDialogPane").tryQuery().isPresent());
  }*/

}
