/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package com.neuronrobotics.bowlerbuilder.controller.scripting.scripteditor;

import static org.junit.Assert.assertEquals;

import com.neuronrobotics.bowlerbuilder.controller.scripting.scripteditor.ace.AceEditor;
import org.junit.jupiter.api.Test;

public class AceInterfaceTest {

  private final MockAdapter mockAdapter = new MockAdapter();
  private final AceEditor aceEditor = new AceEditor(mockAdapter);

  @Test
  void insertTextTest() {
    aceEditor.insertAtCursor("test");
    assertEquals("editor.insert(\"test\");", mockAdapter.lastExecutedScript);
  }

  @Test
  void insertTextTest2() {
    aceEditor.insertAtCursor("CSG foo = new Cube(1,1,1).toCSG();");
    assertEquals("editor.insert(\"CSG foo = new Cube(1,1,1).toCSG();\");",
        mockAdapter.lastExecutedScript);
  }

  @Test
  void insertTextTest3() {
    aceEditor.insertAtCursor("\n\t");
    assertEquals("editor.insert(\"\\n\t\");",
        mockAdapter.lastExecutedScript);
  }

  @Test
  void insertTextTest4() {
    aceEditor.insertAtCursor("\\'\"");
    assertEquals("editor.insert(\"\\\\'\\\"\");",
        mockAdapter.lastExecutedScript);
  }

  @Test
  void fontSizeTest() {
    aceEditor.setFontSize(1);
    assertEquals("document.getElementById('editor').style.fontSize='1px';",
        mockAdapter.lastExecutedScript);
  }

  @Test
  void gotoLineTest() {
    aceEditor.gotoLine(1);
    assertEquals("editor.gotoLine(1);",
        mockAdapter.lastExecutedScript);
  }

}
