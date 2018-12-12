/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.bowlerbuilder.view.cad.cadengine.bowlercadengine

import com.google.common.collect.ImmutableSet
import com.neuronrobotics.bowlerbuilder.cad.CsgParser
import eu.mihosoft.vrl.v3d.CSG
import javafx.scene.shape.MeshView
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject

class CSGManager
@Inject constructor(
    val csgParser: CsgParser
) {
    private val csgToMeshView: MutableMap<CSG, MeshView>
    private val csgNameToMeshView: MutableMap<String, MeshView>
    private val csgNameToCSG: MutableMap<String, CSG>

    init {
        csgToMeshView = ConcurrentHashMap()
        csgNameToMeshView = ConcurrentHashMap()
        csgNameToCSG = ConcurrentHashMap()
    }

    /**
     * Adds a [CSG] to the maps.
     *
     * @param csg the CSG
     * @param mesh the CSG's [MeshView]
     */
    fun addCSG(csg: CSG, mesh: MeshView) {
        csgToMeshView[csg] = mesh
        csgNameToMeshView[csg.name] = mesh
        csgNameToCSG[csg.name] = csg
    }

    /**
     * Removes a CSG from the maps.
     *
     * @param csg the CSG
     */
    fun removeCSG(csg: CSG) {
        csgToMeshView.remove(csg)
        csgNameToMeshView.remove(csg.name)
    }

    /**
     * Removes a CSG from the maps by name.
     *
     * @param csgName the CSG's name
     */
    fun removeCSG(csgName: String) {
        csgToMeshView.remove(csgNameToCSG[csgName])
        csgNameToMeshView.remove(csgName)
        csgNameToCSG.remove(csgName)
    }

    /**
     * Gets a [CSG] by name.
     *
     * @param csgName the CSG's name
     * @return the CSG
     */
    fun getCSG(csgName: String) = csgNameToCSG[csgName]

    /**
     * Gets a CSG's [MeshView] by CSG name.
     *
     * @param csgName the CSG's name
     * @return the MeshView
     */
    fun getMeshView(csgName: String) = csgNameToMeshView[csgName]

    /**
     * Gets a CSG's [MeshView].
     *
     * @param csg the CSG
     * @return the MeshView
     */
    fun getMeshView(csg: CSG) = csgToMeshView[csg]

    /**
     * Gets all the CSGs.
     *
     * @return the csgs
     */
    fun getCSGs() = ImmutableSet.copyOf(csgToMeshView.keys)

    /**
     * Gets the raw [csgToMeshView] map.
     *
     * @return the map
     */
    fun getCsgToMeshView() = csgToMeshView

    /**
     * Whether the CSG is present.
     *
     * @param csg the csg
     * @return true if present
     */
    fun has(csg: CSG) = csgToMeshView.containsKey(csg)

    /**
     * Whether the CSG is present.
     *
     * @param csgName the csg's name
     * @return true if present
     */
    fun has(csgName: String) = csgNameToMeshView.containsKey(csgName)
}
