package com.neuronrobotics.bowlerbuilder.controller; //NOPMD

import static com.neuronrobotics.bowlerstudio.scripting.ScriptingEngine.hasNetwork;

import com.google.common.base.Throwables;
import com.neuronrobotics.bowlerbuilder.GistUtilities;
import com.neuronrobotics.bowlerbuilder.LoggerUtilities;
import com.neuronrobotics.bowlerbuilder.controller.view.FileEditorTab;
import com.neuronrobotics.bowlerbuilder.controller.view.PreferencesController;
import com.neuronrobotics.bowlerstudio.scripting.ScriptingEngine;
import com.neuronrobotics.bowlerstudio.vitamins.Vitamins;
import com.neuronrobotics.sdk.util.ThreadUtil;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.logging.Level;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.PasswordField;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebView;
import org.apache.commons.io.FileUtils;
import org.controlsfx.control.Notifications;
import org.controlsfx.glyphfont.FontAwesome;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.kohsuke.github.GHGist;
import org.kohsuke.github.GHGistFile;
import org.kohsuke.github.GHMyself;
import org.kohsuke.github.GHOrganization;
import org.kohsuke.github.GHPersonSet;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.PagedIterable;

public class MainWindowController implements Initializable {

  @FXML
  private BorderPane root;
  @FXML
  private MenuItem logOut;
  @FXML
  private Menu myGists;
  @FXML
  private Menu myOrgs;
  @FXML
  private Menu myRepos;
  @FXML
  private Menu cadVitamins;
  @FXML
  private TabPane tabPane;
  @FXML
  private Tab homeTab;
  @FXML
  private SplitPane splitPane;

  @FXML
  private Button backPageButton;
  @FXML
  private Button nextPageButton;
  @FXML
  private Button reloadPageButton;
  @FXML
  private Button homePageButton;
  @FXML
  private TextField urlField;
  @FXML
  private WebView homeWebView;

  @FXML
  private TextArea console;

  //Open file editors
  private final List<FileEditorController> fileEditors;
  private Map<String, Object> preferences;

  public MainWindowController() {
    fileEditors = new ArrayList<>();
    preferences = new HashMap<>();
    preferences.put("Font Size", 14); //TODO: Load previous font size preference
  }

  //Simple stream to append input characters to a text area
  private static class TextAreaPrintStream extends OutputStream {

    private final TextArea textArea;

    public TextAreaPrintStream(TextArea textArea) {
      this.textArea = textArea;
    }

    @Override
    public void write(int character) {
      Platform.runLater(() -> textArea.appendText(String.valueOf((char) character)));
    }
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    //Add date to console
    console.setText(console.getText()
        + new SimpleDateFormat(
        "HH:mm:ss, MM dd, yyyy",
        new Locale("en", "US")).format(new Date())
        + "\n");

    //Redirect output to console
    PrintStream stream = null;
    try {
      stream = new PrintStream(new TextAreaPrintStream(console), true, "UTF-8");
    } catch (UnsupportedEncodingException e) {
      LoggerUtilities.getLogger().log(Level.WARNING, "UTF-8 encoding unsupported.");
    }
    System.setOut(stream);
    System.setErr(stream);

    backPageButton.setGraphic(FontAwesome.Glyph.ARROW_LEFT.create());
    nextPageButton.setGraphic(FontAwesome.Glyph.ARROW_RIGHT.create());
    reloadPageButton.setGraphic(FontAwesome.Glyph.REFRESH.create());
    homePageButton.setGraphic(FontAwesome.Glyph.HOME.create());

    loadPage("http://commonwealthrobotics.com/BowlerStudio/Welcome-To-BowlerStudio/");

    SplitPane.setResizableWithParent(console, false);

