/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.bowlerbuilder

import javafx.application.Platform
import org.testfx.util.WaitForAsyncUtils
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

object FxHelper {

    /**
     * Runs the given runnable on the JavaFX application thread and waits for it to complete.
     *
     * @param runnable the action to run on the application thread
     */
    @JvmStatic
    fun runAndWait(runnable: Runnable) {
        Platform.runLater(runnable)
        WaitForAsyncUtils.waitForFxEvents()
    }

    /**
     * Runs the given runnable on the JavaFX application thread and waits for it to complete.
     *
     * @param runnable the action to run on the application thread
     */
    fun runAndWait(runnable: () -> Unit) {
        runAndWait(Runnable(runnable))
    }

    /**
     * Try to catch an uncaught exception on the JavaFX thread. The UncaughtExceptionHandler is set
     * on the JavaFX thread and the provided Runnable is run. If the provided Exception class is
     * found, return true. If not, return false.
     *
     * @param runnable Runnable to run on this thread
     * @param exceptionClass Exception class to look for in the JavaFX thread
     * @param timeout Timeout for waiting for the exception to be thrown
     * @param timeoutUnit Timeout units
     * @return True if a matching exception was thrown from the JavaFX thread, false otherwise
     */
    @JvmStatic
    inline fun <reified T : Any> catchInJavaFXThread(
        runnable: Runnable,
        exceptionClass: Class<T>,
        timeout: Long,
        timeoutUnit: TimeUnit
    ): Boolean {
        val latch = CountDownLatch(1)
        var exceptionWasThrown = false

        runAndWait(Runnable {
            Thread.currentThread()
                .setUncaughtExceptionHandler { _, throwable ->
                    if (throwable::class.java == exceptionClass) {
                        exceptionWasThrown = true
                        latch.countDown()
                    }
                }
        })

        runnable.run()
        return latch.await(timeout, timeoutUnit) && exceptionWasThrown
    }

    /**
     * Try to catch an uncaught exception on the JavaFX thread. The UncaughtExceptionHandler is set
     * on the JavaFX thread and the provided Runnable is run. If the provided Exception class is
     * found, return true. If not, return false.
     *
     * @param runnable Runnable to run on this thread
     * @param exceptionClass Exception class to look for in the JavaFX thread
     * @param timeout Timeout for waiting for the exception to be thrown
     * @param timeoutUnit Timeout units
     * @return True if a matching exception was thrown from the JavaFX thread, false otherwise
     */
    inline fun <reified T : Any> catchInJavaFXThread(
        noinline runnable: () -> Unit,
        exceptionClass: Class<T>,
        timeout: Long,
        timeoutUnit: TimeUnit
    ): Boolean = catchInJavaFXThread(Runnable(runnable), exceptionClass, timeout, timeoutUnit)
}
