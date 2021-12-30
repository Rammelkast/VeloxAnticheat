package com.rammelkast.veloxanticheat.checks.impl.fly;

import com.rammelkast.veloxanticheat.checks.CheckInfo;
import com.rammelkast.veloxanticheat.checks.type.MotionCheck;
import com.rammelkast.veloxanticheat.player.PlayerWrapper;
import com.rammelkast.veloxanticheat.settings.Setting;
import com.rammelkast.veloxanticheat.settings.Setting.SettingType;
import com.rammelkast.veloxanticheat.utils.Exemption;
import com.rammelkast.veloxanticheat.utils.MathLib;
import com.rammelkast.veloxanticheat.utils.Motion;

/**
 * Mathematical ground spoof check
 */
@CheckInfo(name = "Fly", type = 'B')
public final class FlyB extends MotionCheck {
	
	// Settings
	private static final Setting RUBBERBAND = new Setting("rubberband", SettingType.BOOLEAN);
	
	public FlyB(final PlayerWrapper wrapper) {
		super(wrapper);
	}

	@Override
	public void process(final Motion motion) {
		if (exempt(Exemption.CONNECTING, Exemption.CHUNK_LOADING, Exemption.GLIDING, Exemption.LEVITATING,
				Exemption.LIQUID, Exemption.CLIMBABLE)) {
			decreaseBuffer(0.25);
			return;
		}
		
		/**
		 * When we should mathematically be on ground (or not)
		 * but the client says otherwise, we flag
		 */
		final boolean clientGround = motion.isOnGround();
		final boolean mathGround = MathLib.computeGrounded(motion.getFrom().getY()) && MathLib.computeGrounded(motion.getTo().getY());
		if (clientGround != mathGround) {
			if (increaseBuffer() > 4) {
				fail("client: " + clientGround + ", math: " + mathGround, RUBBERBAND.getBoolean());
			}
		} else {
			this.buffer /= 2;
		}
	}

}
