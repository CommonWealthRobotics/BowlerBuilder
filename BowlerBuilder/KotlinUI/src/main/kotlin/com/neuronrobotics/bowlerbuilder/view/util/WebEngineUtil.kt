/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.bowlerbuilder.view.util

import javafx.concurrent.Worker
import javafx.scene.web.WebEngine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.javafx.JavaFx
import kotlinx.coroutines.launch
import java.util.concurrent.Callable
import java.util.concurrent.ExecutionException
import java.util.concurrent.FutureTask

object WebEngineUtil {

    /**
     * Run the runnable after the engine is done loading.
     *
     * @param worker [WebEngine] loadWorker to operate on
     * @param runnable runnable to run
     */
    @JvmStatic
    fun runAfterEngine(worker: Worker<Void>, runnable: Runnable) {
        GlobalScope.launch(context = Dispatchers.JavaFx) {
            if (checkEngine(worker)) {
                runnable.run()
            } else {
                worker.stateProperty().addListener { _, _, newState ->
                    if (newState == Worker.State.SUCCEEDED) {
                        runnable.run()
                    }
                }
            }
        }
    }

    /**
     * Run the callable after the engine is done loading and return the result.
     *
     * @param worker [WebEngine] loadWorker to operate on
     * @param callable callable to run
     * @param <T> return type of callable
     * @return callable return value
     * @throws ExecutionException when running callable
     * @throws InterruptedException when running callable
    </T> */
    @JvmStatic
    @Throws(ExecutionException::class, InterruptedException::class)
    fun <T> returnAfterEngine(worker: Worker<Void>, callable: Callable<T>): T {
        val query = FutureTask(callable)
        runAfterEngine(worker, query)
        return query.get()
    }

    /**
     * Return if the engine is done loading, and is safe to execute scripts on.
     *
     * @param worker [WebEngine] loadWorker to operate on
     * @return whether the engine is done loading
     */
    private fun checkEngine(worker: Worker<Void>): Boolean {
        return worker.state == Worker.State.SUCCEEDED
    }
}