    try {
      ScriptingEngine.runLogin();
      if (ScriptingEngine.isLoginSuccess() && hasNetwork()) {
        //showLoginNotification();
        setupMenusOnLogin();
      }
    } catch (IOException e) {
      LoggerUtilities.getLogger().log(Level.WARNING,
          "Could not automatically log in.\n" + Throwables.getStackTraceAsString(e));
    }
  }

  @FXML
  private void onOpenScratchpad(ActionEvent actionEvent) {
    FileEditorTab tab;
    FXMLLoader loader = new FXMLLoader(MainWindowController.class.getResource(
        "/com/neuronrobotics/bowlerbuilder/view/FileEditor.fxml"));

    try {
      Node node = loader.load();
      final FileEditorController controller = loader.getController();
      fileEditors.add(controller);

      tab = new FileEditorTab("Scratchpad", controller);
      tab.setContent(node);

      controller.setFontSize((int) preferences.get("Font Size"));
      controller.initScratchpad(tab, this::reloadGitMenus);

      tab.setOnCloseRequest(event -> fileEditors.remove(controller));

      tabPane.getTabs().add(tab);
      tabPane.getSelectionModel().select(tab);
    } catch (IOException e) {
      LoggerUtilities.getLogger().log(Level.SEVERE,
          "Could not load FileEditor.fxml.\n" + Throwables.getStackTraceAsString(e));
    }
  }

  /**
   * Open a gist file in the file editor.
   *
   * @param gist Gist containing file
   * @param gistFile File
   */
  public void openGistFileInEditor(GHGist gist, GHGistFile gistFile) {
    FileEditorTab tab;
    FXMLLoader loader = new FXMLLoader(MainWindowController.class.getResource(
        "/com/neuronrobotics/bowlerbuilder/view/FileEditor.fxml"));

    try {
      Node node = loader.load();
      final FileEditorController controller = loader.getController();
      fileEditors.add(controller);

      tab = new FileEditorTab(gistFile.getFileName(), controller);
      tab.setContent(node);

      controller.setFontSize((int) preferences.get("Font Size"));
      controller.loadGist(gist, gistFile);

      tab.setOnCloseRequest(event -> fileEditors.remove(controller));

      tabPane.getTabs().add(tab);
      tabPane.getSelectionModel().select(tab);
    } catch (IOException e) {
      LoggerUtilities.getLogger().log(Level.SEVERE,
          "Could not load FileEditor.fxml.\n" + Throwables.getStackTraceAsString(e));
    }
  }

  @FXML
  private void onExitProgram(ActionEvent actionEvent) {
    saveAndQuit();
  }

  @FXML
  private void onLogInToGitHub(ActionEvent actionEvent) {
    tryLogin();
  }

  @FXML
  private void onBackPage(ActionEvent actionEvent) {
    Platform.runLater(() -> homeWebView.getEngine().executeScript("history.back()"));
  }

  @FXML
  private void onNextPage(ActionEvent actionEvent) {
    Platform.runLater(() -> homeWebView.getEngine().executeScript("history.forward()"));
  }

  @FXML
  private void onReloadPage(ActionEvent actionEvent) {
    homeWebView.getEngine().reload();
  }

  @FXML
  private void onHomePage(ActionEvent actionEvent) {
    loadPage("http://commonwealthrobotics.com/BowlerStudio/Welcome-To-BowlerStudio/");
  }

  @FXML
  private void onNavigate(ActionEvent actionEvent) {
    String url = urlField.getText();

    if (!url.toLowerCase(Locale.ENGLISH).matches("^\\w+://.*")) {
      url = String.format("http://%s", url);
    }

    loadPage(url);
  }

  private void loadPage(String url) {
    homeWebView.getEngine().load(url);
    urlField.setText(url);
  }

