package com.neuronrobotics.bowlerbuilder.model.preferences

interface PreferencesService<T> {

    fun getCurrentPreferences(): T?

    fun getCurrentPreferencesOrDefault(): T

    fun writePreferences(preferences: T)

    fun getFilePath(name: String) = "${PreferencesPath.PATH}$name.json"
}
