package com.rammelkast.veloxanticheat.checks.type;

import org.bukkit.entity.LivingEntity;

import com.rammelkast.veloxanticheat.checks.Check;
import com.rammelkast.veloxanticheat.player.PlayerWrapper;

public abstract class CombatCheck extends Check<LivingEntity> {

	public CombatCheck(final PlayerWrapper wrapper) {
		super(wrapper);
	}

}
