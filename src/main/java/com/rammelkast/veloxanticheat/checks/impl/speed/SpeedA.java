/**
 * Velox Anticheat | Simple, stable and accurate anticheat
 * Copyright (C) 2021-2022 Marco Moesman ("Rammelkast")
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.rammelkast.veloxanticheat.checks.impl.speed;

import com.rammelkast.veloxanticheat.checks.CheckInfo;
import com.rammelkast.veloxanticheat.checks.type.MotionCheck;
import com.rammelkast.veloxanticheat.player.PlayerWrapper;
import com.rammelkast.veloxanticheat.settings.Setting;
import com.rammelkast.veloxanticheat.settings.Setting.SettingType;
import com.rammelkast.veloxanticheat.utils.Exemption;
import com.rammelkast.veloxanticheat.utils.Friction;
import com.rammelkast.veloxanticheat.utils.MathLib;
import com.rammelkast.veloxanticheat.utils.Motion;

/**
 * Prediction speed check
 */
@CheckInfo(name = "Speed", type = 'A')
public final class SpeedA extends MotionCheck {

	// Settings
	private static final Setting RUBBERBAND = new Setting("rubberband", SettingType.BOOLEAN);

	private double adjusted;
	private float friction;

	public SpeedA(final PlayerWrapper wrapper) {
		super(wrapper);
	}

	@Override
	public void process(final Motion motion) {
		if (exempt(Exemption.CONNECTING, Exemption.CHUNK_LOADING, Exemption.GLIDING)) {
			return;
		}

		/**
		 * This check is mostly based on the client movement code We do assume some best
		 * case scenarios here, like always sprinting This corner cutting seems to be
		 * the best way to reduce false positives I dislike the wacky ice false positive
		 * fix, but I'm also too lazy to improve it
		 */
		final double horizontal = motion.getHorizontal();
		final float angle = motion.getAngle();
		double movementSpeed = this.wrapper.getMovementSpeed();
		if (motion.wasOnGround()) {
			movementSpeed *= 1.3f; // Always assume sprinting
			movementSpeed *= 0.16277136f / Math.pow((this.friction *= 0.91f), 3); // Client magic numbers

			// Adjust for strafing speed penalty
			if (angle > 135.0f) {
				movementSpeed /= 1.05;
			}

			// Fixes false while going very slow on ice
			if (this.wrapper.getMotionProcessor().iceTimer.hasNotPassed(2) && horizontal - this.adjusted > 0.05f
					&& this.adjusted < 0.08f) {
				movementSpeed *= 3.0f;
			}

			// Apply jump boost if jumping or when having a head collision
			// Might want to hard check against known jump deltas here
			if (!motion.isOnGround()
					&& (this.wrapper.getMotionProcessor().hasHeadCollision() || motion.getY() >= 0.42f)) {
				movementSpeed += 0.2f;
			}
		} else {
			// Movement in air in simple
			movementSpeed = 0.026f;
			this.friction = 0.91f;
		}

		// Add velocities
		movementSpeed += this.wrapper.getVelocityProcessor().getHorizontal();

		final double factor = (horizontal - this.adjusted) / movementSpeed;
		/**
		 * Any difference lower than .001 is negligible We check if adjusted is higher
		 * than 0.0f because this can happen (and false) sometimes, but has no effect on
		 * detection performance
		 */
		if (factor > 1.001 && this.adjusted > 0.0f) {
			// Verbose for debugging
			verbose("factor: " + MathLib.roundDouble(factor, 3) + ", angle: " + MathLib.roundFloat(angle, 1));
			verbose("adjusted: " + this.adjusted + " / horizontal: " + horizontal + " / wasOnGround: "
					+ motion.wasOnGround());

			if (increaseBuffer() > 5) {
				fail("factor: " + MathLib.roundDouble(factor, 3) + ", angle: " + MathLib.roundFloat(angle, 1),
						RUBBERBAND.getBoolean());
				decreaseBuffer(2);
			}
		} else {
			decreaseBuffer(0.1);
		}

		this.adjusted = horizontal * this.friction;
		this.friction = Friction.getFactor(motion.getTo().clone().subtract(0, 1, 0).getBlock());
	}

}
