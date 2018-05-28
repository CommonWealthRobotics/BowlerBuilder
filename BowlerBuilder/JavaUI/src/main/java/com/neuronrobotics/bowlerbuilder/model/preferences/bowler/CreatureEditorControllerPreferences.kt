package com.neuronrobotics.bowlerbuilder.model.preferences.bowler

import com.beust.klaxon.Klaxon
import com.neuronrobotics.bowlerbuilder.model.preferences.Preference
import com.neuronrobotics.bowlerbuilder.model.preferences.Preferences
import com.neuronrobotics.bowlerbuilder.model.preferences.PreferencesService
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

data class CreatureEditorControllerPreferences(
    @Preference(name = "Auto-regen CAD", description = "Whether to automatically regenerate CAD in the Creature editor.")
    var autoRegenCAD: Boolean = false
) : Preferences {

    override fun save() =
            CreatureEditorControllerPreferencesService().writePreferences(this)
}

/**
 * Used by automatic introspection. See [CustomBeanInfo].
 */
class CreatureEditorControllerPreferencesBeanInfo : CustomBeanInfo(CreatureEditorControllerPreferences::class.java)
