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

data class MainWindowControllerPreferences(
    @Preference(name = "Plugins", description = "The installed plugins.")
    var plugins: List<Plugin> = emptyList(),
    @Preference(name = "Favorite Gists", description = "The favorited gists.")
    var favoriteGists: Set<String> = emptySet(),
    @Preference(name = "Default Creature Push URL", description = "The default creature push URL.")
    var defaultCreaturePushURL: String = "https://gist.github.com/e72d6c298cfc02cc5b5f11061cd99702.git",
    @Preference(name = "Default Creature File Name", description = "The default creature file name.")
    var defaultCreatureFileName: String = "defaultCreatures.json"
) : Preferences<MainWindowControllerPreferencesService> {

    override fun save() =
            MainWindowControllerPreferencesService().writePreferences(this)
}

class MainWindowControllerPreferencesBeanInfo : CustomBeanInfo(MainWindowControllerPreferences::class.java)
