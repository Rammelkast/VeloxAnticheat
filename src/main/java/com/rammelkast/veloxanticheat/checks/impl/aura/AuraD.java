package com.rammelkast.veloxanticheat.checks.impl.aura;

import com.rammelkast.veloxanticheat.checks.CheckInfo;
import com.rammelkast.veloxanticheat.checks.type.MotionCheck;
import com.rammelkast.veloxanticheat.player.PlayerWrapper;
import com.rammelkast.veloxanticheat.utils.Exemption;
import com.rammelkast.veloxanticheat.utils.MathLib;
import com.rammelkast.veloxanticheat.utils.Motion;

@CheckInfo(name = "Aura", type = 'D')
public final class AuraD extends MotionCheck {

	public AuraD(final PlayerWrapper wrapper) {
		super(wrapper);
	}

	@Override
	public void process(final Motion motion) {
		if (exempt(Exemption.CONNECTING)) {
			return;
		}

		if (!motion.hasRotation()) {
			// No rotation -> do not check
			return;
		}

		final boolean attacking = this.wrapper.getCombatProcessor().isInCombat();
		if (!attacking) {
			return;
		}

		final float deltaYaw = motion.getYaw();
		final float lastDeltaYaw = this.wrapper.getMotionProcessor().getPrevious().getYaw();
		final float yawAcceleration = Math.abs(deltaYaw - lastDeltaYaw);
		final float deltaPitch = motion.getPitch();
		final float lastDeltaPitch = this.wrapper.getMotionProcessor().getPrevious().getPitch();
		final float pitchAcceleration = Math.abs(deltaPitch - lastDeltaPitch);
		/**
		 * These are all magic values obtained during testing
		 * 
		 * TODO
		 * These may need additional verification and adjustment with different clients
		 */
		if (yawAcceleration < 0.1f && pitchAcceleration == 0.0f && deltaYaw > 15.0f) {
			// Verbose for debugging
			verbose("acceleration: " + MathLib.roundFloat(yawAcceleration, 1) + "/0.0, delta: "
					+ MathLib.roundFloat(deltaYaw, 1));

			if (increaseBuffer() > 2.5) {
				fail("type: A, acceleration: " + MathLib.roundFloat(yawAcceleration, 1) + ", delta: "
						+ MathLib.roundFloat(deltaYaw, 1));
				decreaseBuffer();
			}
		} else if (yawAcceleration > 15.0f && pitchAcceleration < 0.5f && deltaYaw > 15.0f) {
			// Verbose for debugging
			verbose("acceleration: " + MathLib.roundFloat(yawAcceleration, 1) + "/"
					+ MathLib.roundFloat(pitchAcceleration, 1) + ", delta: " + MathLib.roundFloat(deltaYaw, 1));

			if (increaseBuffer() > 3.5) {
				fail("type: B, acceleration: " + MathLib.roundFloat(yawAcceleration, 1) + "/"
						+ MathLib.roundFloat(pitchAcceleration, 1) + ", delta: " + MathLib.roundFloat(deltaYaw, 1));
				decreaseBuffer();
			}
		} else if (yawAcceleration < 1E-4f && pitchAcceleration < 1E-4f && deltaYaw > 30.0f && deltaPitch > 1.5f) {
			// Verbose for debugging
			verbose("acceleration: " + MathLib.roundFloat(yawAcceleration, 1) + "/"
					+ MathLib.roundFloat(pitchAcceleration, 1) + ", delta: " + MathLib.roundFloat(deltaYaw, 1) + "/"
					+ MathLib.roundFloat(deltaPitch, 1));

			if (increaseBuffer() > 1.5) {
				fail("type: C, acceleration: " + MathLib.roundFloat(yawAcceleration, 1) + "/"
						+ MathLib.roundFloat(pitchAcceleration, 1) + ", delta: " + MathLib.roundFloat(deltaYaw, 1) + "/"
						+ MathLib.roundFloat(deltaPitch, 1));
				decreaseBuffer();
			}
		} else {
			decreaseBuffer(0.05);
		}
	}

}
