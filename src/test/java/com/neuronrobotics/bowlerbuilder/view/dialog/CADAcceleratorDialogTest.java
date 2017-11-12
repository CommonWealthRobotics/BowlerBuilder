package com.neuronrobotics.bowlerbuilder.view.dialog;

import java.util.function.Supplier;
import javafx.scene.control.Dialog;
import javafx.stage.Stage;
import org.testfx.framework.junit5.ApplicationTest;

public class CADAcceleratorDialogTest<T extends Dialog> extends ApplicationTest {

  T dialog;
  private final Supplier<T> dialogSupplier;

  CADAcceleratorDialogTest(Supplier<T> dialogSupplier) {
    this.dialogSupplier = dialogSupplier;
  }

  @Override
  public void start(Stage stage) throws Exception {
    dialog = dialogSupplier.get();
    stage.setScene(dialog.getDialogPane().getScene());
    stage.show();
  }

}
