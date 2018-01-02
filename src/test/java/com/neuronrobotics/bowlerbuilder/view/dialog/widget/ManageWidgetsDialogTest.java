package com.neuronrobotics.bowlerbuilder.view.dialog.widget;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.neuronrobotics.bowlerbuilder.AutoClosingApplicationTest;
import com.neuronrobotics.bowlerbuilder.controller.widget.Widget;
import javafx.collections.FXCollections;
import javafx.scene.control.ListView;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;

public class ManageWidgetsDialogTest extends AutoClosingApplicationTest {

  private ManageWidgetsDialog dialog;

  @Override
  public void start(Stage stage) throws Exception {
    dialog = new ManageWidgetsDialog(FXCollections.observableArrayList());
    stage.setScene(dialog.getDialogPane().getScene());
    stage.show();
  }

  @Test
  void addWidgetTest() {
    dialog.setWidgets(FXCollections.observableArrayList(new Widget("url", "name")));
    clickOn("OK");

    assertEquals(1, dialog.getResult().size());
  }

  @Test
  void removeWidgetTest() {
    dialog.setWidgets(FXCollections.observableArrayList(new Widget("url", "name")));
    ((ListView<Widget>) lookup("#widgetList").query()).getSelectionModel().selectFirst();
    clickOn("#removeWidget").clickOn("OK");

    assertEquals(0, dialog.getResult().size());
  }

}
