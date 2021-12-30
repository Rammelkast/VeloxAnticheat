package com.rammelkast.veloxanticheat.checks.impl.aura;

import org.bukkit.entity.LivingEntity;

import com.rammelkast.veloxanticheat.checks.CheckInfo;
import com.rammelkast.veloxanticheat.checks.type.CombatCheck;
import com.rammelkast.veloxanticheat.player.PlayerWrapper;
import com.rammelkast.veloxanticheat.utils.Exemption;

@CheckInfo(name = "Aura", type = 'E')
public final class AuraE extends CombatCheck {

	public AuraE(final PlayerWrapper wrapper) {
		super(wrapper);
	}

	@Override
	public void process(final LivingEntity target) {
		if (exempt(Exemption.CONNECTING, Exemption.SERVER_LAG, Exemption.BAD_CONNECTION)) {
			resetBuffer();
			return;
		}

		final int streak = this.wrapper.getMotionProcessor().getPacketStreak();
		/**
		 * Some clients still send an additional packet (or in the wrong order) before
		 * hitting with aura
		 */
		if (streak == 1) {
			if (increaseBuffer() > 3) {
				fail("post: true");
			}
		} else {
			this.buffer /= 4;
		}
	}

}