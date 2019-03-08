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
package com.neuronrobotics.bowlerbuilder.controller.util

import com.google.common.collect.ImmutableList
import org.octogonapus.ktguava.collections.toImmutableList
import java.net.NetworkInterface

/**
 * Gets the [NetworkInterface] instances which are not loopback interfaces.
 *
 * @return Non-loopback interfaces.
 */
fun getNonLoopbackNIs(): ImmutableList<NetworkInterface> =
    NetworkInterface.getNetworkInterfaces().toList().filter { it.name != "lo" }.toImmutableList()

/**
 * Gets the [NetworkInterface] MACs which are not loopback interfaces.
 *
 * @return Non-loopback interface MACs.
 */
fun getNonLoopbackNIMacs(): ImmutableList<String> =
    getNonLoopbackNIs().map {
        it.hardwareAddress.joinToString(":") {
            String.format("%02x", it)
        }
    }.toImmutableList()
