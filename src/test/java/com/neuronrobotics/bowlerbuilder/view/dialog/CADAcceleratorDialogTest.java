package com.neuronrobotics.bowlerbuilder.view.dialog;

import com.neuronrobotics.bowlerbuilder.AutoClosingApplicationTest;
import java.util.function.Supplier;
import javafx.scene.control.Dialog;
import javafx.stage.Stage;

public class CADAcceleratorDialogTest<T extends Dialog> extends AutoClosingApplicationTest {

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
