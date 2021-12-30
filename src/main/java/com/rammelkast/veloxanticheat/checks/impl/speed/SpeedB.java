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

import org.bukkit.potion.PotionEffectType;

import com.rammelkast.veloxanticheat.checks.CheckInfo;
import com.rammelkast.veloxanticheat.checks.type.MotionCheck;
import com.rammelkast.veloxanticheat.player.PlayerWrapper;
import com.rammelkast.veloxanticheat.player.processor.MotionProcessor;
import com.rammelkast.veloxanticheat.settings.Setting;
import com.rammelkast.veloxanticheat.settings.Setting.SettingType;
import com.rammelkast.veloxanticheat.utils.Exemption;
import com.rammelkast.veloxanticheat.utils.MathLib;
import com.rammelkast.veloxanticheat.utils.Motion;

/**
 * Limit speed check
 */
@CheckInfo(name = "Speed", type = 'B')
public final class SpeedB extends MotionCheck {
	
	// Settings
	private static final Setting RUBBERBAND = new Setting("rubberband", SettingType.BOOLEAN);
	
	public SpeedB(final PlayerWrapper wrapper) {
		super(wrapper);
	}

	@Override
	public void process(final Motion motion) {
		if (exempt(Exemption.CONNECTING, Exemption.CREATIVE, Exemption.GLIDING, Exemption.WAS_FLYING)) {
			return;
		}
		
		/**
		 * We're not interested in jumps
		 */
		if (!motion.isOnGround() && motion.wasOnGround()) {
			return;
		}
		
		final double horizontal = motion.getHorizontal();
		double limit = 0.4 + (0.2 * MathLib.getPotionLevel(this.wrapper.getPlayer(), PotionEffectType.SPEED));
		
		final MotionProcessor processor = this.wrapper.getMotionProcessor();
		/**
		 * Adjustments that prevent false positives
		 * These are all magic numbers that I picked while testing
		 */
		limit += (processor.slimeTimer.hasNotPassed(4) ? 0.04 : 0.0);
		limit += (processor.iceTimer.hasNotPassed(10) ? 0.12 : 0.0);
		
		// Head bops
		if (processor.bopTimer.hasNotPassed(6)) {
			limit *= processor.bopTimer.hasNotPassed(4) ? 1.6 : 1.4;
		}
		
		// Add velocities
		limit += this.wrapper.getVelocityProcessor().getHorizontal();
		
		if (horizontal > limit) {
			// Verbose for debugging
			verbose("hz: " + MathLib.roundDouble(horizontal, 4) + ", limit: " + MathLib.roundDouble(limit, 4));
			
			if (increaseBuffer() > 3.5) {
				fail("hz: " + MathLib.roundDouble(horizontal, 2) + ", limit: " + MathLib.roundDouble(limit, 2)
						+ ", bop: " + processor.bopTimer.hasNotPassed(6), RUBBERBAND.getBoolean());
				this.buffer /= 2;
			}
		} else {
			decreaseBuffer(0.05);
		}
	}

}
