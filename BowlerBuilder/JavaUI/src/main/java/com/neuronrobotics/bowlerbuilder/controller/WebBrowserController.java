/*
 * Copyright 2017 Ryan Benasutti
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.bowlerbuilder.controller;

import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableList;
import com.neuronrobotics.bowlerbuilder.LoggerUtilities;
import com.neuronrobotics.bowlerbuilder.PublicAPI;
import com.neuronrobotics.bowlerbuilder.view.tab.cadeditor.AceCadEditorTab;
import com.neuronrobotics.bowlerstudio.assets.AssetFactory;
import com.neuronrobotics.bowlerstudio.scripting.ScriptingEngine;
import eu.mihosoft.vrl.v3d.CSG;
import java.io.File;
import java.io.IOException;
import java.util.Locale;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.concurrent.Worker;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.inject.Inject;
import org.controlsfx.glyphfont.Glyph;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.kohsuke.github.GHGist;

@ParametersAreNonnullByDefault
public class WebBrowserController {

  private static final Logger LOGGER =
      LoggerUtilities.getLogger(WebBrowserController.class.getSimpleName());

  private final MainWindowController parentController;
  @FXML private Button backPageButton;
  @FXML private Button nextPageButton;
  @FXML private Button reloadPageButton;
  @FXML private Button homePageButton;
  @FXML private TextField urlField;
  @FXML private WebView webView;
  @FXML private Button runButton;
  @FXML private ImageView runIcon;
  @FXML private Button modifyButton;
  @FXML private ChoiceBox<String> fileBox;
  private final StringProperty currentGistURL = new SimpleStringProperty("currentGist");
  private final StringProperty lastURL = new SimpleStringProperty("");

  @Inject
  public WebBrowserController(final MainWindowController parentController) {
    this.parentController = parentController;
  }

  @FXML
  protected void initialize() {
    backPageButton.setGraphic(new Glyph("FontAwesome", "ARROW_LEFT"));
    nextPageButton.setGraphic(new Glyph("FontAwesome", "ARROW_RIGHT"));
    reloadPageButton.setGraphic(new Glyph("FontAwesome", "REFRESH"));
    homePageButton.setGraphic(new Glyph("FontAwesome", "HOME"));
    runButton.setGraphic(AssetFactory.loadIcon("Run.png"));
    modifyButton.setGraphic(AssetFactory.loadIcon("Edit-Script.png"));

    // Update the url field when a new page gets loaded
    webView
        .getEngine()
        .locationProperty()
        .addListener((observable, oldValue, newValue) -> urlField.setText(newValue));

    webView
        .getEngine()
        .getLoadWorker()
        .stateProperty()
        .addListener(
            (observableValue, oldState, newState) -> {
              if (newState.equals(Worker.State.SUCCEEDED)) {
                final Thread thread =
                    LoggerUtilities.newLoggingThread(LOGGER, this::onPageLoadCallback);
                thread.setDaemon(true);
                thread.start();
              }
            });
  }

  private void onPageLoadCallback() {
    Platform.runLater(() -> setScriptRunnerUIState(true));

    final ImmutableList<String> gists =
        ImmutableList.copyOf(ScriptingEngine.getCurrentGist(lastURL.get(), webView.getEngine()));

    if (gists.isEmpty()) {
      LOGGER.info("No gists found on the current page.");
    }

    // Transform the current page URL into a git URL
    currentGistURL.set(getGitUrlFromPageUrl(lastURL.get(), gists));
    LOGGER.fine("Current gist is: " + currentGistURL.get());

    try {
      // Load files and remove "csgDatabase.json"
      final ImmutableList<String> files =
          ImmutableList.copyOf(
              ScriptingEngine.filesInGit(currentGistURL.get())
                  .stream()
                  .filter(item -> !item.contains("csgDatabase.json"))
                  .collect(Collectors.toList()));

      if (files.isEmpty()) {
        // If there aren't files, just clear the file box
        Platform.runLater(() -> fileBox.getItems().clear());
      } else {
        // If there are files, add them to the fileBox, re-enable the buttons,
        // and load the first file
        loadGitLocal(currentGistURL.get(), files.get(0));

        Platform.runLater(
            () -> {
              setScriptRunnerUIState(false);
              fileBox.getItems().setAll(files);
              fileBox.getSelectionModel().select(0);
            });
      }
    } catch (Exception e) {
      LOGGER.warning("Could not parse and run script.\n" + Throwables.getStackTraceAsString(e));
    }
  }

  /**
   * Maps the URL for a page containing Git resources to the URL for that resource.
   *
   * @param pageUrl The current page URL.
   * @param gists The gists on the current page.
   * @return A Git URL.
   */
  private String getGitUrlFromPageUrl(final String pageUrl, final ImmutableList<String> gists) {
    if (pageUrl.contains("https://github.com/")) {
      if (pageUrl.endsWith("/")) {
        if (pageUrl.endsWith(".git/")) {
          return pageUrl.substring(0, pageUrl.length() - 1);
        } else {
          return pageUrl.substring(0, pageUrl.length() - 1) + ".git";
        }
      } else if (pageUrl.endsWith(".git")) {
        return pageUrl;
      } else {
        return pageUrl + ".git";
      }
    } else {
      return "https://gist.github.com/" + gists.get(0) + ".git";
    }
  }

  /** Sets the disabled state of the run and modify buttons and the file selection box. */
  private void setScriptRunnerUIState(final boolean state) {
    runButton.setDisable(state);
    modifyButton.setDisable(state);
    fileBox.setDisable(state);
  }

  @FXML
  private void onRun(final ActionEvent actionEvent) {
    final Thread thread =
        LoggerUtilities.newLoggingThread(
            LOGGER,
            () -> {
              try {
                final File currentFile =
                    ScriptingEngine.fileFromGit(
                        currentGistURL.get(), fileBox.getSelectionModel().getSelectedItem());

                parseResult(
                    ScriptingEngine.inlineScriptRun(
                        currentFile, null, ScriptingEngine.getShellType(currentFile.getName())));
              } catch (Exception e) {
                LOGGER.warning(
                    "Could not parse and run script.\n" + Throwables.getStackTraceAsString(e));
              }
            });

    thread.setDaemon(true);
    thread.start();
  }

  /**
   * Parse the result of a script. CSG objects get added to a CAD engine.
   *
   * @param result script result
   */
  @SuppressWarnings({"unchecked", "CastCanBeRemovedNarrowingVariableType"})
  private void parseResult(@Nullable final Object result) {
    if (result instanceof Iterable) {
      final Iterable<?> iterable = (Iterable) result;
      final Object firstElement = iterable.iterator().next();
      if (firstElement instanceof CSG) {
        parseCSG((Iterable<CSG>) iterable);
      }
    }
  }

  /**
   * Add CSGs to a new {@link AceCadEditorTab}.
   *
   * @param csgIterable CSGs to add
   */
  private void parseCSG(final Iterable<CSG> csgIterable) {
    Platform.runLater(
        () -> {
          final String selection = fileBox.getSelectionModel().getSelectedItem();

          try {
            final AceCadEditorTab tab = new AceCadEditorTab(selection);
            tab.getController().getDefaultCadModelViewerController().addAllCSGs(csgIterable);

            final GHGist gist =
                ScriptingEngine.getGithub()
                    .getGist(ScriptingEngine.urlToGist(currentGistURL.get()));

            tab.getController()
                .getDefaultScriptEditorController()
                .loadGist(gist, gist.getFile(selection));

            parentController.addTab(tab);
          } catch (IOException e) {
            LOGGER.warning(Throwables.getStackTraceAsString(e));
          }
        });
  }

  @FXML
  private void onModify(final ActionEvent actionEvent) {
    final String selection = fileBox.getSelectionModel().getSelectedItem();
    LOGGER.fine("selection: " + selection);

    try {
      final File currentFile = ScriptingEngine.fileFromGit(currentGistURL.get(), selection);

      // TODO: Doesn't properly detect owner
      final boolean isOwner = ScriptingEngine.checkOwner(currentFile);

      LOGGER.fine("currentFile: " + currentFile);
      LOGGER.fine("isOwner: " + isOwner);

      if (isOwner) {
        LOGGER.fine("Opening file from git in editor: " + currentGistURL.get() + ", " + selection);
        final GHGist gist = ScriptingEngine.getGithub().getGist(currentGistURL.get());
        parentController.openGistFileInEditor(gist, gist.getFile(selection));
      } else {
        LOGGER.info("Forking file from git: " + currentGistURL.get());
        final GHGist gist = ScriptingEngine.fork(ScriptingEngine.urlToGist(currentGistURL.get()));
        currentGistURL.set(gist.getGitPushUrl());
        LOGGER.info("Fork Push URL: " + currentGistURL.get());
        LOGGER.info("Fork done.");
        parentController.openGistFileInEditor(gist, gist.getFile(selection));
      }
    } catch (Exception e) {
      LOGGER.warning("Could not load script.\n" + Throwables.getStackTraceAsString(e));
    }
  }

  private void loadGitLocal(final String currentGist, final String file) {
    LOGGER.fine("currentGistURL: " + currentGist);
    LOGGER.fine("file: " + file);

    try {
      final File currentFile = ScriptingEngine.fileFromGit(currentGist, file);
      final boolean isOwner = ScriptingEngine.checkOwner(currentFile);

      LOGGER.fine("currentFile: " + currentFile);
      LOGGER.fine("isOwner: " + isOwner);

      Platform.runLater(
          () -> {
            if (isOwner) {
              modifyButton.setText("Edit...");
              modifyButton.setGraphic(AssetFactory.loadIcon("Edit-Script.png"));
            } else {
              modifyButton.setText("Make a Copy");
              modifyButton.setGraphic(AssetFactory.loadIcon("Make-Copy-Script.png"));
            }
            try {
              runIcon.setImage(
                  AssetFactory.loadAsset(
                      "Script-Tab-"
                          + ScriptingEngine.getShellType(currentFile.getName() + ".png")));
            } catch (Exception e) {
              LOGGER.warning("Could not load asset.\n" + Throwables.getStackTraceAsString(e));
            }
          });
    } catch (GitAPIException | IOException e) {
      LOGGER.warning(
          "Could not parse file from git: "
              + currentGist
              + ", "
              + file
              + "\n"
              + Throwables.getStackTraceAsString(e));
    }
  }

  public WebEngine getEngine() {
    return webView.getEngine();
  }

  @FXML
  private void onBackPage(final ActionEvent actionEvent) {
    Platform.runLater(() -> webView.getEngine().executeScript("history.back()"));
  }

  @FXML
  private void onNextPage(final ActionEvent actionEvent) {
    Platform.runLater(() -> webView.getEngine().executeScript("history.forward()"));
  }

  @FXML
  private void onReloadPage(final ActionEvent actionEvent) {
    webView.getEngine().reload();
  }

  @FXML
  private void onHomePage(final ActionEvent actionEvent) {
    loadPage("http://commonwealthrobotics.com/BowlerStudio/Welcome-To-BowlerStudio/");
  }

  @FXML
  private void onNavigate(final ActionEvent actionEvent) {
    String url = urlField.getText();

    if (!url.toLowerCase(Locale.ENGLISH).matches("^\\w+://.*")) {
      url = String.format("http://%scale", url);
    }

    loadPage(url);
  }

  @SuppressWarnings("WeakerAccess")
  @PublicAPI
  public void loadPage(final String url) {
    lastURL.set(url);
    webView.getEngine().load(url);
  }
}
