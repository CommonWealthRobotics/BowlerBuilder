/*
 * Copyright 2017 Ryan Benasutti
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.bowlerbuilder.view.util

import javafx.application.Platform
import tornadofx.*
import java.util.concurrent.CountDownLatch
import java.util.concurrent.ExecutionException
import java.util.concurrent.FutureTask

/** JavaFX helper utility class.  */
object FxUtil {

    /**
     * Run the runnable on the FX thread if not already on that thread. Block for the runnable to
     * finish.
     *
     * @param runnable runnable to run
     * @throws InterruptedException when waiting for the runnable to finish
     */
    @Throws(InterruptedException::class)
    @JvmStatic
    fun runFXAndWait(runnable: () -> Unit) {
        if (Platform.isFxApplicationThread()) {
            runnable()
        } else {
            val latch = CountDownLatch(1)
            runLater {
                runnable()
                latch.countDown()
            }
            latch.await()
        }
    }

    /**
     * Run the runnable on the FX thread if not already on that thread. Block for the runnable to
     * finish.
     *
     * @param runnable runnable to run
     * @throws InterruptedException when waiting for the runnable to finish
     */
    @Throws(InterruptedException::class)
    @JvmStatic
    fun runFXAndWait(runnable: Runnable) = runFXAndWait { runnable.run() }

    /**
     * Run the callable on the FX thread if not already on that thread and return the result.
     *
     * @param callable callable to run
     * @param <T> return type of callable
     * @return callable return value
     * @throws ExecutionException when running callable
     * @throws InterruptedException when running callable
     */
    @Throws(ExecutionException::class, InterruptedException::class)
    @JvmStatic
    fun <T> returnFX(callable: () -> T): T {
        val query = FutureTask(callable)

        if (Platform.isFxApplicationThread()) {
            query.run()
        } else {
            runLater { query.run() }
        }

        return query.get()
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
    </T> */
    @Throws(ExecutionException::class, InterruptedException::class)
    @JvmStatic
    fun <T> returnFXAndWait(callable: () -> T): T {
        val query = FutureTask(callable)
        runFXAndWait(query)
        return query.get()
    }
}
