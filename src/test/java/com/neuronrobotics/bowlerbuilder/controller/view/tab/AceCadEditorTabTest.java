package com.neuronrobotics.bowlerbuilder.controller.view.tab;

import com.neuronrobotics.bowlerbuilder.AutoClosingApplicationTest;
import com.neuronrobotics.bowlerbuilder.view.tab.AceCadEditorTab;
import javafx.stage.Stage;

public class AceCadEditorTabTest extends AutoClosingApplicationTest {

  private AceCadEditorTab tab;

  @Override
  public void start(Stage stage) throws Exception {
    tab = new AceCadEditorTab("");
    stage.setScene(tab.getView().getScene());
    stage.show();
  }

  //TODO: This times out on Travis for some reason
  /*@Test
  void basicTest() throws Exception {
    FxUtil.runFX(() -> tab.getController().getAceScriptEditorController().getScriptEditor()
            .insertAtCursor("return new Cube(1,1,1).toCSG();"));

    Object result = FxUtil.returnFX(() -> tab.getController().getAceScriptEditorController()
        .getScriptRunner().runScript(tab.getController().getAceScriptEditorController()
            .getScriptEditor().getText(), new ArrayList<>(), "BowlerGroovy"));

    assertTrue(result instanceof CSG);
  }*/

}
