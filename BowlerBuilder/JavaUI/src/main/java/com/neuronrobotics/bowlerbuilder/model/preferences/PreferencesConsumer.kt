package com.neuronrobotics.bowlerbuilder.model.preferences

interface PreferencesConsumer {

    /**
     * Read the current preferences and apply them. Requires that a [PreferencesService] be stored somewhere in the implementing class.
     */
    fun refreshPreferences()

    /**
     * Read the current preferences. Requires that a [PreferencesService] be stored somewhere in the implementing class.
     */
    fun getCurrentPreferences(): Preferences
}
