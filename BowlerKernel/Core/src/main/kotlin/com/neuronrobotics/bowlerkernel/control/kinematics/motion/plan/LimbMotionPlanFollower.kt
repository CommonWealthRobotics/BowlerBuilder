/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.neuronrobotics.bowlerkernel.control.kinematics.motion.plan

/**
 * A motion plan follower which operates on a limb.
 */
interface LimbMotionPlanFollower {

    /**
     * Follows a [LimbMotionPlan].
     *
     * @param plan The [LimbMotionPlan] to follow.
     */
    fun followPlan(plan: LimbMotionPlan)
}
