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

import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.NumberConversions;

/**
 * Library for world related functions
 */
public final class WorldLib {

	/**
	 * Gets if a worlds chunk at the specified X position and Z position
	 * 
	 * @param world The world of the chunk
	 * @param x The X position
	 * @param z The Z position
	 * @return if the chunk is loaded
	 */
	public static boolean isChunkLoaded(final World world, final double x, final double z) {
		return world.isChunkLoaded(NumberConversions.floor(x) >> 4, NumberConversions.floor(z) >> 4);
	}
	
	/**
	 * Draws the given bounding box to the given player in particle form
	 * 
	 * @param player The particle target player
	 * @param box The bounding box
	 * @param thickness The line thickness
	 */
	public static void drawBoundingBox(final Player player, final BoundingBox box, final double thickness) {
        for (double x = box.getMinX(); x < box.getMaxX() + 0.1; x += thickness) {
            for (double y = box.getMinY(); y < box.getMaxY() + 0.1; y += thickness) {
                for (double z = box.getMinZ(); z < box.getMaxZ() + 0.1; z += thickness) {
                   player.spawnParticle(Particle.FLAME, x, y, z, 0, 0, 0, 0, 1, null);
                }
            }
        }
    }
	
}
