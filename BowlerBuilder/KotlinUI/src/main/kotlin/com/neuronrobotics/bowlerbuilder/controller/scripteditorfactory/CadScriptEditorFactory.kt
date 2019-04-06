/*
 * This file is part of BowlerBuilder.
 *
 * BowlerBuilder is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * BowlerBuilder is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with BowlerBuilder.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.neuronrobotics.bowlerbuilder.controller.scripteditorfactory

import com.neuronrobotics.bowlerbuilder.view.scripteditor.CadScriptEditor
import java.io.File

interface CadScriptEditorFactory {

    /**
     * Creates a new [CadScriptEditor] and opens it. Do not call from the FX thread.
     *
     * @param url The url of the gist which is being edited.
     * @param file The file on disk where the gist file is cloned.
     * @return The [CadScriptEditor] which was created.
     */
    fun createAndOpenScriptEditor(url: String, file: File): CadScriptEditor

    /**
     * Creates a new scratchpad [CadScriptEditor] and opens it. Do not call from the FX thread.
     *
     * @return The [CadScriptEditor] which was created.
     */
    fun createAndOpenScratchpad(): CadScriptEditor
}
