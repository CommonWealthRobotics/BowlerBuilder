package com.neuronrobotics.bowlerbuilder.model.preferences

import java.io.File

interface PreferencesService<T : Any> {

    /**
     * Read the current preferences. If the preferences need to be loaded from disk, the result
     * could be null.
     */
    fun getCurrentPreferences(): T?

    /**
     * Read the current preferences. If the preferences needed to be loaded from disk and the
     * result was null, default preferences are written to disk and returned.
     */
    fun getCurrentPreferencesOrDefault(): T

    /**
     * Write the preferences to disk.
     */
    fun writePreferences(preferences: T)

    /**
     * Get a short name for the type of preferences.
     */
    fun getName(): String

    /**
     * The preferences file path.
     */
    fun getFilePath(name: String): String {
        createPreferencesFolder()
        return "${PreferencesPath.PATH}$name.json"
    }

    /**
     * Create the preferences folder if it does not exist.
     */
    private fun createPreferencesFolder() {
        File(PreferencesPath.PATH).apply {
            if (!exists()) {
                mkdir()
            }
        }
    }
}
