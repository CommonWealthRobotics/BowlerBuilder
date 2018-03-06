/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

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
import javafx.application.Platform;
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
import javax.annotation.Nonnull;
import org.controlsfx.control.Notifications;
import org.controlsfx.glyphfont.FontAwesome;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.kohsuke.github.GHGist;
import org.kohsuke.github.GHGistFile;

public class AceScriptEditorController {

  private static final Logger LOGGER =
      LoggerUtilities.getLogger(AceScriptEditorController.class.getSimpleName());
  private final ScriptEditorView scriptEditorView;
  private final ScriptEditor scriptEditor;
  private final ScriptRunner scriptRunner;
  private final StringClipper stringClipper;
  private final IntegerProperty fontSize;
  private final IntegerProperty maxToastLength;
  private String scriptLangName;
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
  public AceScriptEditorController(
      @Nonnull final PreferencesServiceFactory preferencesServiceFactory,
      @Nonnull final ScriptEditorView scriptEditorView,
      @Nonnull final ScriptRunner scriptRunner,
      @Nonnull @Named("scriptLangName") final String scriptLangName,
      @Nonnull final StringClipper stringClipper) {
    this.scriptEditorView = scriptEditorView;
    this.scriptEditor = scriptEditorView.getScriptEditor();
    this.scriptRunner = scriptRunner;
    this.scriptLangName = scriptLangName;
    this.stringClipper = stringClipper;

    LOGGER.log(Level.FINE, "factory: " + preferencesServiceFactory);

    final PreferencesService preferencesService
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

    LOGGER.log(Level.FINE, "Running with language: " + scriptLangName);
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
  private void runFile(final ActionEvent actionEvent) {
    final Thread thread = LoggerUtilities.newLoggingThread(LOGGER, this::runEditorContent);
    thread.setDaemon(true);
    thread.start();
  }

  @FXML
  private void publishFile(final ActionEvent actionEvent) {
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
        final String remote;
        final String relativePath;

        if (gist == null) {
          remote = manualRemote;
          relativePath = manualFile;
        } else {
          final File currentFile = ScriptingEngine.fileFromGit(
              gist.getGitPushUrl(),
              gistFile.getFileName()
          );

          final Git git = ScriptingEngine.locateGit(currentFile);
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
      } catch (final Exception e) {
        LOGGER.log(Level.SEVERE,
            "Could not commit.\n" + Throwables.getStackTraceAsString(e));
        Platform.runLater(() -> Notifications.create()
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
    final NewGistDialog dialog = new NewGistDialog();
    dialog.showAndWait().ifPresent(result -> {
      try {
        //Make a new gist
        final GHGist newGist = GistUtilities.createNewGist(
            dialog.getName(),
            dialog.getDescription(),
            dialog.isPublic()
        );

        final PublishDialog publishDialog = new PublishDialog();
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
          } catch (final Exception e) {
            LOGGER.log(Level.SEVERE,
                "Could not push code.\n" + Throwables.getStackTraceAsString(e));
          }
        });
      } catch (final IOException e) {
        LOGGER.log(Level.SEVERE,
            "Could not create new gist.\n" + Throwables.getStackTraceAsString(e));
      }
    });
  }

  @FXML
  private void onCopyGist(final ActionEvent actionEvent) {
    //Put gist URL on system clipboard
    final ClipboardContent content = new ClipboardContent();
    content.putString(gistURLField.getText());
    Clipboard.getSystemClipboard().setContent(content);
  }

  /**
   * Load a file from disk and insert its content into the editor.
   *
   * @param file File to load
   */
  public void loadFile(@Nonnull final File file) {
    try {
      scriptEditor.setText(Files.toString(file, Charset.forName("UTF-8")));
      if (file.getName().endsWith(".xml")) {
        scriptLangName = "MobilBaseXML";
      } else if (file.getName().endsWith(".groovy")) {
        scriptLangName = "BowlerGroovy";
      }
    } catch (final IOException e) {
      LOGGER.log(Level.SEVERE,
          "Could not load file: " + file.getAbsolutePath() + ".\n"
              + Throwables.getStackTraceAsString(e));
    }
  }

