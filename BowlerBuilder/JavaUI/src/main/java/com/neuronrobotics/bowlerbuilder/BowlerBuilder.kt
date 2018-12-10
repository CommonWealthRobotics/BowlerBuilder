/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.bowlerbuilder

import com.google.common.base.Throwables
import com.google.inject.Guice
import com.google.inject.Injector
import com.google.inject.Singleton
import com.neuronrobotics.bowlerbuilder.controller.MainWindowController
import javafx.application.Application
import javafx.fxml.FXMLLoader
import javafx.scene.Scene
import javafx.scene.control.SplitPane
import javafx.stage.Stage
import java.io.IOException
import java.util.logging.Level

@Singleton
class BowlerBuilder : Application() {

    @Throws(IOException::class)
    override fun start(primaryStage: Stage) {
        // Log uncaught exceptions on the FX thread
        Thread.currentThread()
            .setUncaughtExceptionHandler { _, exception ->
                LOGGER.log(Level.SEVERE, Throwables.getStackTraceAsString(exception))
            }

        Thread.setDefaultUncaughtExceptionHandler { _, exception ->
            LOGGER.log(Level.SEVERE, Throwables.getStackTraceAsString(exception))
        }

        val loader = FXMLLoader(
            BowlerBuilder::class.java.getResource("/com/neuronrobotics/bowlerbuilder/MainWindow.fxml"),
            null,
            null,
            { injector.getInstance(it) })

        val mainWindow = loader.load<SplitPane>()

        primaryStage.title = "BowlerBuilder"
        primaryStage.scene = Scene(mainWindow)
        primaryStage.setOnCloseRequest { (loader.getController<Any>() as MainWindowController).saveAndQuit() }
        primaryStage.show()
    }

    companion object {

        private val LOGGER = LoggerUtilities.getLogger(BowlerBuilder::class.java.simpleName)

        @PublicAPI
        @JvmStatic
        val injector: Injector = Guice.createInjector()

        @Suppress("unused")
        @PublicAPI
        @JvmStatic
        val mainController: MainWindowController
            get() = injector.getInstance(MainWindowController::class.java)
    }
}
