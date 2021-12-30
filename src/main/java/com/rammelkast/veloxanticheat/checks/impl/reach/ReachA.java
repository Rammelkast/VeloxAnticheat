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
package com.rammelkast.veloxanticheat.checks.impl.reach;

import org.bukkit.entity.LivingEntity;
import org.bukkit.util.NumberConversions;
import org.bukkit.util.Vector;

import com.rammelkast.veloxanticheat.checks.CheckInfo;
import com.rammelkast.veloxanticheat.checks.type.CombatCheck;
import com.rammelkast.veloxanticheat.player.PlayerWrapper;
import com.rammelkast.veloxanticheat.utils.Exemption;
import com.rammelkast.veloxanticheat.utils.MathLib;

/**
 * Hitbox-distance based reach check
 * 
 * TODO
 * This needs more verification
 */
@CheckInfo(name = "Reach", type = 'A')
public final class ReachA extends CombatCheck {

	private static final double HITBOX_EXPANSION = 0.15; // TODO is this OK?
	
	public ReachA(final PlayerWrapper wrapper) {
		super(wrapper);
	}

	@Override
	public void process(final LivingEntity target) {
		if (exempt(Exemption.CONNECTING, Exemption.CHUNK_LOADING, Exemption.CREATIVE, Exemption.SERVER_LAG)) {
			resetBuffer();
			return;
		}

		final Vector player = this.wrapper.getPlayer().getLocation().toVector().setY(0);
		final double hitbox = (Math.max(target.getBoundingBox().getWidthX(), target.getBoundingBox().getWidthZ()) / 2)
				+ HITBOX_EXPANSION;
		final double distance = player.distance(target.getLocation().toVector().clone().setY(0)) - hitbox;
		final int ticks = NumberConversions.ceil(this.wrapper.getPlayer().getPing() / 50.0);
		final double limit = Math.min(4.5, 3.2 + (ticks * 0.3)
				+ MathLib.roundDouble(Math.hypot(target.getVelocity().getX(), target.getVelocity().getZ()) * 1.5, 1));
		if (distance > limit) {
			// Verbose for debugging
			verbose("distance: " + MathLib.roundDouble(distance, 3) + "/" + MathLib.roundDouble(limit, 2) + ", expand: " + MathLib.roundDouble(hitbox, 3));
			verbose("ticks: " + ticks + " (" + this.wrapper.getPlayer().getPing() + "ms)");
			
			if (increaseBuffer(distance > (limit * 1.2) ? 1.25 : 1.0) > 5) {
				fail("distance: " + MathLib.roundDouble(distance, 2) + "/" + MathLib.roundDouble(limit, 2) + ", expand: " + MathLib.roundDouble(hitbox, 2));
				this.buffer /= 2;
			}
		} else {
			decreaseBuffer(ticks > 3 ? 0.4 : 0.2);
		}
	}

}
