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
package com.rammelkast.veloxanticheat.utils;

import org.bukkit.Material;
import org.bukkit.block.Block;

/**
 * Friction data library to prevent using NMS
 */
public enum Friction {

	DEFAULT(0.6f),
	ICE(0.98f),
	BLUE_ICE(0.989f),
	PACKED_ICE(0.98f),
	SLIME_BLOCK(0.8f),
	;
	
	private final float factor;

	Friction(final float factor) {
		this.factor = factor;
	}

	/**
	 * Gets the friction factor
	 * 
	 * @return the factor
	 */
	public float getFactor() {
		return this.factor;
	}
	
	/**
	 * Gets the friction factor for the given block
	 * 
	 * @param block The block
	 * @return the friction factor
	 */
	public static float getFactor(final Block block) {
		final Material type = block.getType();
		try {
			return valueOf(type.name()).getFactor();
		} catch (final Exception exception) {
			return DEFAULT.getFactor();
		}
	}
	
}
