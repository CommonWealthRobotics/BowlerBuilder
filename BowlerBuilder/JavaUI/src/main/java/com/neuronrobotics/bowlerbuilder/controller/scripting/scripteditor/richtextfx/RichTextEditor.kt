package com.neuronrobotics.bowlerbuilder.controller.scripting.scripteditor.richtextfx

// TODO: Re-enable once the kernel is a subproject
// class RichTextEditor(private val codeArea: CodeArea) : ScriptEditor {
//
//    override fun insertAtCursor(text: String) {
//        codeArea.insertText(codeArea.caretPosition, text)
//    }
//
//    override fun setText(text: String) {
//        codeArea.replaceText(0, codeArea.length, text)
//    }
//
//    override fun getFullText(): String {
//        return codeArea.text
//    }
//
//    override fun getSelectedText(): String {
//        return codeArea.selectedText
//    }
//
//    override fun gotoLine(lineNumber: Int) {
//        codeArea.moveTo(if (lineNumber - 1 < 0) 0 else lineNumber - 1, 0)
//    }
//
//    override fun getCursorPosition(): Int {
//        return 0
//    }
// }
