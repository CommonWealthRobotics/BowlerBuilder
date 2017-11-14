package com.neuronrobotics.bowlerbuilder;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.neuronrobotics.bowlerbuilder.controller.AceInterface;
import javafx.scene.web.WebEngine;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;

public class AceInterfaceTest extends AutoClosingApplicationTest {

  private AceInterface aceInterface;
  private String result;

  @Override
  public void start(Stage stage) throws Exception {
    WebEngine engine = mock(WebEngine.class);
    when(engine.executeScript(any(String.class))).thenAnswer(invocation -> {
      result = invocation.getArgument(0);
      return invocation.getArgument(0);
    });
    aceInterface = new AceInterface(engine);
  }

  @Test
  void insertBasicCommandTest() {
    aceInterface.insertAtCursor("Cube foo = new Cube(1, 2, 3).toCSG();");
    assertEquals("editor.insert(\"Cube foo = new Cube(1, 2, 3).toCSG();\");", result);
  }

  @Test
  void insertCommentTest() {
    aceInterface.insertAtCursor("//I am a comment");
    assertEquals("editor.insert(\"//I am a comment\");", result);
  }

  @Test
  void insertEscapedTest() {
    aceInterface.insertAtCursor("\"\\\"");
    assertEquals("editor.insert(\"\"\\\"\");", result);
  }

}
