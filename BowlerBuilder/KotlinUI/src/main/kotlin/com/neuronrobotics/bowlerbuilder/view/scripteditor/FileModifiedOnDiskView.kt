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
package com.neuronrobotics.bowlerbuilder.view.scripteditor

import tornadofx.*

class FileModifiedOnDiskView(
    onKeepEditor: () -> Unit,
    onKeepDisk: () -> Unit
) : Fragment() {

    override val root = form {
        fieldset("File modified on disk.") {
            field("The file open in the editor was modified on disk.") {
                button("Keep contents from editor").action {
                    onKeepEditor()
                    close()
                }

                button("Keep contents from disk").action {
                    onKeepDisk()
                    close()
                }
            }
        }
    }
}
