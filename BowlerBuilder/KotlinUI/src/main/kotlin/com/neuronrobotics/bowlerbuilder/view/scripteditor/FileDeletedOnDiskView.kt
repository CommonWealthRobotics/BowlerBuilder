/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.bowlerbuilder.view.scripteditor

import tornadofx.*

class FileDeletedOnDiskView : Fragment() {

    override val root = form {
        fieldset("File deleted on disk.") {
            field("The file open in the editor was deleted on disk.") {
                button("Keep file").action {
                    TODO()
                }

                button("Remove file").action {
                    TODO()
                }
            }
        }
    }
}
