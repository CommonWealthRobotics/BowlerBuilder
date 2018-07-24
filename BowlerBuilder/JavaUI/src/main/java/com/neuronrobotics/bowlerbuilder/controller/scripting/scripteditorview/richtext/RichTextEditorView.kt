package com.neuronrobotics.bowlerbuilder.controller.scripting.scripteditorview.richtext

/**
 * Based on RichTextFX's async Java highlighting demo.
 */
// TODO: Re-enable once the kernel is a subproject
//class RichTextEditorView : ScriptEditorView {
//
//    private val codeArea: CodeArea = CodeArea()
//    private val executor: ExecutorService = Executors.newSingleThreadExecutor()
//    private val editor: RichTextEditor = RichTextEditor(codeArea)
//    private val pane = VirtualizedScrollPane(codeArea)
//
//    private val KEYWORDS = listOf(
//            "abstract", "assert", "boolean", "break", "byte", "case", "catch", "char", "class",
//            "const", "continue", "default", "do", "double", "else", "enum", "extends", "final",
//            "finally", "float", "for", "goto", "if", "implements", "import", "instanceof", "int",
//            "interface", "long", "native", "new", "package", "private", "protected", "public",
//            "return", "short", "static", "strictfp", "super", "switch", "synchronized", "this",
//            "throw", "throws", "transient", "try", "void", "volatile", "while"
//    )
//
//    private val KEYWORD_PATTERN = "\\b(" + KEYWORDS.joinToString("|") + ")\\b"
//    private val PAREN_PATTERN = "\\(|\\)"
//    private val BRACE_PATTERN = "\\{|\\}"
//    private val BRACKET_PATTERN = "\\[|\\]"
//    private val SEMICOLON_PATTERN = "\\;"
//    private val STRING_PATTERN = "\"([^\"\\\\]|\\\\.)*\""
//    private val COMMENT_PATTERN = "//[^\n]*" + "|" + "/\\*(.|\\R)*?\\*/"
//
//    private val PATTERN = Pattern.compile(
//            "(?<KEYWORD>" + KEYWORD_PATTERN + ")" +
//                    "|(?<PAREN>" + PAREN_PATTERN + ")" +
//                    "|(?<BRACE>" + BRACE_PATTERN + ")" +
//                    "|(?<BRACKET>" + BRACKET_PATTERN + ")" +
//                    "|(?<SEMICOLON>" + SEMICOLON_PATTERN + ")" +
//                    "|(?<STRING>" + STRING_PATTERN + ")" +
//                    "|(?<COMMENT>" + COMMENT_PATTERN + ")"
//    )
//
//    init {
//        configureCodeArea(codeArea)
//        pane.stylesheets.add(
//                RichTextEditorView::class.java
//                        .getResource("/com/neuronrobotics/bowlerbuilder/" +
//                                "richtext-editor-groovy-keywords.css")
//                        .toExternalForm())
//    }
//
//    private fun configureCodeArea(codeArea: CodeArea) {
//        // Add line numbers in the gutter
//        codeArea.paragraphGraphicFactory = LineNumberFactory.get(codeArea)
//
//        // Recompute syntax highlighting 500ms after the user stops typing
//        codeArea.multiPlainChanges()
//                .successionEnds(Duration.ofMillis(500))
//                .supplyTask(this::computeHighlightingAsync)
//                .awaitLatest(codeArea.multiPlainChanges())
//                .filterMap {
//                    if (it.isSuccess) {
//                        Optional.of(it.get())
//                    } else {
//                        it.failure.printStackTrace()
//                        Optional.empty()
//                    }
//                }
//                .subscribe(this::applyHighlighting)
//    }
//
//    private fun computeHighlightingAsync(): Task<StyleSpans<Collection<String>>> {
//        val text = codeArea.text
//        val task = object : Task<StyleSpans<Collection<String>>>() {
//            @Throws(Exception::class)
//            override fun call(): StyleSpans<Collection<String>> {
//                return computeHighlighting(text)
//            }
//        }
//
//        executor.execute(task)
//        return task
//    }
//
//    private fun applyHighlighting(highlighting: StyleSpans<Collection<String>>) =
//            codeArea.setStyleSpans(0, highlighting)
//
//    private fun computeHighlighting(text: String): StyleSpans<Collection<String>> {
//        val matcher = PATTERN.matcher(text)
//        var lastKwEnd = 0
//        val spansBuilder = StyleSpansBuilder<Collection<String>>()
//
//        while (matcher.find()) {
//            when {
//                matcher.group("KEYWORD") != null -> "keyword"
//                matcher.group("PAREN") != null -> "paren"
//                matcher.group("BRACE") != null -> "brace"
//                matcher.group("BRACKET") != null -> "bracket"
//                matcher.group("SEMICOLON") != null -> "semicolon"
//                matcher.group("STRING") != null -> "string"
//                matcher.group("COMMENT") != null -> "comment"
//                else -> null
//            }?.also {
//                spansBuilder.add(emptyList(), matcher.start() - lastKwEnd)
//                spansBuilder.add(listOf(it), matcher.end() - matcher.start())
//                lastKwEnd = matcher.end()
//            }
//        }
//
//        spansBuilder.add(Collections.emptyList(), text.length - lastKwEnd)
//        return spansBuilder.create()
//    }
//
//    override fun setFontSize(fontSize: Int) {}
//
//    override fun getView(): Node = pane
//
//    override fun getScriptEditor(): ScriptEditor = editor
//}
