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
package com.rammelkast.veloxanticheat.checks.impl.motion;

import com.rammelkast.veloxanticheat.checks.CheckInfo;
import com.rammelkast.veloxanticheat.checks.type.MotionCheck;
import com.rammelkast.veloxanticheat.player.PlayerWrapper;
import com.rammelkast.veloxanticheat.settings.Setting;
import com.rammelkast.veloxanticheat.settings.Setting.SettingType;
import com.rammelkast.veloxanticheat.utils.Exemption;
import com.rammelkast.veloxanticheat.utils.MathLib;
import com.rammelkast.veloxanticheat.utils.Motion;

@CheckInfo(name = "Motion", type = 'B')
public final class MotionB extends MotionCheck {

	// Settings
	private static final Setting RUBBERBAND = new Setting("rubberband", SettingType.BOOLEAN);
	
	public MotionB(final PlayerWrapper wrapper) {
		super(wrapper);
	}

	@Override
	public void process(Motion motion) {
		if (exempt(Exemption.CONNECTING, Exemption.CHUNK_LOADING, Exemption.CREATIVE, Exemption.GLIDING,
				Exemption.LIQUID, Exemption.CLIMBABLE)) {
			return;
		}

		final int groundTicks = this.wrapper.getMotionProcessor().getGroundTicks();
		final double deltaY = motion.getY();
		final double lastY = motion.getFrom().getY();
		final boolean stepping = MathLib.computeGrounded(deltaY) && MathLib.computeGrounded(lastY);
		if (groundTicks > 5 && deltaY != 0.0 && !stepping) {
			if (increaseBuffer() > 1) {
				fail("ground: " + groundTicks + ", delta: " + MathLib.roundDouble(deltaY, 2), RUBBERBAND.getBoolean());
			}
		} else {
			decreaseBuffer(0.05);
		}
	}

}
