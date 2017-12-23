package com.neuronrobotics.bowlerbuilder;

import com.google.inject.Guice;
import com.google.inject.Injector;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

class BowlerBuilderTest extends AutoClosingApplicationTest {

  @Override
  public void start(Stage stage) throws Exception {
    Injector injector = Guice.createInjector();
    FXMLLoader loader = new FXMLLoader(
        BowlerBuilder.class.getResource("/com/neuronrobotics/bowlerbuilder/MainWindow.fxml"),
        null,
        null,
        injector::getInstance);

    Pane mainWindow = loader.load();
    stage.setScene(new Scene(mainWindow));
    stage.show();
  }

  //  @Test
  //  void openScratchpadTest() {
  //    clickOn("3D CAD").clickOn("Scratchpad");
  //
  //    assertTrue(lookup("#fileEditorRoot").tryQuery().isPresent());
  //  }
  //
  //  @Test
  //  void openHelp() {
  //    clickOn("Help").clickOn("Editor Guide");
  //
  //    assertTrue(lookup("#editorGuide").tryQuery().isPresent());
  //  }
  //
  //  @Test
  //  void openPreferences() {
  //    clickOn("File").clickOn("Preferences");
  //
  //    assertTrue(lookup("#preferencesDialogPane").tryQuery().isPresent());
  //  }

}
