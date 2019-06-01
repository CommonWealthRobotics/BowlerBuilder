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
package com.neuronrobotics.bowlerbuilder

import com.beust.klaxon.Klaxon
import com.google.common.collect.ImmutableList
import com.neuronrobotics.bowlerkernel.gitfs.GitFile
import com.neuronrobotics.bowlerkernel.kinematics.base.KinematicBase
import com.neuronrobotics.bowlerkernel.kinematics.base.model.KinematicBaseData
import com.neuronrobotics.bowlerkernel.kinematics.limb.model.DhParamData
import com.neuronrobotics.bowlerkernel.kinematics.limb.model.LimbData
import com.neuronrobotics.bowlerkernel.kinematics.limb.model.LinkData
import com.neuronrobotics.bowlerkernel.kinematics.motion.FrameTransformation
import eu.mihosoft.vrl.v3d.CSG
import io.ktor.application.call
import io.ktor.http.content.resource
import io.ktor.http.content.resources
import io.ktor.http.content.static
import io.ktor.response.respondText
import io.ktor.routing.get
import io.ktor.routing.routing
import io.ktor.server.engine.ApplicationEngine
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import org.octogonapus.ktguava.collections.emptyImmutableList
import java.util.concurrent.TimeUnit

class KernelServer {

    private var server: ApplicationEngine? = null
    private val robots = mutableListOf<KinematicBase>()
    private val robotCad = mutableMapOf<KinematicBase, ImmutableList<CSG>>()

    fun addRobot(base: KinematicBase) {
        robots.add(base)
    }

    fun addRobotCad(base: KinematicBase, cad: ImmutableList<CSG>) {
        require(robots.contains(base)) {
            "The base $base was not in the list of robots."
        }

        robotCad[base] = cad
    }

    fun start() {
        server = embeddedServer(
            Netty,
            port = 8080,
            module = {
                val klaxon = Klaxon().apply {
                    converter(FrameTransformation.converter)
                }

                routing {
                    get("/robots") {
                        call.respondText(
                            klaxon.toJsonString(
                                Robots(robots.map { base ->
                                    Robot(
                                        base.toKinematicBaseData(),
                                        robotCad[base]?.mapIndexed { index, _ ->
                                            "/robot/${base.id}/$index"
                                        } ?: emptyImmutableList()
                                    )
                                })
                            )
                        )
                    }

                    get("/robot/{id}/{index}") {
                        val csg = robotCad.entries.first {
                            it.key.id.toString() == call.parameters["id"]!!
                        }.value[call.parameters["index"]!!.toInt()]
                        call.respondText { csg.toObjString() }
                    }

                    static {
                        resource("/", "display.html")
                        resource("*", "display.html")
                        static("js") {
                            resources("static/js")
                        }
                    }
                }
            }
        ).apply { start(wait = true) }
    }

    fun stop() {
        server?.stop(1, 5, TimeUnit.SECONDS)
    }
}

private fun KinematicBase.toKinematicBaseData() = KinematicBaseData(
    id.toString(),
    limbs.map {
        LimbData(
            id.toString(),
            it.links.map {
                LinkData(
                    it.type,
                    DhParamData(
                        it.dhParam.d,
                        it.dhParam.theta,
                        it.dhParam.r,
                        it.dhParam.alpha
                    ),
                    it.jointLimits,
                    GitFile("", ""),
                    GitFile("", "")
                )
            },
            GitFile("", ""),
            GitFile("", ""),
            GitFile("", ""),
            GitFile("", ""),
            GitFile("", ""),
            GitFile("", "")
        )
    },
    limbBaseTransforms.values.asList(),
    GitFile("", "")
)

data class Robots(
    val robots: List<Robot>
)

data class Robot(
    val base: KinematicBaseData,
    val cad: List<String>
)
