package com.neuronrobotics.bowlerbuilder.controller;

import com.google.common.base.Throwables;
import com.google.common.io.Files;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.neuronrobotics.bowlerbuilder.FxUtil;
import com.neuronrobotics.bowlerbuilder.GistUtilities;
import com.neuronrobotics.bowlerbuilder.LoggerUtilities;
import com.neuronrobotics.bowlerbuilder.controller.scripting.scripteditor.ScriptEditor;
import com.neuronrobotics.bowlerbuilder.controller.scripting.scripteditor.ScriptEditorView;
import com.neuronrobotics.bowlerbuilder.controller.scripting.scriptrunner.ScriptRunner;
import com.neuronrobotics.bowlerbuilder.controller.util.StringClipper;
import com.neuronrobotics.bowlerbuilder.model.preferences.PreferenceListener;
import com.neuronrobotics.bowlerbuilder.model.preferences.PreferencesService;
import com.neuronrobotics.bowlerbuilder.model.preferences.PreferencesServiceFactory;
import com.neuronrobotics.bowlerbuilder.view.dialog.NewGistDialog;
import com.neuronrobotics.bowlerbuilder.view.dialog.PublishDialog;
import com.neuronrobotics.bowlerstudio.scripting.ScriptingEngine;
import groovy.lang.GroovyRuntimeException;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TextField;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.BorderPane;
import org.controlsfx.control.Notifications;
import org.controlsfx.glyphfont.FontAwesome;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.kohsuke.github.GHGist;
import org.kohsuke.github.GHGistFile;

public class AceScriptEditorController {

  private static final Logger logger =
      LoggerUtilities.getLogger(AceScriptEditorController.class.getSimpleName());
  private final ScriptEditorView scriptEditorView;
  private final ScriptEditor scriptEditor;
  private final ScriptRunner scriptRunner;
  private final String scriptLangName;
  private final StringClipper stringClipper;
  private final IntegerProperty fontSize;
  private final IntegerProperty maxToastLength;
  @FXML
  private SplitPane fileEditorRoot;
  @FXML
  private BorderPane editorBorderPane;
  @FXML
  private Button runButton;
  @FXML
  private Button publishButton;
  @FXML
  private TextField fileNameField;
  @FXML
  private TextField gistURLField;
  private GHGist gist;
  private String manualRemote;
  private GHGistFile gistFile;
  private String manualFile;
  private boolean isScratchpad = true;
  private Tab tab;
  private Runnable reloadMenus;

  @Inject
  public AceScriptEditorController(PreferencesServiceFactory preferencesServiceFactory,
      ScriptEditorView scriptEditorView,
      ScriptRunner scriptRunner,
      @Named("scriptLangName") String scriptLangName,
      StringClipper stringClipper) {
    this.scriptEditorView = scriptEditorView;
    this.scriptEditor = scriptEditorView.getScriptEditor();
    this.scriptRunner = scriptRunner;
    this.scriptLangName = scriptLangName;
    this.stringClipper = stringClipper;

    logger.log(Level.FINE, "factory: " + preferencesServiceFactory);

    PreferencesService preferencesService
        = preferencesServiceFactory.create("AceScriptEditorController");
    fontSize = new SimpleIntegerProperty(preferencesService.get("Font Size", 14));
    preferencesService.addListener("Font Size",
        (PreferenceListener<Integer>) (oldVal, newVal) -> {
          fontSize.setValue(newVal);
          scriptEditor.setFontSize(newVal);
        });

    maxToastLength = new SimpleIntegerProperty(preferencesService.get("Max Toast Length", 15));
    preferencesService.addListener("Max Toast Length",
        (PreferenceListener<Integer>) (oldVal, newVal) -> maxToastLength.setValue(newVal));

    logger.log(Level.FINE, "Running with language: " + scriptLangName);
  }

