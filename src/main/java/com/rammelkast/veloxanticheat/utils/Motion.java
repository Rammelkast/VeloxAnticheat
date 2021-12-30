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
import org.bukkit.util.Vector;

/**
 * Represents the deltas and state changes between two locations
 */
public final class Motion {

	private final Location from, to;
	private final boolean wasOnGround, onGround;
	
	private final double x, y, z;
	private final double horizontal;
	
	private final float yaw, pitch;
	
	public Motion(final Location from, final Location to, final boolean wasOnGround, final boolean onGround) {
		this.from = from;
		this.to = to;
		this.wasOnGround = wasOnGround;
		this.onGround = onGround;
		
		this.x = to.getX() - from.getX();
		this.y = to.getY() - from.getY();
		this.z = to.getZ() - from.getZ();
		this.horizontal = Math.hypot(this.x, this.z);
		
		this.yaw = Math.abs(to.getYaw() - from.getYaw()) % 360f;
		this.pitch = Math.abs(to.getPitch() - from.getPitch());
	}

	/**
	 * Gets the location this motion is moving from
	 * 
	 * @return the from location
	 */
	public Location getFrom() {
		return this.from;
	}

	/**
	 * Gets the location this motion is moving towards
	 * 
	 * @return the towards location
	 */
	public Location getTo() {
		return this.to;
	}
	
	/**
	 * Get if the player was on ground
	 * This is sent by the client and unchecked
	 * 
	 * @return the previous player ground state
	 */
	public boolean wasOnGround() {
		return this.wasOnGround;
	}
	
	/**
	 * Get if the player is on ground
	 * This is sent by the client and unchecked
	 * 
	 * @return the current player ground state
	 */
	public boolean isOnGround() {
		return this.onGround;
	}
	
	/**
	 * Gets the X-axis of this motion
	 * 
	 * @return the motion X-axis
	 */
	public double getX() {
		return this.x;
	}
	
	/**
	 * Gets the Y-axis of this motion
	 * 
	 * @return the motion Y-axis
	 */
	public double getY() {
		return this.y;
	}
	
	/**
	 * Gets the Z-axis of this motion
	 * 
	 * @return the motion Z-axis
	 */
	public double getZ() {
		return this.z;
	}
	
	/**
	 * Gets the horizontal delta of this motion
	 * 
	 * @return the horizontal delta
	 */
	public double getHorizontal() {
		return this.horizontal;
	}
	
	/**
	 * Gets the yaw delta of this motion
	 * 
	 * @return the yaw delta
	 */
	public float getYaw() {
		return this.yaw;
	}
	
	/**
	 * Gets the pitch delta of this motion
	 * 
	 * @return the pitch delta
	 */
	public float getPitch() {
		return this.pitch;
	}
	
	/**
	 * Gets the angle of this motion
	 * 
	 * @return the motion angle
	 */
	public float getAngle() {
		final float moveAngle = (float) (Math.toDegrees(Math.atan2(this.z, this.x)) - 90.0f);
		return Math.abs(MathLib.getAngleWrapped(moveAngle - this.from.getYaw()));
	}
	
	/**
	 * Gets if the motion has a rotation
	 * 
	 * @return true if rotating
	 */
	public boolean hasRotation() {
		return this.yaw > 0.0 || this.pitch > 0.0;
	}
	
	/**
	 * Gets the motion represented as a {@link Vector}
	 * 
	 * @return the motion as a vector
	 */
	public Vector toVector() {
		return new Vector(this.x, this.y, this.z);
	}
	
}
