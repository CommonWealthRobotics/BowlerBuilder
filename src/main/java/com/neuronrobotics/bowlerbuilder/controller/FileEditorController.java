package com.neuronrobotics.bowlerbuilder.controller;

import com.google.common.base.Throwables;
import com.google.common.io.Files;

import com.neuronrobotics.bowlerbuilder.GistUtilities;
import com.neuronrobotics.bowlerbuilder.LoggerUtilities;
import com.neuronrobotics.bowlerbuilder.controller.aceinterface.AceEditor;
import com.neuronrobotics.bowlerbuilder.view.dialog.NewGistDialog;
import com.neuronrobotics.bowlerbuilder.view.dialog.PublishDialog;
import com.neuronrobotics.bowlerstudio.scripting.ScriptingEngine;

import eu.mihosoft.vrl.v3d.CSG;

import org.controlsfx.glyphfont.FontAwesome;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.kohsuke.github.GHGist;
import org.kohsuke.github.GHGistFile;

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
import javafx.beans.property.ObjectProperty;
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
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

public class FileEditorController implements Initializable {

  @FXML
  private SplitPane root;
  @FXML
  private WebView webView;
  private WebEngine webEngine; //NOPMD
  private AceEditor aceEditor;
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
  private MainWindowController parentController;
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
    webEngine.load(getClass().getResource("../web/ace.html").toString());
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
          }
        });
  }

  @FXML
  private void runFile(ActionEvent actionEvent) {
    Runnable runnable = () -> {
      Thread thread = new Thread(() -> {
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
        } catch (Exception e) {
          LoggerUtilities.getLogger().log(Level.WARNING,
              "Could not run CAD script.\n" + Throwables.getStackTraceAsString(e));
        }
      });
      thread.setDaemon(false);
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

  /**
   * Parse CSGs out of an Object. All CSGs will get added to the supplied controller.
   *
   * @param controller CAD viewer controller
   * @param item       Object with CSGs
   */
  private void parseCSG(CADModelViewerController controller, Object item) {
    if (item instanceof CSG) {
      controller.addMeshesFromCSG((CSG) item);
    } else if (item instanceof List) {
      List itemList = (List) item;
      for (Object elem : itemList) {
        parseCSG(controller, elem);
      }
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
        } catch (RuntimeException e) {
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

  /**
   * Set the font size of this editor.
   *
   * @param fontSize Font size
   */
  public void setFontSize(int fontSize) {
    if (webEngine.getLoadWorker().stateProperty().get() == Worker.State.SUCCEEDED) {
      aceEditor.setFontSize(fontSize);
    } else {
      requestedFontSize = fontSize;
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
   * @param gist     Parent gist
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

}
