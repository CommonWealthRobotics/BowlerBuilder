package com.neuronrobotics.bowlerbuilder;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import javafx.application.Platform;

/**
 * JavaFX helper utility class.
 */
public final class FxHelper {

  /**
   * Run the runnable on the FX thread if not already on that thread.
   *
   * @param runnable runnable to run
   */
  public static void runFX(Runnable runnable) {
    if (!Platform.isFxApplicationThread()) {
      Platform.runLater(runnable);
    } else {
      runnable.run();
    }
  }

  /**
   * Run the callable on the FX thread if not already on that thread and return the result.
   *
   * @param callable callable to run
   * @param <T> return type of callable
   * @return callable return value
   * @throws ExecutionException when running callable
   * @throws InterruptedException when running callable
   */
  public static <T> T returnFX(Callable<T> callable)
      throws ExecutionException, InterruptedException {
    final FutureTask<T> query = new FutureTask<>(callable);
    runFX(query);
    return query.get();
  }

}