  @FXML
  protected void initialize() {
    editorBorderPane.setCenter(scriptEditorView.getView());

    fileEditorRoot.setDividerPosition(0, 0.8);

    runButton.setGraphic(new FontAwesome().create(String.valueOf(FontAwesome.Glyph.PLAY)));
    publishButton.setGraphic(
        new FontAwesome().create(String.valueOf(FontAwesome.Glyph.CLOUD_UPLOAD)));

    scriptEditor.setFontSize(fontSize.get());
  }

  @FXML
  private void runFile(ActionEvent actionEvent) {
    Thread thread = LoggerUtilities.newLoggingThread(logger, this::runEditorContent);
    thread.setDaemon(true);
    thread.start();
  }

  @FXML
  private void publishFile(ActionEvent actionEvent) {
    if (isScratchpad) {
      publishScratchpad();
    } else {
      publishNormal();
    }
  }

  /**
   * Publish the editor contents normally (not scratchpad).
   */
  private void publishNormal() {
    new PublishDialog().showAndWait().ifPresent(commitMessage -> {
      try {
        String remote;
        String relativePath;

        if (gist == null) {
          remote = manualRemote;
          relativePath = manualFile;
        } else {
          File currentFile = ScriptingEngine.fileFromGit(
              gist.getGitPushUrl(),
              gistFile.getFileName()
          );

          Git git = ScriptingEngine.locateGit(currentFile);
          remote = git.getRepository().getConfig().getString("remote", "origin", "url");
          relativePath = ScriptingEngine.findLocalPath(currentFile, git);
        }

        //Push to existing gist
        ScriptingEngine.pushCodeToGit(
            remote,
            ScriptingEngine.getFullBranch(remote),
            relativePath,
            scriptEditor.getText(),
            commitMessage
        );
      } catch (Exception e) {
        logger.log(Level.SEVERE,
            "Could not commit.\n" + Throwables.getStackTraceAsString(e));
        FxUtil.runFX(() -> Notifications.create()
            .title("Commit failed")
            .text("Could not perform commit. Changes not saved.")
            .showError());
      }
    });
  }

  /**
   * Publish the editor contents during scratchpad mode.
   */
  private void publishScratchpad() {
    NewGistDialog dialog = new NewGistDialog();
    dialog.showAndWait().ifPresent(__ -> {
      try {
        //Make a new gist
        GHGist newGist = GistUtilities.createNewGist(
            dialog.getName(),
            dialog.getDescription(),
            dialog.getIsPublic()
        );

        PublishDialog publishDialog = new PublishDialog();
        publishDialog.showAndWait().ifPresent(commitMessage -> {
          try {
            //Push the new gist
            ScriptingEngine.pushCodeToGit(
                newGist.getGitPushUrl(),
                ScriptingEngine.getFullBranch(newGist.getGitPushUrl()),
                dialog.getName(),
                scriptEditor.getText(),
                commitMessage
            );

            isScratchpad = false;
            gistURLField.setText(newGist.getGitPushUrl());
            fileNameField.setText(dialog.getName());
            gist = newGist;
            gistFile = newGist.getFiles().get(dialog.getName());
            tab.setText(dialog.getName());
            reloadMenus.run();
          } catch (Exception e) {
            logger.log(Level.SEVERE,
                "Could not push code.\n" + Throwables.getStackTraceAsString(e));
          }
        });
      } catch (IOException e) {
        logger.log(Level.SEVERE,
            "Could not create new gist.\n" + Throwables.getStackTraceAsString(e));
      }
    });
  }

  @FXML
  private void onCopyGist(ActionEvent actionEvent) {
    //Put gist URL on system clipboard
    ClipboardContent content = new ClipboardContent();
    content.putString(gistURLField.getText());
    Clipboard.getSystemClipboard().setContent(content);
  }

  /**
   * Load a file from disk and insert its content into the editor.
   *
   * @param file File to load
   */
  public void loadFile(File file) {
    if (file != null) {
      try {
        scriptEditor.setText(Files.toString(file, Charset.forName("UTF-8")));
      } catch (IOException e) {
        logger.log(Level.SEVERE,
            "Could not load file: " + file.getAbsolutePath() + ".\n"
                + Throwables.getStackTraceAsString(e));
      }
    }
  }

