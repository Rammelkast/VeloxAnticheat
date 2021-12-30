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

import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.RayTraceResult;

import com.rammelkast.veloxanticheat.checks.CheckInfo;
import com.rammelkast.veloxanticheat.checks.type.CombatCheck;
import com.rammelkast.veloxanticheat.player.PlayerWrapper;
import com.rammelkast.veloxanticheat.utils.Exemption;
import com.rammelkast.veloxanticheat.utils.MathLib;

/**
 * TODO better player/server lag adjustments
 */
@CheckInfo(name = "Aura", type = 'A')
public final class AuraA extends CombatCheck {

	private static final double HITBOX_EXPANSION = 0.6; // TODO per-entity?
	private static final double RAY_LENGTH = 4.5;

	public AuraA(final PlayerWrapper wrapper) {
		super(wrapper);
	}

	@Override
	public void process(final LivingEntity target) {
		if (exempt(Exemption.CONNECTING, Exemption.CHUNK_LOADING, Exemption.CREATIVE, Exemption.SERVER_LAG)) {
			decreaseBuffer();
			return;
		}

		/**
		 * Ray tracing from player eyes to target entity
		 * Will flag if distance is larger than 0.3 and
		 * player view is obscured by a block
		 */
		final Location eyes = this.wrapper.getPlayer().getEyeLocation().clone();
		final World world = this.wrapper.getPlayer().getWorld();
		final RayTraceResult result = world.rayTrace(eyes, eyes.getDirection(), RAY_LENGTH, FluidCollisionMode.NEVER,
				true, HITBOX_EXPANSION, entity -> !entity.equals(this.wrapper.getPlayer()));
		if (result != null) {
			final Block block = result.getHitBlock();
			final Entity entity = result.getHitEntity();
			final double distance = result.getHitPosition().distance(eyes.toVector());
			if (entity == null && block != null && distance > 0.3) {
				// Verbose for debugging
				verbose("distance: " + MathLib.roundDouble(distance, 2) + ", block: "
						+ block.getType().name().toLowerCase());
				
				if (increaseBuffer() > 2) {
					fail("distance: " + MathLib.roundDouble(distance, 2) + ", block: "
							+ block.getType().name().toLowerCase());
					this.buffer /= 2;
				}
			} else {
				decreaseBuffer(0.2);
			}
		} else {
			// We are most likely hitting outside ray length
			// Either using reach, or lagging
			// For now we let the reach and angle checks handle this
			verbose("ray had no hit");
		}
	}

}
