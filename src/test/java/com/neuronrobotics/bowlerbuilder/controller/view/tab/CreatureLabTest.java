package com.neuronrobotics.bowlerbuilder.controller.view.tab;

import com.neuronrobotics.bowlerbuilder.AbstractAutoClosingApplicationTest;
import com.neuronrobotics.bowlerbuilder.view.tab.CreatureLabTab;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;

public class CreatureLabTest extends AbstractAutoClosingApplicationTest {

  @Override
  public void start(final Stage stage) throws Exception {
    final CreatureLabTab tab = new CreatureLabTab("");
    stage.setScene(tab.getView().getScene());
    stage.show();
  }

  @Test
  void testStart() {
    //Nothing here, just test to make sure we start without exceptions
  }

}
