package com.neuronrobotics.bowlerbuilder.model.preferences

import com.beust.klaxon.Klaxon
import java.io.File
import java.io.FileNotFoundException

class AceScriptEditorPreferencesService : PreferencesService<AceScriptEditorPreferences> {
    private val path = getFilePath("AceScriptEditorController")

    override fun getCurrentPreferences() =
            try {
                Klaxon().parse<AceScriptEditorPreferences>(File(path).readText())
            } catch (e: FileNotFoundException) {
                writePreferences(AceScriptEditorPreferences())
                AceScriptEditorPreferences()
            }

    override fun getCurrentPreferencesOrDefault() =
            getCurrentPreferences() ?: AceScriptEditorPreferences()

    override fun writePreferences(preferences: AceScriptEditorPreferences) =
            File(path).writeText(Klaxon().toJsonString(preferences))
}

class AceScriptEditorPreferences(
    val maxToastLength: Int = 15,
    val fontSize: Int = 14
) : Preferences
