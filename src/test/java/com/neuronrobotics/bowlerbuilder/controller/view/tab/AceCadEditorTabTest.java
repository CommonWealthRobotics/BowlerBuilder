package com.neuronrobotics.bowlerbuilder.controller.view.tab;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.neuronrobotics.bowlerbuilder.AutoClosingApplicationTest;
import com.neuronrobotics.bowlerbuilder.FxHelper;
import com.neuronrobotics.bowlerbuilder.FxUtil;
import com.neuronrobotics.bowlerbuilder.view.tab.AceCadEditorTab;
import eu.mihosoft.vrl.v3d.CSG;
import java.util.ArrayList;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;

public class AceCadEditorTabTest extends AutoClosingApplicationTest {

  private AceCadEditorTab tab;

  @Override
  public void start(Stage stage) throws Exception {
    tab = new AceCadEditorTab("");
    stage.setScene(tab.getView().getScene());
    stage.show();
  }

  @Test
  void basicTest() throws Exception {
    FxHelper.runAndWait(() -> tab.getController().getAceScriptEditorController().getScriptEditor()
        .insertAtCursor("return new Cube(1,1,1).toCSG();"));

    Object result = FxUtil.returnFX(() -> tab.getController().getAceScriptEditorController()
        .getScriptRunner().runScript(tab.getController().getAceScriptEditorController()
            .getScriptEditor().getText(), new ArrayList<>(), "BowlerGroovy"));

    assertTrue(result instanceof CSG);
  }

}
