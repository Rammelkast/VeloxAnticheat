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
import com.rammelkast.veloxanticheat.utils.Motion;

/**
 * Collision ground spoof check
 */
@CheckInfo(name = "Fly", type = 'D')
public final class FlyD extends MotionCheck {

	// Settings
	private static final Setting RUBBERBAND = new Setting("rubberband", SettingType.BOOLEAN);
	
	public FlyD(final PlayerWrapper wrapper) {
		super(wrapper);
	}

	@Override
	public void process(final Motion motion) {
		if (exempt(Exemption.CONNECTING, Exemption.CHUNK_LOADING, Exemption.GLIDING, Exemption.LEVITATING,
				Exemption.LIQUID, Exemption.CLIMBABLE)) {
			resetBuffer();
			return;
		}

		final boolean clientGround = motion.isOnGround();
		final boolean freeInAir = this.wrapper.getMotionProcessor().isFreeInAir();
		/**
		 * Client says it's on ground while completely free in air -> flag
		 */
		if (clientGround && freeInAir) {
			if (increaseBuffer() > 2) {
				fail("client: " + clientGround + ", free: " + freeInAir, RUBBERBAND.getBoolean());
			}
		} else if (!clientGround && !freeInAir) {
			this.buffer /= 2;
		} else {
			this.buffer /= 4;
		}
	}

}
