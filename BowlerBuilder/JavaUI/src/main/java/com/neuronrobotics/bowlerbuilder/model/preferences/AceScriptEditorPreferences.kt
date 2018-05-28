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

data class AceScriptEditorPreferences(
    @Preference(name = "Max Toast Length", description = "The maximum length for a toast in the script editor.")
    var maxToastLength: Int = 15,
    @Preference(name = "Font Size", description = "The editor's font size.")
    var fontSize: Int = 14
) : Preferences<AceScriptEditorPreferencesService> {

    override fun save() =
            AceScriptEditorPreferencesService().writePreferences(this)

    override fun getService() =
            AceScriptEditorPreferencesService()
}

class AceScriptEditorPreferencesBeanInfo : CustomBeanInfo(AceScriptEditorPreferences::class.java)
