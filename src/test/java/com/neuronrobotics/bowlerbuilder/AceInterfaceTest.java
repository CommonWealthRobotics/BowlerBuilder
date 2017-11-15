package com.neuronrobotics.bowlerbuilder;

//public class AceInterfaceTest extends AutoClosingApplicationTest {
//
//  private AceInterface aceInterface;
//  private String result;
//
//  @Override
//  public void start(Stage stage) throws Exception {
//    WebEngine engine = mock(WebEngine.class);
//    when(engine.executeScript(any(String.class))).thenAnswer(invocation -> {
//      result = (String) invocation.getArguments()[0];
//      return invocation.getArguments()[0];
//    });
//    aceInterface = new AceInterface(engine);
//  }
//
//  @Test
//  void insertBasicCommandTest() {
//    aceInterface.insertAtCursor("Cube foo = new Cube(1, 2, 3).toCSG();");
//    assertEquals("editor.insert(\"Cube foo = new Cube(1, 2, 3).toCSG();\");", result);
//  }
//
//  @Test
//  void insertCommentTest() {
//    aceInterface.insertAtCursor("//I am a comment");
//    assertEquals("editor.insert(\"//I am a comment\");", result);
//  }
//
//  @Test
//  void insertEscapedTest() {
//    aceInterface.insertAtCursor("\"\\\"");
//    assertEquals("editor.insert(\"\"\\\"\");", result);
//  }
//
//}
