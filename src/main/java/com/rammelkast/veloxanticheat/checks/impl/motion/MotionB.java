package com.rammelkast.veloxanticheat.checks.impl.motion;

import com.rammelkast.veloxanticheat.checks.CheckInfo;
import com.rammelkast.veloxanticheat.checks.type.MotionCheck;
import com.rammelkast.veloxanticheat.player.PlayerWrapper;
import com.rammelkast.veloxanticheat.settings.Setting;
import com.rammelkast.veloxanticheat.settings.Setting.SettingType;
import com.rammelkast.veloxanticheat.utils.Exemption;
import com.rammelkast.veloxanticheat.utils.MathLib;
import com.rammelkast.veloxanticheat.utils.Motion;

@CheckInfo(name = "Motion", type = 'B')
public final class MotionB extends MotionCheck {

	// Settings
	private static final Setting RUBBERBAND = new Setting("rubberband", SettingType.BOOLEAN);
	
	public MotionB(final PlayerWrapper wrapper) {
		super(wrapper);
	}

	@Override
	public void process(Motion motion) {
		if (exempt(Exemption.CONNECTING, Exemption.CHUNK_LOADING, Exemption.CREATIVE, Exemption.GLIDING,
				Exemption.LIQUID, Exemption.CLIMBABLE)) {
			return;
		}

		final int groundTicks = this.wrapper.getMotionProcessor().getGroundTicks();
		final double deltaY = motion.getY();
		final double lastY = motion.getFrom().getY();
		final boolean stepping = MathLib.computeGrounded(deltaY) && MathLib.computeGrounded(lastY);
		if (groundTicks > 5 && deltaY != 0.0 && !stepping) {
			if (increaseBuffer() > 1) {
				fail("ground: " + groundTicks + ", delta: " + MathLib.roundDouble(deltaY, 2), RUBBERBAND.getBoolean());
			}
		} else {
			decreaseBuffer(0.05);
		}
	}

}
