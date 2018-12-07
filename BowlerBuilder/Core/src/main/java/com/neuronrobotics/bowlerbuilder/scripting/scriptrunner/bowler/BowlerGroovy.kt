/*
 * Copyright 2017 Ryan Benasutti
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.bowlerbuilder.scripting.scriptrunner.bowler

import com.neuronrobotics.bowlerstudio.scripting.GroovyHelper
import com.neuronrobotics.bowlerstudio.scripting.IScriptingLanguage
import com.neuronrobotics.bowlerstudio.scripting.ScriptingEngine
import com.neuronrobotics.sdk.common.DeviceManager
import groovy.lang.Binding
import groovy.lang.GroovyShell
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.customizers.ImportCustomizer
import java.io.File
import java.util.ArrayList
import java.util.Arrays

/** Simple copy of [GroovyHelper] that keeps a flag for when it is compiling or running.  */
class BowlerGroovy : IScriptingLanguage {

    override fun getShellType(): String = "BowlerGroovy"

    @Throws(Exception::class)
    override fun inlineScriptRun(code: File, args: ArrayList<Any>?): Any? = this.inline(code, args)

    @Throws(Exception::class)
    override fun inlineScriptRun(code: String, args: ArrayList<Any>?): Any? =
            this.inline(code, args)

    override fun getIsTextFile(): Boolean = true

    override fun getFileExtenetion(): ArrayList<String> =
            ArrayList(Arrays.asList("java", "groovy"))

    @Throws(Exception::class)
    private fun inline(code: Any, args: List<Any>?): Any? {
        val configuration = CompilerConfiguration().also {
            it.addCompilationCustomizers(
                    ImportCustomizer()
                            .addStarImports(*ScriptingEngine.getImports())
                            .addStarImports(
                                    "com.neuronrobotics.bowlerbuilder",
                                    "com.neuronrobotics.bowlerbuilder.controller",
                                    "com.neuronrobotics.bowlerbuilder.view.tab",
                                    "com.neuronrobotics.kinematicschef")
                            .addStaticStars(
                                    "com.neuronrobotics.sdk.util.ThreadUtil",
                                    "eu.mihosoft.vrl.v3d.Transform",
                                    "com.neuronrobotics.bowlerstudio.vitamins.Vitamins"))
        }

        val binding = Binding()

        for (deviceName in DeviceManager.listConnectedDevice()) {
            DeviceManager.getSpecificDevice(null, deviceName).let {
                binding.setVariable(it.scriptingName, Class.forName(it.javaClass.name).cast(it))
            }
        }

        binding.setVariable("args", args)

        val shell = GroovyShell(Thread.currentThread().contextClassLoader, binding, configuration)

        val script =
                if (String::class.java.isInstance(code)) {
                    shell.parse(code as String)
                } else {
                    if (!File::class.java.isInstance(code)) {
                        return null
                    }

                    shell.parse(code as File)
                }

        return script.run()
    }
}