  private void tryLogin() {
    ScriptingEngine.setLoginManager(s -> {
      VBox vBox = new VBox();
      TextField nameField = new TextField();
      PasswordField passField = new PasswordField();

      nameField.setPromptText("Username");
      passField.setPromptText("Password");

      vBox.setSpacing(5);
      vBox.getChildren().addAll(nameField, passField);

      Dialog<Boolean> dialog = new Dialog<>();
      dialog.getDialogPane().setContent(vBox);
      dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

      dialog.setResultConverter(buttonType -> !buttonType.getButtonData().isCancelButton());

      if (dialog.showAndWait().isPresent() && dialog.showAndWait().get()) {
        return new String[]{nameField.getText(), passField.getText()};
      } else {
        return new String[0];
      }
    });

    try {
      ScriptingEngine.waitForLogin();
      if (ScriptingEngine.isLoginSuccess() && hasNetwork()) {
        //showLoginNotification();
        setupMenusOnLogin();
      }
    } catch (IOException e) {
      LoggerUtilities.getLogger().log(Level.WARNING,
          "Could not launch GitHub as non-anonymous.\n" + Throwables.getStackTraceAsString(e));
      try {
        ScriptingEngine.setupAnyonmous();
      } catch (IOException e1) {
        LoggerUtilities.getLogger().log(Level.WARNING,
            "Could not launch GitHub anonymous.\n" + Throwables.getStackTraceAsString(e));
      }
    } catch (GitAPIException e) {
      LoggerUtilities.getLogger().log(Level.WARNING,
          "Could not log in.\n" + Throwables.getStackTraceAsString(e));
    }
  }

  /**
   * Show a GitHub login toast.
   */
  private void showLoginNotification() {
    Platform.runLater(() -> {
      try {
        Notifications.create() //TODO: Weird exception
            .title("Login Success")
            .text(ScriptingEngine.getGithub().getMyself().getLogin())
            .show();
      } catch (IOException e) {
        LoggerUtilities.getLogger().log(Level.WARNING,
            "Unable to get GitHub.\n" + Throwables.getStackTraceAsString(e));
      }
    });
  }

  /**
   * Setup the menus for the main menu bar.
   */
  private void setupMenusOnLogin() {
    try {
      ScriptingEngine.setAutoupdate(true);
    } catch (IOException e) {
      LoggerUtilities.getLogger().log(Level.WARNING,
          "Could not set auto update.\n" + Throwables.getStackTraceAsString(e));
    }

    logOut.setDisable(false);

    reloadGitMenus();
    reloadCadMenus();
  }

  /**
   * Reload the GitHub-related menus.
   */
  public void reloadGitMenus() {
    //Wait for GitHub to load in
    GitHub gitHub;
    while ((gitHub = ScriptingEngine.getGithub()) == null) {
      ThreadUtil.wait(20);
    }

    myGists.getItems().clear();
    myOrgs.getItems().clear();
    myRepos.getItems().clear();

    GHMyself myself;
    try {
      myself = gitHub.getMyself();

      LoggerUtilities.newLoggingThread(() -> {
        try {
          loadGistsIntoMenus(myGists, myself.listGists());
        } catch (IOException e) {
          LoggerUtilities.getLogger().log(Level.SEVERE,
              "Unable to list gists.\n" + Throwables.getStackTraceAsString(e));
        }
      }).start();

      LoggerUtilities.newLoggingThread(() -> {
        try {
          loadOrgsIntoMenus(myOrgs, myself.getAllOrganizations());
        } catch (IOException e) {
          LoggerUtilities.getLogger().log(Level.SEVERE,
              "Unable to get organizations.\n" + Throwables.getStackTraceAsString(e));
        }
      }).start();

      LoggerUtilities.newLoggingThread(() ->
          loadReposIntoMenus(myRepos, myself.listRepositories())).start();
    } catch (IOException e) {
      LoggerUtilities.getLogger().log(Level.SEVERE,
          "Could not get GitHub.\n" + Throwables.getStackTraceAsString(e));
    }
  }

