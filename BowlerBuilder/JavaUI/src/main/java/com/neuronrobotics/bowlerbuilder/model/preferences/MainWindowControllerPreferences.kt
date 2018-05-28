package com.neuronrobotics.bowlerbuilder.model.preferences

import com.beust.klaxon.Klaxon
import com.neuronrobotics.bowlerbuilder.plugin.Plugin
import java.io.File
import java.io.FileNotFoundException

class MainWindowControllerPreferencesService : PreferencesService<MainWindowControllerPreferences> {
    private val path = getFilePath("MainWindowController")

    override fun getCurrentPreferences() =
            try {
                Klaxon().parse<MainWindowControllerPreferences>(File(path).readText())
            } catch (e: FileNotFoundException) {
                writePreferences(MainWindowControllerPreferences())
                MainWindowControllerPreferences()
            }

    override fun getCurrentPreferencesOrDefault() =
            getCurrentPreferences() ?: MainWindowControllerPreferences()

    override fun writePreferences(preferences: MainWindowControllerPreferences) =
            File(path).writeText(Klaxon().toJsonString(preferences))
}

class MainWindowControllerPreferences(
    val plugins: List<Plugin> = emptyList(),
    val favoriteGists: Set<String> = emptySet(),
    val defaultCreaturePushURL: String = "https://gist.github.com/e72d6c298cfc02cc5b5f11061cd99702.git",
    val defaultCreatureFileName: String = "defaultCreatures.json"
) : Preferences
