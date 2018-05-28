package com.neuronrobotics.bowlerbuilder.model.preferences

import com.beust.klaxon.Klaxon
import java.io.File
import java.io.FileNotFoundException

class BowlerCadEnginePreferencesService : PreferencesService<BowlerCadEnginePreferences> {
    private val path = getFilePath("BowlerCadEngine")

    override fun getCurrentPreferences() =
            try {
                Klaxon().parse<BowlerCadEnginePreferences>(File(path).readText())
            } catch (e: FileNotFoundException) {
                writePreferences(BowlerCadEnginePreferences())
                BowlerCadEnginePreferences()
            }

    override fun getCurrentPreferencesOrDefault() =
            getCurrentPreferences() ?: BowlerCadEnginePreferences()

    override fun writePreferences(preferences: BowlerCadEnginePreferences) =
            File(path).writeText(Klaxon().toJsonString(preferences))
}

data class BowlerCadEnginePreferences(
    @Preference(name = "Anti-aliasing", description = "Whether to apply anti-aliasing in the CAD engine.")
    var shouldAA: Boolean = true
) : Preferences<BowlerCadEnginePreferencesService> {

    override fun save() =
            BowlerCadEnginePreferencesService().writePreferences(this)
}

class BowlerCadEnginePreferencesBeanInfo : CustomBeanInfo(BowlerCadEnginePreferences::class.java)