  /**
   * Load a file from a gist.
   *
   * @param gist Parent gist
   * @param gistFile File in gist
   */
  public void loadGist(@Nonnull final GHGist gist, @Nonnull final GHGistFile gistFile) {
    isScratchpad = false;
    final File file;

    try {
      file = ScriptingEngine.fileFromGit(gist.getGitPushUrl(), gistFile.getFileName());

      this.gist = gist;
      this.gistFile = gistFile;

      gistURLField.setText(gist.getGitPushUrl());
      fileNameField.setText(gistFile.getFileName());

      loadFile(file);
    } catch (GitAPIException | IOException e) {
      LOGGER.log(Level.SEVERE,
          "Could get file from git.\n" + Throwables.getStackTraceAsString(e));
    }
  }

  /**
   * Load in a gist manually if a {@link GHGist} is not available.
   *
   * @param pushURL gist push URL
   * @param fileName filename in gist
   * @param file file on disk
   */
  public void loadManualGist(@Nonnull final String pushURL, @Nonnull final String fileName,
      @Nonnull final File file) {
    isScratchpad = false;
    manualRemote = pushURL;
    manualFile = fileName;

    gistURLField.setText(pushURL);
    fileNameField.setText(fileName);

    try {
      scriptEditor.setText(Files.toString(file, Charset.forName("UTF-8")));
    } catch (final IOException e) {
      LOGGER.log(Level.SEVERE,
          "Could not load file: " + file.getAbsolutePath() + ".\n"
              + Throwables.getStackTraceAsString(e));
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
      } catch (final ExecutionException e) {
        LOGGER.log(Level.SEVERE,
            "Could not get text from editor.\n" + Throwables.getStackTraceAsString(e));
      }
    } catch (final InterruptedException e) {
      LOGGER.log(Level.WARNING,
          "CountDownLatch interrupted while waiting to get editor content.\n"
              + Throwables.getStackTraceAsString(e));
    }

    return null;
  }

  /**
   * Run a script from a string in the editor'scale environment.
   *
   * @param script script content
   * @param arguments script arguments
   * @param languageName scripting language name
   * @return script result
   */
  public Object runStringScript(@Nonnull final String script,
      @Nonnull final ArrayList<Object> arguments, //NOPMD
      @Nonnull final String languageName) {
    try {
      //Run the code
      LOGGER.log(Level.FINE, "Running script.");
      final Object result = scriptRunner.runScript(script, arguments, languageName);
      LOGGER.log(Level.FINER, "Result is: " + result);
      return result;
    } catch (final IOException e) {
      LOGGER.log(Level.SEVERE,
          "Could not load CADModelViewer.\n" + Throwables.getStackTraceAsString(e));
    } catch (final GroovyRuntimeException e) {
      LOGGER.log(Level.WARNING,
          "Error in CAD script: " + e.getMessage());
      Platform.runLater(() -> Notifications.create()
          .title("Error in CAD Script")
          .text(stringClipper.clipStringToLines(e.getMessage(), maxToastLength.getValue()))
          .owner(fileEditorRoot)
          .position(Pos.BOTTOM_RIGHT)
          .showInformation());
    } catch (final Exception e) {
      LOGGER.log(Level.SEVERE,
          "Could not run CAD script.\n" + Throwables.getStackTraceAsString(e));
    }

    return null;
  }

  /**
   * Gives the scratchpad code what it needs to work properly.
   *
   * @param tab Tab the editor is contained in
   */
  public void initScratchpad(@Nonnull final Tab tab, @Nonnull final Runnable reloadMenus) {
    this.tab = tab;
    this.reloadMenus = reloadMenus;
  }

  /**
   * Insert the text at the cursor.
   *
   * @param text text to insert
   */
  public void insertAtCursor(final String text) {
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

  public void setScriptLangName(final String scriptLangName) {
    this.scriptLangName = scriptLangName;
  }

}
