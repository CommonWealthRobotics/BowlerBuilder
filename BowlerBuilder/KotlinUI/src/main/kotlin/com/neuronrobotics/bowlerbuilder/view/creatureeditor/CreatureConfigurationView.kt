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
package com.neuronrobotics.bowlerbuilder.view.creatureeditor

import com.google.common.collect.ImmutableList
import com.neuronrobotics.bowlerbuilder.model.LimbData
import com.neuronrobotics.bowlerbuilder.view.creatureeditor.configuration.AddLimbWizard
import com.neuronrobotics.bowlerbuilder.view.util.loadImageAsset
import com.neuronrobotics.sdk.addons.kinematics.DHParameterKinematics
import com.neuronrobotics.sdk.addons.kinematics.MobileBase
import javafx.beans.property.SimpleDoubleProperty
import javafx.geometry.Insets
import javafx.scene.Node
import org.controlsfx.glyphfont.FontAwesome
import org.octogonapus.ktguava.collections.toImmutableList
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

                button("Regen CAD", loadImageAsset("Generate-Cad.png", FontAwesome.Glyph.PLUS)) {
                    tooltip("Regenerate CAD")
                    action {
                    }
                }

                button("Print CAD", loadImageAsset("Printable-Cad.png", FontAwesome.Glyph.PLUS)) {
                    tooltip("Export printable STLs")
                    action {
                    }
                }

                button(
                    "Kinematic STL",
                    loadImageAsset("Printable-Cad.png", FontAwesome.Glyph.PLUS)
                ) {
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
                graphic = loadImageAsset("creature.png", FontAwesome.Glyph.COGS)
                tooltip("Limb/Link Configuration")
                content = generateConfigurationTabContent(limbs)
            }

            tab("") {
                graphic = loadImageAsset("Move-Limb.png", FontAwesome.Glyph.ARROWS_ALT)
                tooltip("Movement Controls")
                content = generateMovementTabContent(limbs)
            }

            tab("") {
                graphic = loadImageAsset("Advanced-Configuration.png", FontAwesome.Glyph.COGS)
                tooltip("Hardware Configuration")
                content = generateHardwareTabContent(limbs)
            }

            tab("") {
                graphic = loadImageAsset("Edit-Script.png", FontAwesome.Glyph.EDIT)
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
