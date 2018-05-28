package com.neuronrobotics.bowlerbuilder.model.preferences

import com.beust.klaxon.Klaxon
import java.io.File
import java.io.FileNotFoundException

class CreatureEditorControllerPreferencesService : PreferencesService<CreatureEditorControllerPreferences> {
    private val path = getFilePath("CreatureEditorController")

    override fun getCurrentPreferences() =
            try {
                Klaxon().parse<CreatureEditorControllerPreferences>(File(path).readText())
            } catch (e: FileNotFoundException) {
                writePreferences(CreatureEditorControllerPreferences())
                CreatureEditorControllerPreferences()
            }

    override fun getCurrentPreferencesOrDefault() =
            getCurrentPreferences() ?: CreatureEditorControllerPreferences()

    override fun writePreferences(preferences: CreatureEditorControllerPreferences) =
            File(path).writeText(Klaxon().toJsonString(preferences))
}

class CreatureEditorControllerPreferences(
    val autoRegenCAD: Boolean = false
) : Preferences
