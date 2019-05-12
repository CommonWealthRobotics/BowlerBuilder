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
import com.neuronrobotics.bowlerbuilder.KernelServer
import com.neuronrobotics.bowlercad.cadgenerator.DefaultCadGenerator
import com.neuronrobotics.bowlerkernel.kinematics.base.DefaultKinematicBase
import com.neuronrobotics.bowlerkernel.kinematics.base.baseid.SimpleKinematicBaseId
import com.neuronrobotics.bowlerkernel.kinematics.closedloop.JointAngleController
import com.neuronrobotics.bowlerkernel.kinematics.closedloop.NoopBodyController
import com.neuronrobotics.bowlerkernel.kinematics.limb.DefaultLimb
import com.neuronrobotics.bowlerkernel.kinematics.limb.Limb
import com.neuronrobotics.bowlerkernel.kinematics.limb.limbid.SimpleLimbId
import com.neuronrobotics.bowlerkernel.kinematics.limb.link.DefaultLink
import com.neuronrobotics.bowlerkernel.kinematics.limb.link.DhParam
import com.neuronrobotics.bowlerkernel.kinematics.limb.link.LinkType
import com.neuronrobotics.bowlerkernel.kinematics.motion.FrameTransformation
import com.neuronrobotics.bowlerkernel.kinematics.motion.MotionConstraints
import com.neuronrobotics.bowlerkernel.kinematics.motion.NoopForwardKinematicsSolver
import com.neuronrobotics.bowlerkernel.kinematics.motion.NoopInertialStateEstimator
import com.neuronrobotics.bowlerkernel.kinematics.motion.NoopInverseKinematicsSolver
import com.neuronrobotics.bowlerkernel.kinematics.motion.plan.NoopLimbMotionPlanFollower
import com.neuronrobotics.bowlerkernel.kinematics.motion.plan.NoopLimbMotionPlanGenerator
import com.neuronrobotics.bowlerkernel.util.Limits
import org.junit.jupiter.api.Test
import org.octogonapus.ktguava.collections.immutableListOf
import org.octogonapus.ktguava.collections.toImmutableList
import org.octogonapus.ktguava.collections.toImmutableMap

internal class KernelServerTest {

    private val cmmInputArmDhParams = immutableListOf(
        DhParam(13, 180, 32, -90),
        DhParam(25, -90, 93, 180),
        DhParam(11, 90, 24, 90),
        DhParam(128, -90, 0, 90),
        DhParam(0, 0, 0, -90),
        DhParam(25, 90, 0, 0)
    )

    private val increasingController = object : JointAngleController {
        var angle = 0.0

        override fun getCurrentAngle(): Double {
            angle += 0.5
            return angle
        }

        override fun setTargetAngle(
            angle: Double,
            motionConstraints: MotionConstraints
        ) {
        }
    }

    @Test
    fun `test talking to server`() {
        val cmmArm = DefaultLimb(
            SimpleLimbId("My Test Limb"),
            cmmInputArmDhParams.map { dhParam ->
                DefaultLink(
                    LinkType.Rotary,
                    dhParam,
                    Limits(180, -180),
                    NoopInertialStateEstimator
                )
            }.toImmutableList(),
            NoopForwardKinematicsSolver,
            NoopInverseKinematicsSolver,
            NoopLimbMotionPlanGenerator,
            NoopLimbMotionPlanFollower,
            immutableListOf(
                increasingController,
                increasingController,
                increasingController,
                increasingController,
                increasingController,
                increasingController
            ),
            NoopInertialStateEstimator
        ) as Limb

        val limbs = immutableListOf(cmmArm)

        val base = DefaultKinematicBase(
            SimpleKinematicBaseId("My Test Robot"),
            limbs,
            limbs.map { it.id to FrameTransformation.identity }.toImmutableMap(),
            NoopBodyController
        )

        val (bodyCad, limbCad) = DefaultCadGenerator().let {
            it.generateBody(base) to it.generateLimbs(base)
        }

        val server = KernelServer()
        server.addRobot(base)
        server.addRobotCad(base, (limbCad.values().flatten() + bodyCad).toImmutableList())
        server.start()
        Thread.sleep(3000)
        server.stop()
    }
}
