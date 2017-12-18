package com.neuronrobotics.bowlerbuilder.controller.scripteditor;

//class AceInterfaceTest extends AutoClosingApplicationTest {
//
//  private AceEditor aceInterface;
//  private WebView webView;
//
//  @Override
//  public void start(Stage stage) throws Exception {
//    webView = new WebView();
//    stage.setScene(new Scene(webView));
//    stage.show();
//    aceInterface = new AceEditor(webView.getEngine());
//  }
//
//  @Test
//  void insertBasicCommandTest() {
//    FxHelper.runAndWait(() -> {
//      webView.getEngine().getLoadWorker().stateProperty()
// .addListener((observable, oldValue, newValue) -> {
//        if (newValue == Worker.State.SUCCEEDED) {
//          aceInterface.insertAtCursor("Cube foo = new Cube(1, 2, 3).toCSG();");
//          assertEquals("editor.insert(\"Cube foo = new Cube(1, 2, 3).toCSG();\");",
// aceInterface.getText());
//        }
//      });
//    });
//
////    assertEquals("editor.insert(\"Cube foo = new Cube(1, 2, 3).toCSG();\");",
// aceInterface.getText());
////    FxHelper.runAndWait(() ->
// assertEquals("editor.insert(\"Cube foo = new Cube(1, 2, 3).toCSG();\");",
// aceInterface.getText()));
//  }
//
////  @Test
////  void insertCommentTest() {
////    aceInterface.insertAtCursor("//I am a comment");
////    assertEquals("editor.insert(\"//I am a comment\");", result);
////  }
////
////  @Test
////  void insertEscapedTest() {
////    aceInterface.insertAtCursor("\"\\\"");
////    assertEquals("editor.insert(\"\"\\\"\");", result);
////  }
//
//}
