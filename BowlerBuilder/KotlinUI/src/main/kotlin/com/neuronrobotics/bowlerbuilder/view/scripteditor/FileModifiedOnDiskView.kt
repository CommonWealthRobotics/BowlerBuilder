/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
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
