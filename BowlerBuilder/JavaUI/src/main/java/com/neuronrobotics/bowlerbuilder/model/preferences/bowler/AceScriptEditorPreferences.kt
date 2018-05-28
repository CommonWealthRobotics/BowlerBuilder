package com.neuronrobotics.bowlerbuilder.model.preferences.bowler

import com.beust.klaxon.Klaxon
import com.neuronrobotics.bowlerbuilder.model.preferences.Preference
import com.neuronrobotics.bowlerbuilder.model.preferences.Preferences
import com.neuronrobotics.bowlerbuilder.model.preferences.PreferencesService
import java.io.File
import java.io.FileNotFoundException

class DefaultScriptEditorPreferencesService : PreferencesService<AceScriptEditorPreferences> {

    private val path = getFilePath("DefaultScriptEditorController")

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

    override fun getName() = "Default Script Editor"
}

data class AceScriptEditorPreferences(
    @Preference(name = "Max Toast Length", description = "The maximum length for a toast in the script editor.")
    var maxToastLength: Int = 15,
    @Preference(name = "Font Size", description = "The editor's font size.")
    var fontSize: Int = 14
) : Preferences {

    override fun save() =
            DefaultScriptEditorPreferencesService().writePreferences(this)
}

/**
 * Used by automatic introspection. See [CustomBeanInfo].
 */
class AceScriptEditorPreferencesBeanInfo : CustomBeanInfo(AceScriptEditorPreferences::class.java)
