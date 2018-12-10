/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.bowlerbuilder.model.preferences

interface PreferencesConsumer {

    /**
     * Read the current preferences and apply them. Requires that a [PreferencesService] be stored
     * somewhere in the implementing class.
     */
    fun refreshPreferences()

    /**
     * Read the current preferences. Requires that a [PreferencesService] be stored somewhere in
     * the implementing class.
     */
    fun getCurrentPreferences(): Preferences
}
