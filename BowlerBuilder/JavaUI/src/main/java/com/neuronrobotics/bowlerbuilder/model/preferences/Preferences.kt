package com.neuronrobotics.bowlerbuilder.model.preferences

interface Preferences<T : PreferencesService<Preferences<T>>> {

    /**
     * Save the current preferences.
     */
    fun save()

    fun getService(): T
}
