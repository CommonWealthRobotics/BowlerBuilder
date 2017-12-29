package com.neuronrobotics.bowlerbuilder.view.dialog;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.neuronrobotics.bowlerbuilder.AutoClosingApplicationTest;
import com.neuronrobotics.bowlerbuilder.FxHelper;
import com.neuronrobotics.bowlerbuilder.model.preferences.PreferenceListener;
import com.neuronrobotics.bowlerbuilder.model.preferences.PreferencesService;
import java.util.Collections;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;

public class PreferencesDialogTest extends AutoClosingApplicationTest {

  private Stage stage;

  @Override
  public void start(Stage stage) {
    this.stage = stage;
  }

  @Test
  void valueChangedTest() {
    PreferencesService service = new PreferencesService("");
    service.set("a", "foo");
    service.set("b", 99);

    FxHelper.runAndWait(() -> {
      PreferencesDialog dialog = new PreferencesDialog(Collections.singletonList(service));
      stage.setScene(dialog.getDialogPane().getScene());
      stage.show();
    });

    doubleClickOn("foo").write("bar");
    doubleClickOn("99").write("98");
    clickOn("OK");

    assertEquals("bar", service.get("a", "baz"));
    assertEquals(new Integer(98), service.get("b", 100));
  }

  @Test
  void listenerTest() {
    PreferencesService service = new PreferencesService("");
    service.set("a", "foo");
    BooleanProperty val = new SimpleBooleanProperty(false);
    service.addListener("a", (PreferenceListener<String>) (oldVal, newVal) ->
        val.setValue(true));

    FxHelper.runAndWait(() -> {
      PreferencesDialog dialog = new PreferencesDialog(Collections.singletonList(service));
      stage.setScene(dialog.getDialogPane().getScene());
      stage.show();
    });

    doubleClickOn("foo").write("bar");
    clickOn("OK");

    assertTrue(val.getValue());
  }

}
