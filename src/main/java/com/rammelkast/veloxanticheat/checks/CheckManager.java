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
package com.rammelkast.veloxanticheat.checks;

import org.bukkit.plugin.Plugin;

import com.google.common.collect.ClassToInstanceMap;
import com.google.common.collect.ImmutableClassToInstanceMap;
import com.rammelkast.veloxanticheat.checks.impl.aura.AuraA;
import com.rammelkast.veloxanticheat.checks.impl.aura.AuraB;
import com.rammelkast.veloxanticheat.checks.impl.aura.AuraC;
import com.rammelkast.veloxanticheat.checks.impl.aura.AuraD;
import com.rammelkast.veloxanticheat.checks.impl.aura.AuraE;
import com.rammelkast.veloxanticheat.checks.impl.aura.AuraF;
import com.rammelkast.veloxanticheat.checks.impl.aura.AuraG;
import com.rammelkast.veloxanticheat.checks.impl.fly.FlyA;
import com.rammelkast.veloxanticheat.checks.impl.fly.FlyB;
import com.rammelkast.veloxanticheat.checks.impl.fly.FlyC;
import com.rammelkast.veloxanticheat.checks.impl.fly.FlyD;
import com.rammelkast.veloxanticheat.checks.impl.fly.FlyE;
import com.rammelkast.veloxanticheat.checks.impl.motion.MotionA;
import com.rammelkast.veloxanticheat.checks.impl.motion.MotionB;
import com.rammelkast.veloxanticheat.checks.impl.reach.ReachA;
import com.rammelkast.veloxanticheat.checks.impl.speed.SpeedA;
import com.rammelkast.veloxanticheat.checks.impl.speed.SpeedB;
import com.rammelkast.veloxanticheat.player.PlayerWrapper;

public final class CheckManager {

	public void enable(final Plugin plugin) {
		// TODO Auto-generated method stub
		
	}
	
	public void disable() {
		// TODO Auto-generated method stub
		
	}
	
	/**
	 * Creates the map of checks for a player
	 * 
	 * @param wrapper The player wrapper
	 * @return the map of checks
	 */
	public ClassToInstanceMap<Check<?>> createChecks(final PlayerWrapper wrapper) {
		return new ImmutableClassToInstanceMap.Builder<Check<?>>()
				.put(AuraA.class, new AuraA(wrapper))
				.put(AuraB.class, new AuraB(wrapper))
				.put(AuraC.class, new AuraC(wrapper))
				.put(AuraD.class, new AuraD(wrapper))
				.put(AuraE.class, new AuraE(wrapper))
				.put(AuraF.class, new AuraF(wrapper))
				.put(AuraG.class, new AuraG(wrapper))
				.put(FlyA.class, new FlyA(wrapper))
				.put(FlyB.class, new FlyB(wrapper))
				.put(FlyC.class, new FlyC(wrapper))
				.put(FlyD.class, new FlyD(wrapper))
				.put(FlyE.class, new FlyE(wrapper))
				.put(MotionA.class, new MotionA(wrapper))
				.put(MotionB.class, new MotionB(wrapper))
				.put(ReachA.class, new ReachA(wrapper))
				.put(SpeedA.class, new SpeedA(wrapper))
				.put(SpeedB.class, new SpeedB(wrapper))
				.build();
	}
	
}