  /**
   * Reload the CAD menus.
   */
  public void reloadCadMenus() {
    LoggerUtilities.newLoggingThread(() ->
        Vitamins.listVitaminTypes().forEach(vitamin -> {

          Menu vitaminMenu = new Menu(vitamin);

          Vitamins.listVitaminSizes(vitamin).forEach(size -> {
            MenuItem sizeMenu = new MenuItem(size);

            sizeMenu.setOnAction(event1 -> {
              Tab selection = tabPane.getSelectionModel().getSelectedItem();
              if (selection instanceof FileEditorTab) {
                FileEditorTab editorTab = (FileEditorTab) selection;
                String insertion =
                    "CSG foo = Vitamins.get(\"" + vitamin + "\", \"" + size + "\");";
                editorTab.getController().insertAtCursor(insertion);
              }
            });

            vitaminMenu.getItems().add(sizeMenu);
          });

          cadVitamins.getItems().add(vitaminMenu);
        })).start();
  }

  /**
   * Load gists into menus for the main menu bar.
   *
   * @param menu menu to put submenus into
   * @param gists list of gists
   */
  private void loadGistsIntoMenus(Menu menu, PagedIterable<GHGist> gists) {
    gists.forEach(gist -> {
      MenuItem showWebGist = new MenuItem("Show Gist on Web");
      showWebGist.setOnAction(event -> {
        WebView webView = new WebView();
        webView.getEngine().load(gist.getHtmlUrl());
        Tab tab = new Tab(gist.getDescription(), webView);
        tabPane.getTabs().add(tab);
        tabPane.getSelectionModel().select(tab);
      });

      MenuItem addFileToGist = new MenuItem("Add File");
      addFileToGist.setOnAction(event -> Platform.runLater(() -> {
        try {
          //TODO: Maybe addFileToGist is broken
          GHGist newGist = GistUtilities.addFileToGist("test name", "test content", gist);
          openGistFileInEditor(newGist, newGist.getFile("test name"));
        } catch (Exception e) {
          LoggerUtilities.getLogger().log(Level.WARNING,
              "Could not get files in git.\n" + Throwables.getStackTraceAsString(e));
        }
      }));

      String gistMenuText = gist.getDescription();
      if (gistMenuText == null || gistMenuText.length() == 0) {
        Set<String> filenames = gist.getFiles().keySet();
        if (filenames.size() >= 1) {
          gistMenuText = filenames.iterator().next();
        } else {
          gistMenuText = "";
        }
      }

      //Cap length to 15
      gistMenuText = gistMenuText.substring(0, Math.min(15, gistMenuText.length()));

      Menu gistMenu = new Menu(gistMenuText);
      gistMenu.getItems().addAll(showWebGist, addFileToGist);

      gist.getFiles().forEach((name, gistFile) -> {
        MenuItem gistFileItem = new MenuItem(name);
        gistFileItem.setOnAction(event -> openGistFileInEditor(gist, gistFile));
        gistMenu.getItems().add(gistFileItem);
      });

      menu.getItems().add(gistMenu);
    });
  }

  /**
   * Load organizations into menus for the main menu bar.
   *
   * @param menu menu to put submenus into
   * @param orgs organizations
   */
  private void loadOrgsIntoMenus(Menu menu, GHPersonSet<GHOrganization> orgs) {
    orgs.forEach(org -> {
      try {
        String name = org.getName();
        if (name == null || name.length() == 0) {
          name = org.getLogin();
        }

        Menu orgMenu = new Menu(name);
        org.getRepositories().forEach((key, value) -> {
          MenuItem repoMenu = new MenuItem(key);
          repoMenu.setOnAction(__ -> homeWebView.getEngine().load(value.gitHttpTransportUrl()));
          orgMenu.getItems().add(repoMenu);
        });
        orgMenu.setOnAction(event -> homeWebView.getEngine().load(org.getHtmlUrl()));
        menu.getItems().add(orgMenu);
      } catch (IOException e) {
        LoggerUtilities.getLogger().log(Level.WARNING,
            "Unable to get name of organization.\n" + Throwables.getStackTraceAsString(e));
      }
    });
  }

