package com.neuronrobotics.bowlerbuilder.model.preferences

interface PreferencesConsumer<T> {

    fun refreshPreferences()

    fun getCurrentPreferences(): Preferences<T>
}
