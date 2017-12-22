package com.neuronrobotics.bowlerbuilder.controller;

import com.google.common.base.Throwables;
import com.google.common.io.Files;
import com.google.inject.Inject;
import com.neuronrobotics.bowlerbuilder.GistUtilities;
import com.neuronrobotics.bowlerbuilder.LoggerUtilities;
import com.neuronrobotics.bowlerbuilder.controller.scripteditor.ScriptEditor;
import com.neuronrobotics.bowlerbuilder.controller.scripteditor.ScriptEditorView;
import com.neuronrobotics.bowlerbuilder.controller.scripteditor.ace.AceEditorView;
import com.neuronrobotics.bowlerbuilder.view.dialog.NewGistDialog;
import com.neuronrobotics.bowlerbuilder.view.dialog.PublishDialog;
import com.neuronrobotics.bowlerstudio.scripting.ScriptingEngine;
import eu.mihosoft.vrl.v3d.CSG;
import groovy.lang.GroovyRuntimeException;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
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

public class FileEditorController {

  private static final Logger logger =
      LoggerUtilities.getLogger(FileEditorController.class.getSimpleName());
  private final ScriptEditorView scriptEditorView;
  private final ScriptEditor scriptEditor;
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
  @FXML
  private CADModelViewerController cadviewerController;

  private GHGist gist;
  private GHGistFile gistFile;

  private boolean isScratchpad = true;
  private Tab tab;
  private Runnable reloadMenus;

  @Inject
  public FileEditorController(ScriptEditorView scriptEditorView) {
    this.scriptEditorView = scriptEditorView;
    this.scriptEditor = scriptEditorView.getScriptEditor();
  }

  @FXML
  protected void initialize() {
    editorBorderPane.setCenter(scriptEditorView.getView());

    fileEditorRoot.setDividerPosition(0, 0.8);

    runButton.setGraphic(new FontAwesome().create(String.valueOf(FontAwesome.Glyph.PLAY)));
    publishButton.setGraphic(
        new FontAwesome().create(String.valueOf(FontAwesome.Glyph.CLOUD_UPLOAD)));
  }

  @FXML
  private void runFile(ActionEvent actionEvent) {
    Runnable runnable = () -> {
      Thread thread = LoggerUtilities.newLoggingThread(logger, () -> {
        try {
          //Grab code from FX thread
          ObjectProperty<String> text = new SimpleObjectProperty<>();
          CountDownLatch latch = new CountDownLatch(1);
          Platform.runLater(() -> {
            text.set(scriptEditor.getText());
            latch.countDown();
          });
          latch.await();

          //Run the code
          logger.log(Level.FINE, "Running script.");
          Object result = ScriptingEngine.inlineScriptStringRun(
              text.get(),
              new ArrayList<>(),
              "Groovy");

          logger.log(Level.FINER, "Result is: " + result);

          //Add CSGs
          logger.log(Level.FINE, "Parsing result.");
          CountDownLatch latch2 = new CountDownLatch(1);
          Platform.runLater(() -> {
            cadviewerController.clearMeshes();
            parseCSG(cadviewerController, result);
            latch2.countDown();
          });
          latch2.await();
        } catch (IOException e) {
          logger.log(Level.SEVERE,
              "Could not load CADModelViewer.\n" + Throwables.getStackTraceAsString(e));
        } catch (GroovyRuntimeException e) {
          logger.log(Level.WARNING,
              "Error in CAD script: " + e.getMessage());
          Platform.runLater(() -> Notifications.create()
              .title("Error in CAD Script")
              .text(e.getMessage())
              .owner(fileEditorRoot)
              .position(Pos.BOTTOM_RIGHT)
              .showInformation());
        } catch (Exception e) {
          logger.log(Level.SEVERE,
              "Could not run CAD script.\n" + Throwables.getStackTraceAsString(e));
        }
      });
      thread.setDaemon(true);
      thread.start();
    };

    runnable.run();
  }

  @FXML
  private void publishFile(ActionEvent actionEvent) {
    if (isScratchpad) {
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
    } else {
      PublishDialog dialog = new PublishDialog();
      dialog.showAndWait().ifPresent(commitMessage -> {
        try {
          File currentFile = ScriptingEngine.fileFromGit(
              gist.getGitPushUrl(),
              gistFile.getFileName()
          );
          Git git = ScriptingEngine.locateGit(currentFile);
          String remote = git.getRepository().getConfig().getString("remote", "origin", "url");
          String relativePath = ScriptingEngine.findLocalPath(currentFile, git);

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
        }
      });
    }
  }

  @FXML
  private void onCopyGist(ActionEvent actionEvent) {
    //Put gist URL on system clipboard
    ClipboardContent content = new ClipboardContent();
    content.putString(gistURLField.getText());
    Clipboard.getSystemClipboard().setContent(content);
  }

  /**
   * Set the font size of this editor.
   *
   * @param fontSize font size object ({@link IntegerProperty} or {@link Integer})
   */
  public void setFontSize(Object fontSize) {
    if (fontSize instanceof IntegerProperty) {
      setFontSize(((IntegerProperty) fontSize).getValue());
    } else if (fontSize instanceof Integer) {
      setFontSize((Integer) fontSize);
    }
  }

  /**
   * Set the font size of this editor.
   *
   * @param fontSize font size
   */
  public void setFontSize(Integer fontSize) {
    scriptEditor.setFontSize(fontSize);
  }

  /**
   * Parse CSGs out of an Object. All CSGs will get added to the supplied controller.
   *
   * @param controller CAD viewer controller
   * @param item object with CSGs
   */
  private void parseCSG(CADModelViewerController controller, Object item) {
    if (item instanceof CSG) {
      controller.addCSG((CSG) item);
    } else if (item instanceof List) {
      List itemList = (List) item;
      for (Object elem : itemList) {
        parseCSG(controller, elem);
      }
    }
  }

  /**
   * Load a file from disk and insert its content into the editor.
   *
   * @param file File to load
   */
  protected void loadFile(File file) {
    if (file != null) {
      try {
        scriptEditor.insertAtCursor(Files.toString(file, Charset.forName("UTF-8")));
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
    File file = null;
    try {
      file = ScriptingEngine.fileFromGit(gist.getGitPushUrl(), gistFile.getFileName());
      this.gist = gist;
      this.gistFile = gistFile;
      gistURLField.setText(gist.getGitPushUrl());
      fileNameField.setText(gistFile.getFileName());
    } catch (GitAPIException | IOException e) {
      logger.log(Level.SEVERE,
          "Could get file from git.\n" + Throwables.getStackTraceAsString(e));
    }

    loadFile(file);
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

  public CADModelViewerController getCADViewerController() {
    return cadviewerController;
  }
}
