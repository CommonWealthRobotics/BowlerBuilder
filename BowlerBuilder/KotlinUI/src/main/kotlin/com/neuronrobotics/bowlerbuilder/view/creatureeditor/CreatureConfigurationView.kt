/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.bowlerbuilder.view.creatureeditor

import com.google.common.collect.ImmutableList
import com.neuronrobotics.bowlerbuilder.model.LimbData
import com.neuronrobotics.bowlerbuilder.view.creatureeditor.configuration.AddLimbWizard
import com.neuronrobotics.bowlerstudio.assets.AssetFactory
import com.neuronrobotics.kinematicschef.util.toImmutableList
import com.neuronrobotics.sdk.addons.kinematics.DHParameterKinematics
import com.neuronrobotics.sdk.addons.kinematics.MobileBase
import javafx.beans.property.SimpleDoubleProperty
import javafx.geometry.Insets
import javafx.scene.Node
import tornadofx.*

class CreatureConfigurationView(
    private val device: MobileBase
) : Fragment() {

    val cadGenProgressProperty = SimpleDoubleProperty()
    val cadGenProcess by cadGenProgressProperty

    override val root = borderpane {
        top = vbox {
            padding = Insets(5.0)
            spacing = 5.0

            hbox {
                progressindicator(cadGenProgressProperty)
            }

            hbox {
                spacing = 5.0

                button("Regen CAD", AssetFactory.loadIcon("Generate-Cad.png")) {
                    tooltip("Regenerate CAD")
                    action {
                    }
                }

                button("Print CAD", AssetFactory.loadIcon("Printable-Cad.png")) {
                    tooltip("Export printable STLs")
                    action {
                    }
                }

                button("Kinematic STL", AssetFactory.loadIcon("Printable-Cad.png")) {
                    tooltip("Export kinematics STLs")
                    action {
                    }
                }
            }
        }

        center = tabpane {
            val limbs = (device.appendages +
                device.legs +
                device.drivable +
                device.steerable).toImmutableList()

            tab("") {
                graphic = AssetFactory.loadIcon("creature.png")
                tooltip("Limb/Link Configuration")
                content = generateConfigurationTabContent(limbs)
            }

            tab("") {
                graphic = AssetFactory.loadIcon("Move-Limb.png")
                tooltip("Movement Controls")
                content = generateMovementTabContent(limbs)
            }

            tab("") {
                graphic = AssetFactory.loadIcon("Advanced-Configuration.png")
                tooltip("Hardware Configuration")
                content = generateHardwareTabContent(limbs)
            }

            tab("") {
                graphic = AssetFactory.loadIcon("Edit-Script.png")
                tooltip("Scripting")
                content = generateScriptingTabContent(limbs)
            }
        }
    }

    private fun generateConfigurationTabContent(limbs: ImmutableList<DHParameterKinematics>): Node {
        return button("Add Limb") {
            action {
                AddLimbWizard().apply {
                    scope.set(LimbData())
                    openModal()
                    onComplete {
                        println(limbData)
                    }
                }
            }
        }
    }

    private fun generateMovementTabContent(limbs: ImmutableList<DHParameterKinematics>): Node {
        return vbox {
        }
    }

    private fun generateHardwareTabContent(limbs: ImmutableList<DHParameterKinematics>): Node {
        return vbox {
        }
    }

    private fun generateScriptingTabContent(limbs: ImmutableList<DHParameterKinematics>): Node {
        return vbox {
        }
    }
}
