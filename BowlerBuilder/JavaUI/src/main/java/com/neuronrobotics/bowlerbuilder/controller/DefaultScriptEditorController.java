/*
 * Copyright 2017 Ryan Benasutti
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.bowlerbuilder.controller;

import com.google.common.base.Throwables;
import com.google.common.io.Files;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.neuronrobotics.bowlerbuilder.FxUtil;
import com.neuronrobotics.bowlerbuilder.GistUtilities;
import com.neuronrobotics.bowlerbuilder.LoggerUtilities;
import com.neuronrobotics.bowlerbuilder.controller.scripting.scripteditorview.ScriptEditorView;
import com.neuronrobotics.bowlerbuilder.model.preferences.Preferences;
import com.neuronrobotics.bowlerbuilder.model.preferences.PreferencesConsumer;
import com.neuronrobotics.bowlerbuilder.model.preferences.bowler.AceScriptEditorPreferences;
import com.neuronrobotics.bowlerbuilder.model.preferences.bowler.DefaultScriptEditorPreferencesService;
import com.neuronrobotics.bowlerbuilder.scripting.scripteditor.ScriptEditor;
import com.neuronrobotics.bowlerbuilder.scripting.scriptrunner.ScriptRunner;
import com.neuronrobotics.bowlerbuilder.util.StringClipper;
import com.neuronrobotics.bowlerbuilder.util.Verified;
import com.neuronrobotics.bowlerbuilder.view.dialog.NewGistDialog;
import com.neuronrobotics.bowlerbuilder.view.dialog.PublishDialog;
import com.neuronrobotics.bowlerstudio.scripting.ScriptingEngine;
import groovy.lang.GroovyRuntimeException;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
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
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import kotlin.Unit;
import org.controlsfx.control.Notifications;
import org.controlsfx.glyphfont.FontAwesome;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.jetbrains.annotations.NotNull;
import org.kohsuke.github.GHGist;
import org.kohsuke.github.GHGistFile;

@ParametersAreNonnullByDefault
public class DefaultScriptEditorController implements PreferencesConsumer {

  private static final Logger LOGGER =
      LoggerUtilities.getLogger(DefaultScriptEditorController.class.getSimpleName());
  private final DefaultScriptEditorPreferencesService preferencesService;
  private final ScriptEditorView scriptEditorView;
  private final ScriptEditor scriptEditor;
  private final ScriptRunner scriptRunner;
  private final StringClipper stringClipper;
  private int maxToastLength;
  private String scriptLangName;
  @FXML private SplitPane fileEditorRoot;
  @FXML private BorderPane editorBorderPane;
  @FXML private Button runButton;
  @FXML private Button publishButton;
  @FXML private TextField fileNameField;
  @FXML private TextField gistURLField;
  private GHGist gist;
  private String manualRemote;
  private GHGistFile gistFile;
  private String manualFile;
  private boolean isScratchpad = true;
  private Tab tab;
  private Runnable reloadMenus;

  /**
   * A script editor.
   *
   * @param preferencesService this class's preferences
   * @param scriptEditorView the script editor view to embed and pull a script editor from
   * @param scriptRunner the script runner to pass script content to
   * @param scriptLangName the scripting language to use
   * @param stringClipper used to clip toast length for error messages
   */
  @Inject
  public DefaultScriptEditorController(
      final DefaultScriptEditorPreferencesService preferencesService,
      final ScriptEditorView scriptEditorView,
      final ScriptRunner scriptRunner,
      @Named("defaultScriptEditorLangName") final String scriptLangName,
      final StringClipper stringClipper) {
    this.preferencesService = preferencesService;
    this.scriptEditorView = scriptEditorView;
    this.scriptEditor = scriptEditorView.getScriptEditor();
    this.scriptRunner = scriptRunner;
    this.scriptLangName = scriptLangName;
    this.stringClipper = stringClipper;
    LOGGER.log(Level.FINE, "Running with language: " + scriptLangName);
  }

  @FXML
  protected void initialize() {
    editorBorderPane.setCenter(scriptEditorView.getView());
    fileEditorRoot.setDividerPosition(0, 0.8);

    runButton.setGraphic(new FontAwesome().create(String.valueOf(FontAwesome.Glyph.PLAY)));
    publishButton.setGraphic(
        new FontAwesome().create(String.valueOf(FontAwesome.Glyph.CLOUD_UPLOAD)));

    refreshPreferences();
  }

  @Override
  public void refreshPreferences() {
    final AceScriptEditorPreferences preferences =
        preferencesService.getCurrentPreferencesOrDefault();
    maxToastLength = preferences.getMaxToastLength();
    scriptEditorView.setFontSize(preferences.getFontSize());
  }

  @NotNull
  @Override
  public Preferences getCurrentPreferences() {
    return preferencesService.getCurrentPreferencesOrDefault();
  }

  @FXML
  private void runFile(final ActionEvent actionEvent) {
    LoggerUtilities.newLoggingThread(LOGGER, this::runEditorContent).start();
  }

  @FXML
  private void publishFile(final ActionEvent actionEvent) {
    if (isScratchpad) {
      publishScratchpad();
    } else {
      publishNormal();
    }
  }

  /** Publish the editor contents normally (not scratchpad). */
  private void publishNormal() {
    new PublishDialog()
        .showAndWait()
        .ifPresent(
            commitMessage -> {
              try {
                if (gist == null) {
                  ScriptingEngine.pushCodeToGit(
                      manualRemote,
                      ScriptingEngine.getFullBranch(manualRemote),
                      manualFile,
                      scriptEditor.getFullText(),
                      commitMessage);
                } else {
                  final File currentFile =
                      ScriptingEngine.fileFromGit(gist.getGitPushUrl(), gistFile.getFileName());

                  final Git git = ScriptingEngine.locateGit(currentFile);
                  final String remote =
                      git.getRepository().getConfig().getString("remote", "origin", "url");

                  ScriptingEngine.pushCodeToGit(
                      git.getRepository().getConfig().getString("remote", "origin", "url"),
                      ScriptingEngine.getFullBranch(remote),
                      ScriptingEngine.findLocalPath(currentFile, git),
                      scriptEditor.getFullText(),
                      commitMessage);
                }
              } catch (final Exception e) {
                LOGGER.log(
                    Level.SEVERE, "Could not commit.\n" + Throwables.getStackTraceAsString(e));
                Platform.runLater(
                    () ->
                        Notifications.create()
                            .title("Commit failed")
                            .text("Could not perform commit. Changes not saved.")
                            .showError());
              }
            });
  }

  /** Publish the editor contents during scratchpad mode. Disables scratchpad mode. */
  private void publishScratchpad() {
    final NewGistDialog dialog = new NewGistDialog();
    dialog
        .showAndWait()
        .ifPresent(
            result -> {
              // Make a new gist
              GistUtilities.createNewGist(
                      dialog.getName(), dialog.getDescription(), dialog.isPublic())
                  .handle(
                      gist -> {
                        final PublishDialog publishDialog = new PublishDialog();
                        publishDialog
                            .showAndWait()
                            .ifPresent(
                                commitMessage ->
                                    commitAndPushScratchpad(dialog.getName(), gist, commitMessage));
                        return Unit.INSTANCE;
                      },
                      e -> {
                        LOGGER.log(
                            Level.SEVERE,
                            "Could not create new gist.\n" + Throwables.getStackTraceAsString(e));
                        return Unit.INSTANCE;
                      });
            });
  }

  /**
   * Commit the scratchpad code and push. Disables scratchpad mode.
   *
   * @param newFileName file name for the script
   * @param gist gist to push to
   * @param commitMessage commit message
   */
  private void commitAndPushScratchpad(
      final String newFileName, final GHGist gist, final String commitMessage) {
    try {
      // Push the new gist
      ScriptingEngine.pushCodeToGit(
          gist.getGitPushUrl(),
          ScriptingEngine.getFullBranch(gist.getGitPushUrl()),
          newFileName,
          scriptEditor.getFullText(),
          commitMessage);

      isScratchpad = false;
      gistURLField.setText(gist.getGitPushUrl());
      fileNameField.setText(newFileName);
      this.gist = gist;
      gistFile = gist.getFiles().get(newFileName);
      tab.setText(newFileName);
      reloadMenus.run();
    } catch (final Exception e) {
      LOGGER.log(Level.SEVERE, "Could not push code.\n" + Throwables.getStackTraceAsString(e));
    }
  }

  @FXML
  private void onCopyGist(final ActionEvent actionEvent) {
    // Put gist URL on system clipboard
    final ClipboardContent content = new ClipboardContent();
    content.putString(gistURLField.getText());
    Clipboard.getSystemClipboard().setContent(content);
  }

  /**
   * Load a file from disk and insert its content into the editor.
   *
   * @param file File to load
   */
  public void loadFile(final File file) {
    try {
      scriptEditor.setText(Files.toString(file, Charset.forName("UTF-8")));
      if (file.getName().endsWith(".xml")) {
        scriptLangName = "MobilBaseXML";
      } else if (file.getName().endsWith(".groovy")) {
        scriptLangName = "BowlerGroovy";
      }
    } catch (final IOException e) {
      LOGGER.log(
          Level.SEVERE,
          "Could not load file: "
              + file.getAbsolutePath()
              + ".\n"
              + Throwables.getStackTraceAsString(e));
    }
  }

  /**
   * Load a file from a gist.
   *
   * @param gist Parent gist
   * @param gistFile File in gist
   */
  public void loadGist(final GHGist gist, final GHGistFile gistFile) {
    isScratchpad = false;
    final File file;

    try {
      file = ScriptingEngine.fileFromGit(gist.getGitPushUrl(), gistFile.getFileName());

      this.gist = gist;
      this.gistFile = gistFile;

      gistURLField.setText(gist.getGitPushUrl());
      fileNameField.setText(gistFile.getFileName());

      loadFile(file);
    } catch (final GitAPIException | IOException e) {
      LOGGER.log(Level.SEVERE, "Could get file from git.\n" + Throwables.getStackTraceAsString(e));
    }
  }

  /**
   * Load in a gist manually if a {@link GHGist} is not available.
   *
   * @param pushURL gist push URL
   * @param fileName filename in gist
   * @param file file on disk
   */
  public void loadManualGist(final String pushURL, final String fileName, final File file) {
    isScratchpad = false;
    manualRemote = pushURL;
    manualFile = fileName;

    gistURLField.setText(pushURL);
    fileNameField.setText(fileName);

    try {
      scriptEditor.setText(Files.toString(file, Charset.forName("UTF-8")));
    } catch (final IOException e) {
      LOGGER.log(
          Level.SEVERE,
          "Could not load file: "
              + file.getAbsolutePath()
              + ".\n"
              + Throwables.getStackTraceAsString(e));
    }
  }

  /**
   * Run the content inside the editor.
   *
   * @return result from the script
   */
  public Optional<Object> runEditorContent() {
    try {
      final String content = FxUtil.returnFX(scriptEditor::getFullText);

      runStringScript(content, null, scriptLangName)
          .fold(
              Optional::ofNullable,
              e -> {
                logScriptFailure(e);
                return Optional.empty();
              });
    } catch (final ExecutionException e) {
      LOGGER.log(
          Level.SEVERE, "Could not get text from editor.\n" + Throwables.getStackTraceAsString(e));
    } catch (final InterruptedException e) {
      LOGGER.log(
          Level.WARNING,
          "CountDownLatch interrupted while waiting to get editor content.\n"
              + Throwables.getStackTraceAsString(e));
    }

    return Optional.empty();
  }

  private void logScriptFailure(final Throwable failure) {
    if (failure instanceof IOException) {
      LOGGER.log(
          Level.SEVERE,
          "Could not load CADModelViewer.\n" + Throwables.getStackTraceAsString(failure));
    } else if (failure instanceof GroovyRuntimeException) {
      LOGGER.log(Level.WARNING, "Error in CAD script: " + failure.getMessage());
      Platform.runLater(
          () ->
              Notifications.create()
                  .title("Error in CAD Script")
                  .text(stringClipper.clipStringToLines(failure.getMessage(), maxToastLength))
                  .owner(fileEditorRoot)
                  .position(Pos.BOTTOM_RIGHT)
                  .showInformation());
    } else {
      LOGGER.log(
          Level.SEVERE, "Could not run CAD script.\n" + Throwables.getStackTraceAsString(failure));
    }
  }

  /**
   * Run a script from a string in the editor's environment.
   *
   * @param script script content
   * @param arguments script arguments
   * @param languageName scripting language name
   * @return script result
   */
  public Verified<Exception, Object> runStringScript(
      final String script, @Nullable final ArrayList<Object> arguments, final String languageName) {
    LOGGER.log(Level.INFO, "Running script.");
    return scriptRunner.runScript(script, arguments, languageName);
  }

  /**
   * Gives the scratchpad code what it needs to work properly.
   *
   * @param tab the tab the editor is contained in
   * @param reloadMenus the runnable run to reload the main Git menus
   */
  public void initScratchpad(final Tab tab, final Runnable reloadMenus) {
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
