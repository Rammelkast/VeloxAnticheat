package com.rammelkast.veloxanticheat.checks.impl.fly;

import com.rammelkast.veloxanticheat.checks.CheckInfo;
import com.rammelkast.veloxanticheat.checks.type.MotionCheck;
import com.rammelkast.veloxanticheat.player.PlayerWrapper;
import com.rammelkast.veloxanticheat.settings.Setting;
import com.rammelkast.veloxanticheat.settings.Setting.SettingType;
import com.rammelkast.veloxanticheat.utils.Exemption;
import com.rammelkast.veloxanticheat.utils.Motion;

/**
 * Collision ground spoof check
 */
@CheckInfo(name = "Fly", type = 'D')
public final class FlyD extends MotionCheck {

	// Settings
	private static final Setting RUBBERBAND = new Setting("rubberband", SettingType.BOOLEAN);
	
	public FlyD(final PlayerWrapper wrapper) {
		super(wrapper);
	}

	@Override
	public void process(final Motion motion) {
		if (exempt(Exemption.CONNECTING, Exemption.CHUNK_LOADING, Exemption.GLIDING, Exemption.LEVITATING,
				Exemption.LIQUID, Exemption.CLIMBABLE)) {
			resetBuffer();
			return;
		}

		final boolean clientGround = motion.isOnGround();
		final boolean freeInAir = this.wrapper.getMotionProcessor().isFreeInAir();
		/**
		 * Client says it's on ground while completely free in air -> flag
		 */
		if (clientGround && freeInAir) {
			if (increaseBuffer() > 2) {
				fail("client: " + clientGround + ", free: " + freeInAir, RUBBERBAND.getBoolean());
			}
		} else if (!clientGround && !freeInAir) {
			this.buffer /= 2;
		} else {
			this.buffer /= 4;
		}
	}

}
