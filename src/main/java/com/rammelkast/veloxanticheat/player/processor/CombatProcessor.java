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
package com.rammelkast.veloxanticheat.player.processor;

import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

import com.rammelkast.veloxanticheat.VeloxAnticheat;
import com.rammelkast.veloxanticheat.checks.type.CombatCheck;
import com.rammelkast.veloxanticheat.player.PlayerWrapper;

public final class CombatProcessor extends Processor {

	private LivingEntity current, previous;
	private int lastHit;
	
	@Override
	public void process(final PlayerWrapper wrapper, final Event event) {
		if (!(event instanceof EntityDamageByEntityEvent)) {
			return;
		}
		
		final EntityDamageByEntityEvent damage = (EntityDamageByEntityEvent) event;
		if (damage.getCause() != DamageCause.ENTITY_ATTACK) {
			// Not damaged by combat -> do not continue
			return;
		}
		
		final Entity entity = damage.getEntity();
		if (entity.isDead() || !(entity instanceof LivingEntity)) {
			// Entity is dead or not living -> do not continue
			return;
		}
		
		final LivingEntity target = (LivingEntity) entity;
		{
			// Update target and previous target
			this.previous = this.current;
			this.current = target;
		}
		
		// Pass target to checks
		wrapper.getChecks().stream().filter(CombatCheck.class::isInstance)
				.filter(check -> VeloxAnticheat.getInstance().getSettingsManager().isCheckEnabled(check))
				.forEach(check -> ((CombatCheck) check).process(target));
		
		this.lastHit = VeloxAnticheat.getInstance().getTicks();
	}

	/**
	 * Gets the current target
	 * 
	 * @return the current target
	 */
	public LivingEntity getCurrent() {
		return this.current;
	}
	
	/**
	 * Gets the previous target
	 * 
	 * @return the previous target
	 */
	public LivingEntity getPrevious() {
		return this.previous;
	}
	
	/**
	 * Gets the tick in which the last hit occurred
	 * 
	 * @return the tick of the last hit
	 */
	public int getLastHit() {
		return this.lastHit;
	}

	/**
	 * Gets if the player is currently in combat
	 * 
	 * @return true if in combat
	 */
	public boolean isInCombat() {
		return VeloxAnticheat.getInstance().getTicks() - getLastHit() < 3;
	}
	
}
