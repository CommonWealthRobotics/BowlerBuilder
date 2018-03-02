package com.neuronrobotics.bowlerbuilder.controller;

import static org.junit.jupiter.api.Assertions.assertThrows;

import com.neuronrobotics.bowlerbuilder.AbstractAutoClosingApplicationTest;
import com.neuronrobotics.bowlerbuilder.BowlerBuilder;
import com.neuronrobotics.bowlerbuilder.view.tab.CreatureLabTab;
import java.io.IOException;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;

public class CreatureEditorTest extends AbstractAutoClosingApplicationTest {

  private CreatureEditorController controller;

  @Override
  public void start(final Stage stage) throws IOException {
    final FXMLLoader loader = new FXMLLoader(CreatureLabTab.class.getResource(
        "/com/neuronrobotics/bowlerbuilder/view/CreatureEditor.fxml"),
        null,
        null,
        BowlerBuilder.getInjector()::getInstance);
    final Node content = loader.load();
    controller = loader.getController();
    stage.setScene(content.getScene());
    stage.show();
  }

  @Test
  void badRegenTest() {
    assertThrows(IllegalArgumentException.class, () -> controller.regenerateMenus());
  }

}
