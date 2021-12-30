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

import java.util.function.Function;

import org.bukkit.GameMode;
import org.bukkit.potion.PotionEffectType;

import com.rammelkast.veloxanticheat.VeloxAnticheat;
import com.rammelkast.veloxanticheat.player.PlayerWrapper;

public enum Exemption {

	CONNECTING(wrapper -> MathLib.now()
			- wrapper.getConnectionTime() <= VeloxAnticheat.getInstance().getSettingsManager().getConnectTime() * 50L),

	CHUNK_LOADING(wrapper -> !WorldLib.isChunkLoaded(wrapper.getPlayer().getWorld(),
			wrapper.getPlayer().getLocation().getX(), wrapper.getPlayer().getLocation().getZ())),

	CREATIVE(wrapper -> wrapper.getPlayer().getGameMode() == GameMode.CREATIVE),

	GLIDING(wrapper -> wrapper.getPlayer().isGliding()), // TODO use a timer for this

	LEVITATING(wrapper -> wrapper.getPlayer().hasPotionEffect(PotionEffectType.LEVITATION)),

	LIQUID(wrapper -> wrapper.getMotionProcessor().isTouchingLiquid()),

	CLIMBABLE(wrapper -> wrapper.getMotionProcessor().isTouchingClimbable()),

	BED(wrapper -> wrapper.getMotionProcessor().isTouchingBed()),

	SLIME(wrapper -> wrapper.getMotionProcessor().isTouchingSlime()),

	HEAD_COLLISION(wrapper -> wrapper.getMotionProcessor().hasHeadCollision()),

	SERVER_LAG(wrapper -> VeloxAnticheat.getInstance().getTPS() < VeloxAnticheat.getInstance().getSettingsManager()
			.getLagExemptTPS()),

	BAD_CONNECTION(wrapper -> wrapper.getMotionProcessor().getLagFactor() > 1.5
			&& wrapper.getMotionProcessor().getPacketStreak() != 1),
	
	WAS_FLYING(wrapper -> wrapper.getMotionProcessor().flyingTimer.hasNotPassed(20)),
	
	RIPTIDING(wrapper -> wrapper.getPlayer().isRiptiding()); // TODO use a timer for this (5-10 ticks?)

	private final Function<PlayerWrapper, Boolean> action;

	Exemption(final Function<PlayerWrapper, Boolean> action) {
		this.action = action;
	}

	public Function<PlayerWrapper, Boolean> getAction() {
		return this.action;
	}

}
