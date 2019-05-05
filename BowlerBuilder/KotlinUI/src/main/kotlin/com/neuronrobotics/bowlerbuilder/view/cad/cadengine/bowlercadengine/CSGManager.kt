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
package com.neuronrobotics.bowlerbuilder.view.cad.cadengine.bowlercadengine

import com.google.common.collect.ImmutableSet
import eu.mihosoft.vrl.v3d.CSG
import javafx.scene.shape.MeshView
import java.util.concurrent.ConcurrentHashMap

@SuppressWarnings("TooManyFunctions")
class CSGManager {

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

    /**
     * Removes all the stored CSGs.
     */
    fun clearCSGs() {
        csgToMeshView.clear()
        csgNameToMeshView.clear()
        csgNameToCSG.clear()
    }
}
