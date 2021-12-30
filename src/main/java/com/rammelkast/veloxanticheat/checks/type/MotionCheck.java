package com.rammelkast.veloxanticheat.checks.type;

import com.rammelkast.veloxanticheat.checks.Check;
import com.rammelkast.veloxanticheat.player.PlayerWrapper;
import com.rammelkast.veloxanticheat.utils.Motion;

public abstract class MotionCheck extends Check<Motion> {

	public MotionCheck(final PlayerWrapper wrapper) {
		super(wrapper);
	}

}
