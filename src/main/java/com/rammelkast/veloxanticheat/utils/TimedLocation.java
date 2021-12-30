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

import org.bukkit.Location;
import org.bukkit.World;

/**
 * Extension of Bukkit {@link Location} object with included time stamp
 */
public final class TimedLocation extends Location {

	private final long timestamp;

	public TimedLocation(final World world, final double x, final double y, final double z, final float yaw,
			final float pitch, final long timestamp) {
		super(world, x, y, z, yaw, pitch);
		this.timestamp = timestamp;
	}

	public TimedLocation(final Location location, final long timestamp) {
		this(location.getWorld(), location.getX(), location.getY(), location.getZ(), location.getYaw(),
				location.getPitch(), timestamp);
	}

	/**
	 * Gets the time stamp for this location
	 * 
	 * @return the time stamp
	 */
	public long getTimestamp() {
		return this.timestamp;
	}

}
