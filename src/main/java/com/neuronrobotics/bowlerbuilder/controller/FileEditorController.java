package com.neuronrobotics.bowlerbuilder.controller;

import com.google.common.base.Throwables;
import com.google.common.io.Files;
import com.neuronrobotics.bowlerbuilder.GistUtilities;
import com.neuronrobotics.bowlerbuilder.LoggerUtilities;
import com.neuronrobotics.bowlerbuilder.controller.scripteditor.AceEditor;
import com.neuronrobotics.bowlerbuilder.view.dialog.NewGistDialog;
import com.neuronrobotics.bowlerbuilder.view.dialog.PublishDialog;
import com.neuronrobotics.bowlerstudio.scripting.ScriptingEngine;
import eu.mihosoft.vrl.v3d.CSG;
import groovy.lang.GroovyRuntimeException;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Level;
import javafx.application.Platform;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TextField;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import org.controlsfx.glyphfont.FontAwesome;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.kohsuke.github.GHGist;
import org.kohsuke.github.GHGistFile;

public class FileEditorController implements Initializable {

  @FXML
  private SplitPane root;
  @FXML
  private WebView webView;
  private WebEngine webEngine; //NOPMD
  private AceEditor aceEditor;
  private int line;
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

  private int requestedFontSize;
  private Optional<File> requestedFile;

  private GHGist gist;
  private GHGistFile gistFile;

  private boolean isScratchpad = true;
  private Tab tab;
  private Runnable reloadMenus;

  public FileEditorController() {
    requestedFontSize = 14; //TODO: Load previous font size preference
    requestedFile = Optional.empty();
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    root.setDividerPosition(0, 0.8);
    webEngine = webView.getEngine();
    webEngine.setJavaScriptEnabled(true);
    Platform.runLater(() -> webEngine.load(FileEditorController.class.getResource(
        "/com/neuronrobotics/bowlerbuilder/web/ace.html").toString()));
    aceEditor = new AceEditor(webEngine);

    runButton.setGraphic(new FontAwesome().create(String.valueOf(FontAwesome.Glyph.PLAY)));
    publishButton.setGraphic(
        new FontAwesome().create(String.valueOf(FontAwesome.Glyph.CLOUD_UPLOAD)));

    //Stuff to run once the engine is done loading
    webEngine.getLoadWorker().stateProperty().addListener(
        (ObservableValue<? extends Worker.State> observable,
            Worker.State oldValue,
            Worker.State newValue) -> {
          if (newValue == Worker.State.SUCCEEDED) {
            aceEditor.setFontSize(requestedFontSize); //Set font size to the default
            requestedFile.ifPresent(file -> {
              try {
                aceEditor.insertAtCursor(Files.toString(file, Charset.forName("UTF-8")));
              } catch (IOException e) {
                LoggerUtilities.getLogger().log(Level.WARNING,
                    "Could not load file: " + file.getAbsolutePath() + ".\n"
                        + Throwables.getStackTraceAsString(e));
              }
            });

            //Hack to get scrolling to work
            webView.setOnScroll(event -> {
              int length = (int) webView.getEngine().executeScript("editor.session.getLength();");

              line += (int) Math.copySign(5, -1 * event.getDeltaY());
              if (line < 0) {
                line = 0;
              } else if (line > length) {
                line = length;
              }

              webView.getEngine().executeScript(
                  "editor.renderer.scrollCursorIntoView({row: " + line + ", column: 1}, 0.5);");
            });
          }
        });
  }

  @FXML
  private void runFile(ActionEvent actionEvent) {
    Runnable runnable = () -> {
      Thread thread = LoggerUtilities.newLoggingThread(() -> {
        try {
          //Grab code from FX thread
          ObjectProperty<String> text = new SimpleObjectProperty<>();
          CountDownLatch latch = new CountDownLatch(1);
          Platform.runLater(() -> {
            text.set(aceEditor.getText());
            latch.countDown();
          });
          latch.await();

          //Run the code
          Object result = ScriptingEngine.inlineScriptStringRun(
              text.get(),
              new ArrayList<>(),
              "Groovy");

          //Add CSGs
          CountDownLatch latch2 = new CountDownLatch(1);
          Platform.runLater(() -> {
            cadviewerController.clearMeshes();
            parseCSG(cadviewerController, result);
            latch2.countDown();
          });
          latch2.await();
        } catch (IOException e) {
          LoggerUtilities.getLogger().log(Level.SEVERE,
              "Could not load CADModelViewer.\n" + Throwables.getStackTraceAsString(e));
        } catch (GroovyRuntimeException e) {
          LoggerUtilities.getLogger().log(Level.WARNING,
              "Error in CAD script: " + e.getMessage());
        } catch (Exception e) {
          LoggerUtilities.getLogger().log(Level.WARNING,
              "Could not run CAD script.\n" + Throwables.getStackTraceAsString(e));
        }
      });
      thread.setDaemon(true);
      thread.start();
    };

    //Runnable so we don't try to talk to ACE before it exists
    if (webEngine.getLoadWorker().stateProperty().get() == Worker.State.SUCCEEDED) {
      runnable.run();
    } else {
      webEngine.getLoadWorker().stateProperty().addListener((observable, oldValue, newValue) -> {
        if (newValue == Worker.State.SUCCEEDED) {
          runnable.run();
        }
      });
    }
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
                  aceEditor.getText(),
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
              LoggerUtilities.getLogger().log(Level.SEVERE,
                  "Could not push code.\n" + Throwables.getStackTraceAsString(e));
            }
          });
        } catch (IOException e) {
          LoggerUtilities.getLogger().log(Level.SEVERE,
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
              aceEditor.getText(),
              commitMessage
          );
        } catch (Exception e) {
          LoggerUtilities.getLogger().log(Level.WARNING,
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
   * @param fontSize Font size
   */
  public void setFontSize(Property fontSize) {
    if (fontSize instanceof IntegerProperty) {
      if (webEngine.getLoadWorker().stateProperty().get() == Worker.State.SUCCEEDED) {
        aceEditor.setFontSize(((IntegerProperty) fontSize).getValue());
      } else {
        requestedFontSize = ((IntegerProperty) fontSize).getValue();
      }
    }
  }

  /**
   * Parse CSGs out of an Object. All CSGs will get added to the supplied controller.
   *
   * @param controller CAD viewer controller
   * @param item Object with CSGs
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
      if (webEngine.getLoadWorker().stateProperty().get() == Worker.State.SUCCEEDED) {
        try {
          aceEditor.insertAtCursor(Files.toString(file, Charset.forName("UTF-8")));
        } catch (IOException e) {
          LoggerUtilities.getLogger().log(Level.WARNING,
              "Could not load file: " + file.getAbsolutePath() + ".\n"
                  + Throwables.getStackTraceAsString(e));
        }
      } else {
        requestedFile = Optional.of(file);
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
      LoggerUtilities.getLogger().log(Level.WARNING,
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
    aceEditor.insertAtCursor(text);
  }

  public CADModelViewerController getCADViewerController() {
    return cadviewerController;
  }
}
