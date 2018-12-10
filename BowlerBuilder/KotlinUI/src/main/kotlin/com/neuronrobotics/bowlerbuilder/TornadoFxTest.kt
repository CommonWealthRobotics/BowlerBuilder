package com.neuronrobotics.bowlerbuilder

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
    val injector = Guice.createInjector(module {
        bind<ScriptEditorFactory>().to<AceScriptEditorFactory>()
        bind<ScriptRunner>().to<BowlerScriptRunner>()
    })

    FX.dicontainer = object : DIContainer {
        override fun <T : Any> getInstance(type: KClass<T>): T = injector.getInstance(type.java)
    }

    launch<TornadoFxTest>(args)
}

class TornadoFxTest : App(MainWindowView::class)
