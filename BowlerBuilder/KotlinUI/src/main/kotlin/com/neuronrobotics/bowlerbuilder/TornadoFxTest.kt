package com.neuronrobotics.bowlerbuilder

import com.google.common.base.Throwables
import com.google.inject.Guice
import com.neuronrobotics.bowlerbuilder.controller.scripteditorfactory.AceScriptEditorFactory
import com.neuronrobotics.bowlerbuilder.controller.scripteditorfactory.ScriptEditorFactory
import com.neuronrobotics.bowlerbuilder.scripting.scriptrunner.ScriptRunner
import com.neuronrobotics.bowlerbuilder.scripting.scriptrunner.bowler.BowlerScriptRunner
import com.neuronrobotics.bowlerbuilder.view.MainWindowView
import org.jlleitschuh.guice.module
import tornadofx.*
import kotlin.reflect.KClass

fun main(args: Array<String>) {
    launch<TornadoFxTest>(args)
}

class TornadoFxTest : App(MainWindowView::class) {

    init {
        runLater {
            // Log uncaught exceptions on the FX thread
            Thread.currentThread().setUncaughtExceptionHandler { _, exception ->
                LOGGER.severe(Throwables.getStackTraceAsString(exception))
            }
        }

        Thread.setDefaultUncaughtExceptionHandler { _, exception ->
            LOGGER.severe(Throwables.getStackTraceAsString(exception))
        }

        val injector = Guice.createInjector(module {
            bind<ScriptEditorFactory>().to<AceScriptEditorFactory>()
            bind<ScriptRunner>().to<BowlerScriptRunner>()
        })

        FX.dicontainer = object : DIContainer {
            override fun <T : Any> getInstance(type: KClass<T>): T = injector.getInstance(type.java)
        }
    }

    companion object {
        private val LOGGER = LoggerUtilities.getLogger(TornadoFxTest::class.java.simpleName)
    }
}
