/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package com.neuronrobotics.bowlerbuilder;

import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import javafx.application.Platform;

/**
 * JavaFX helper utility class.
 */
public final class FxUtil {

  private FxUtil() {
  }

  /**
   * Run the runnable on the FX thread if not already on that thread.
   *
   * @param runnable runnable to run
   */
  public static void runFX(Runnable runnable) {
    if (Platform.isFxApplicationThread()) {
      runnable.run();
    } else {
      Platform.runLater(runnable);
    }
  }

  /**
   * Run the runnable on the FX thread if not already on that thread. Block for the runnable to
   * finish.
   *
   * @param runnable runnable to run
   */
  public static void runFXAndWait(Runnable runnable) throws InterruptedException {
    if (Platform.isFxApplicationThread()) {
      runnable.run();
    } else {
      CountDownLatch latch = new CountDownLatch(1);
      Platform.runLater(() -> {
        runnable.run();
        latch.countDown();
      });
      latch.await();
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

  /**
   * Run the callable on the FX thread if not already on that thread and return the result. Block
   * for the callable to finish.
   *
   * @param callable callable to run
   * @param <T> return type of callable
   * @return callable return value
   * @throws ExecutionException when running callable
   * @throws InterruptedException when running callable
   */
  public static <T> T returnFXAndWait(Callable<T> callable)
      throws ExecutionException, InterruptedException {
    final FutureTask<T> query = new FutureTask<>(callable);
    runFXAndWait(query);
    return query.get();
  }

}
