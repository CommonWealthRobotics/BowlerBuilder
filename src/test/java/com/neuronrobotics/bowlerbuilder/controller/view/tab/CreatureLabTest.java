package com.neuronrobotics.bowlerbuilder.controller.view.tab;

import com.neuronrobotics.bowlerbuilder.AutoClosingApplicationTest;
import com.neuronrobotics.bowlerbuilder.view.tab.CreatureLabTab;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;

public class CreatureLabTest extends AutoClosingApplicationTest {

  private CreatureLabTab tab;

  @Override
  public void start(Stage stage) throws Exception {
    tab = new CreatureLabTab("");
    stage.setScene(tab.getView().getScene());
    stage.show();
  }

  @Test
  void testStart() {
    //Nothing here, just test to make sure we start without exceptions
  }

}