  /**
   * Load a file from a gist.
   *
   * @param gist Parent gist
   * @param gistFile File in gist
   */
  public void loadGist(GHGist gist, GHGistFile gistFile) {
    isScratchpad = false;
    File file;

    try {
      file = ScriptingEngine.fileFromGit(gist.getGitPushUrl(), gistFile.getFileName());

      this.gist = gist;
      this.gistFile = gistFile;

      gistURLField.setText(gist.getGitPushUrl());
      fileNameField.setText(gistFile.getFileName());

      loadFile(file);
    } catch (GitAPIException | IOException e) {
      logger.log(Level.SEVERE,
          "Could get file from git.\n" + Throwables.getStackTraceAsString(e));
    }
  }

  public void loadManualGist(String pushURL, String fileName, File file) {
    isScratchpad = false;
    manualRemote = pushURL;
    manualFile = fileName;

    gistURLField.setText(pushURL);
    fileNameField.setText(fileName);

    if (file != null) {
      try {
        scriptEditor.setText(Files.toString(file, Charset.forName("UTF-8")));
      } catch (IOException e) {
        logger.log(Level.SEVERE,
            "Could not load file: " + file.getAbsolutePath() + ".\n"
                + Throwables.getStackTraceAsString(e));
      }
    }
  }

  /**
   * Run the content inside the editor.
   *
   * @return result from the script
   */
  public Object runEditorContent() {
    try {
      try {
        return runStringScript(
            FxUtil.returnFX(scriptEditor::getText), new ArrayList<>(), scriptLangName);
      } catch (ExecutionException e) {
        logger.log(Level.SEVERE,
            "Could not get text from editor.\n" + Throwables.getStackTraceAsString(e));
      }
    } catch (InterruptedException e) {
      logger.log(Level.WARNING,
          "CountDownLatch interrupted while waiting to get editor content.\n"
              + Throwables.getStackTraceAsString(e));
    }

    return null;
  }

  /**
   * Run a script from a string in the editor's environment.
   *
   * @param script script content
   * @param arguments script arguments
   * @param languageName scripting language name
   * @return script result
   */
  public Object runStringScript(String script, ArrayList<Object> arguments, String languageName) {
    try {
      //Run the code
      logger.log(Level.FINE, "Running script.");
      Object result = scriptRunner.runScript(script, arguments, languageName);
      logger.log(Level.FINER, "Result is: " + result);
      return result;
    } catch (IOException e) {
      logger.log(Level.SEVERE,
          "Could not load CADModelViewer.\n" + Throwables.getStackTraceAsString(e));
    } catch (GroovyRuntimeException e) {
      logger.log(Level.WARNING,
          "Error in CAD script: " + e.getMessage());
      FxUtil.runFX(() -> Notifications.create()
          .title("Error in CAD Script")
          .text(stringClipper.clipStringToLines(e.getMessage(), maxToastLength.getValue()))
          .owner(fileEditorRoot)
          .position(Pos.BOTTOM_RIGHT)
          .showInformation());
    } catch (Exception e) {
      logger.log(Level.SEVERE,
          "Could not run CAD script.\n" + Throwables.getStackTraceAsString(e));
    }

    return null;
  }

  /**
   * Gives the scratchpad code what it needs to work properly.
   *
   * @param tab Tab the editor is contained in
   */
  public void initScratchpad(Tab tab, Runnable reloadMenus) {
    this.tab = tab;
    this.reloadMenus = reloadMenus;
  }

  /**
   * Insert the text at the cursor.
   *
   * @param text text to insert
   */
  public void insertAtCursor(String text) {
    scriptEditor.insertAtCursor(text);
  }

  public ScriptEditorView getScriptEditorView() {
    return scriptEditorView;
  }

  public ScriptEditor getScriptEditor() {
    return scriptEditor;
  }

  public ScriptRunner getScriptRunner() {
    return scriptRunner;
  }

}
