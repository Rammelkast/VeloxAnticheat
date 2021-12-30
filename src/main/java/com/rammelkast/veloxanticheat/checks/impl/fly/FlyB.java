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
package com.rammelkast.veloxanticheat.checks.impl.fly;

import com.rammelkast.veloxanticheat.checks.CheckInfo;
import com.rammelkast.veloxanticheat.checks.type.MotionCheck;
import com.rammelkast.veloxanticheat.player.PlayerWrapper;
import com.rammelkast.veloxanticheat.settings.Setting;
import com.rammelkast.veloxanticheat.settings.Setting.SettingType;
import com.rammelkast.veloxanticheat.utils.Exemption;
import com.rammelkast.veloxanticheat.utils.MathLib;
import com.rammelkast.veloxanticheat.utils.Motion;

/**
 * Mathematical ground spoof check
 */
@CheckInfo(name = "Fly", type = 'B')
public final class FlyB extends MotionCheck {
	
	// Settings
	private static final Setting RUBBERBAND = new Setting("rubberband", SettingType.BOOLEAN);
	
	public FlyB(final PlayerWrapper wrapper) {
		super(wrapper);
	}

	@Override
	public void process(final Motion motion) {
		if (exempt(Exemption.CONNECTING, Exemption.CHUNK_LOADING, Exemption.GLIDING, Exemption.LEVITATING,
				Exemption.LIQUID, Exemption.CLIMBABLE)) {
			decreaseBuffer(0.25);
			return;
		}
		
		/**
		 * When we should mathematically be on ground (or not)
		 * but the client says otherwise, we flag
		 */
		final boolean clientGround = motion.isOnGround();
		final boolean mathGround = MathLib.computeGrounded(motion.getFrom().getY()) && MathLib.computeGrounded(motion.getTo().getY());
		if (clientGround != mathGround) {
			if (increaseBuffer() > 4) {
				fail("client: " + clientGround + ", math: " + mathGround, RUBBERBAND.getBoolean());
			}
		} else {
			this.buffer /= 2;
		}
	}

}
