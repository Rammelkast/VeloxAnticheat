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
/**
 * 
 */
package com.rammelkast.veloxanticheat.events.impl;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import com.rammelkast.veloxanticheat.checks.Check;

/**
 * Called when a check flags
 */
public final class VeloxFlagEvent extends Event {

	private static final HandlerList HANDLERS = new HandlerList();
	
	private final Check<?> check;
	private final float totalViolations;
	
	public VeloxFlagEvent(final Check<?> check, final float totalViolations) {
		this.check = check;
		this.totalViolations = totalViolations;
	}
	
	/**
	 * Get the failed check
	 * 
	 * @return the failed {@link Check}
	 */
	public Check<?> getCheck() {
		return this.check;
	}

	/**
	 * Get the total violation count for this check type
	 * 
	 * @return the total violation count
	 */
	public float getTotalViolations() {
		return this.totalViolations;
	}
	
	@Override
	public HandlerList getHandlers() {
		return HANDLERS;
	}

}
