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

import java.util.ArrayList;
import java.util.List;

import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerVelocityEvent;
import org.bukkit.util.Vector;

import com.rammelkast.veloxanticheat.VeloxAnticheat;
import com.rammelkast.veloxanticheat.player.PlayerWrapper;
import com.rammelkast.veloxanticheat.utils.MathLib;

public final class VelocityProcessor extends Processor {

	private static final long VELOCITIZED_TIME = VeloxAnticheat.getInstance().getSettingsManager().getVelocityTime()
			* 50L;

	private final List<TimedVelocity> velocities = new ArrayList<>();

	@Override
	public void process(final PlayerWrapper wrapper, final Event event) {
		if (!(event instanceof PlayerVelocityEvent)) {
			return;
		}

		final PlayerVelocityEvent velocity = (PlayerVelocityEvent) event;
		registerVelocity(velocity.getVelocity());
	}

	protected void registerVelocity(final Vector vector) {
		this.velocities.add(new TimedVelocity(vector.getX(), vector.getY(), vector.getZ(),
				Math.hypot(vector.getX(), vector.getZ()), Math.abs(vector.getY()), MathLib.now()));
	}

	public void tick() {
		this.velocities.removeIf(velocity -> (velocity.getTimestamp() + VELOCITIZED_TIME < MathLib.now()));
	}

	public double getHorizontal() {
		return Math.sqrt(this.velocities.parallelStream().mapToDouble(TimedVelocity::getHorizontal).max().orElse(0.0D));
	}

	public double getVertical() {
		return Math.sqrt(this.velocities.parallelStream().mapToDouble(TimedVelocity::getVertical).max().orElse(0.0D));
	}

	public boolean isVelocitized() {
		return this.velocities.size() != 0;
	}

	private class TimedVelocity {
		@SuppressWarnings("unused")
		private final double motionX, motionY, motionZ;
		private final double horizontal, vertical;
		private final long timestamp;

		protected TimedVelocity(final double motionX, final double motionY, final double motionZ,
				final double horizontal, final double vertical, final long timestamp) {
			this.motionX = motionX;
			this.motionY = motionY;
			this.motionZ = motionZ;
			this.horizontal = horizontal;
			this.vertical = vertical;
			this.timestamp = timestamp;
		}

		public double getHorizontal() {
			return this.horizontal;
		}

		public double getVertical() {
			return this.vertical;
		}

		public long getTimestamp() {
			return this.timestamp;
		}
	}

}