  /**
   * Load repositories into menus for the main menu bar.
   *
   * @param menu menu to put submenus into
   * @param repos repositories
   */
  private void loadReposIntoMenus(Menu menu, PagedIterable<GHRepository> repos) {
    repos.forEach(repo -> {
      MenuItem menuItem = new MenuItem(repo.getName());
      menuItem.setOnAction(event -> homeWebView.getEngine().load(repo.gitHttpTransportUrl()));
      menu.getItems().add(menuItem);
    });
  }

  @FXML
  private void onLogOutFromGitHub(ActionEvent actionEvent) {
    try {
      ScriptingEngine.logout();
      logOut.setDisable(true);
      myGists.getItems().clear();
      myOrgs.getItems().clear();
      myRepos.getItems().clear();
    } catch (IOException e) {
      LoggerUtilities.getLogger().log(Level.WARNING,
          "Could not log out from GitHub.\n" + Throwables.getStackTraceAsString(e));
    }
  }

  @FXML
  private void onDeleteLocalCache(ActionEvent actionEvent) {
    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);

    alert.setTitle("Confirm Deletion");
    alert.setHeaderText("Delete All Local Files and Quit?");
    alert.setContentText("Deleting the cache will remove unsaved work and quit. Are you sure?");

    if (alert.showAndWait().isPresent() && alert.getResult() == ButtonType.OK) {
      new Thread(() -> {
        Thread.currentThread().setName("Delete Cache Thread");

        try {
          FileUtils.deleteDirectory(
              new File(
                  ScriptingEngine.getWorkspace().getAbsolutePath() + "/gistcache/"));
        } catch (IOException e) {
          LoggerUtilities.getLogger().log(Level.WARNING,
              "Unable to delete cache.\n" + Throwables.getStackTraceAsString(e));
        }

        Platform.runLater(this::quit);
      }).start();
    }
  }

  @FXML
  private void openPreferences(ActionEvent actionEvent) {
    FXMLLoader loader = new FXMLLoader(MainWindowController.class.getResource(
        "/com/neuronrobotics/bowlerbuilder/view/Preferences.fxml"));
    Dialog dialog = new Dialog();
    dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CLOSE);

    try {
      dialog.getDialogPane().setContent(loader.load());
      PreferencesController controller = loader.getController();
      controller.setPreferences(preferences);
      dialog.showAndWait();
      preferences = controller.getPreferences();
      if (controller.getPreferences().containsKey("Font Size")) {
        fileEditors.forEach(elem ->
            elem.setFontSize((Integer) controller.getPreferences().get("Font Size")));
      }
    } catch (IOException e) {
      LoggerUtilities.getLogger().log(Level.SEVERE,
          "Could not load Preferences.fxml.\n" + Throwables.getStackTraceAsString(e));
    }
  }

  @FXML
  private void openEditorHelp(ActionEvent actionEvent) {
    FXMLLoader loader = new FXMLLoader(MainWindowController.class.getResource(
        "/com/neuronrobotics/bowlerbuilder/view/dialog/EditorHelp.fxml"));
    Dialog dialog = new Dialog();
    dialog.setTitle("BowlerBuilder Help");
    dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

    try {
      dialog.getDialogPane().setContent(loader.load());
      dialog.showAndWait();
    } catch (IOException e) {
      LoggerUtilities.getLogger().log(Level.SEVERE,
          "Could not load EditorHelp.fxml.\n" + Throwables.getStackTraceAsString(e));
    }
  }

  /**
   * Save work and quit.
   */
  public void saveAndQuit() {
    //Save here
    quit();
  }

  /**
   * Quit the application.
   */
  private void quit() {
    root.getScene().getWindow().hide();
    Platform.exit();
  }

}
