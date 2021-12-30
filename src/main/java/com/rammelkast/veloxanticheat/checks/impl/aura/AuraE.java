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
package com.rammelkast.veloxanticheat.checks.impl.aura;

import org.bukkit.entity.LivingEntity;

import com.rammelkast.veloxanticheat.checks.CheckInfo;
import com.rammelkast.veloxanticheat.checks.type.CombatCheck;
import com.rammelkast.veloxanticheat.player.PlayerWrapper;
import com.rammelkast.veloxanticheat.utils.Exemption;

@CheckInfo(name = "Aura", type = 'E')
public final class AuraE extends CombatCheck {

	public AuraE(final PlayerWrapper wrapper) {
		super(wrapper);
	}

	@Override
	public void process(final LivingEntity target) {
		if (exempt(Exemption.CONNECTING, Exemption.SERVER_LAG, Exemption.BAD_CONNECTION)) {
			resetBuffer();
			return;
		}

		final int streak = this.wrapper.getMotionProcessor().getPacketStreak();
		/**
		 * Some clients still send an additional packet (or in the wrong order) before
		 * hitting with aura
		 */
		if (streak == 1) {
			if (increaseBuffer() > 3) {
				fail("post: true");
			}
		} else {
			this.buffer /= 4;
		}
	}

}