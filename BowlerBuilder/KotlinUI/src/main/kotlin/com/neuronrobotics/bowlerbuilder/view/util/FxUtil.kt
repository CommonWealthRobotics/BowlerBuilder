/*
 * This file is part of BowlerBuilder.
 *
 * BowlerBuilder is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * BowlerBuilder is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with BowlerBuilder.  If not, see <https://www.gnu.org/licenses/>.
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
