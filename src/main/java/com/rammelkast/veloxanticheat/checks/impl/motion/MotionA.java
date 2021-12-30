package com.rammelkast.veloxanticheat.checks.impl.motion;

import org.bukkit.potion.PotionEffectType;

import com.rammelkast.veloxanticheat.checks.CheckInfo;
import com.rammelkast.veloxanticheat.checks.type.MotionCheck;
import com.rammelkast.veloxanticheat.player.PlayerWrapper;
import com.rammelkast.veloxanticheat.settings.Setting;
import com.rammelkast.veloxanticheat.settings.Setting.SettingType;
import com.rammelkast.veloxanticheat.utils.Exemption;
import com.rammelkast.veloxanticheat.utils.MathLib;
import com.rammelkast.veloxanticheat.utils.Motion;

/**
 * Jump motion check
 */
@CheckInfo(name = "Motion", type = 'A')
public final class MotionA extends MotionCheck {

	// Settings
	private static final Setting RUBBERBAND = new Setting("rubberband", SettingType.BOOLEAN);
	
	public MotionA(final PlayerWrapper wrapper) {
		super(wrapper);
	}

	@Override
	public void process(final Motion motion) {
		if (exempt(Exemption.CONNECTING, Exemption.CHUNK_LOADING, Exemption.GLIDING, Exemption.LIQUID,
				Exemption.CLIMBABLE, Exemption.BED, Exemption.SLIME, Exemption.HEAD_COLLISION)) {
			return;
		}

		final double deltaY = motion.getY();
		if (exempt(Exemption.LEVITATING)) {
			// TODO move to different check because of falsing ( needs own buffer )
			final double lastDeltaY = this.wrapper.getMotionProcessor().getPrevious().getY();
			final double expected = (lastDeltaY
					+ ((0.05 * MathLib.getPotionLevel(this.wrapper.getPlayer(), PotionEffectType.LEVITATION)
							- lastDeltaY) * 0.2) * 0.98f)
					+ 0.008;
			final double difference = deltaY - expected;
			if (difference > 0.001) {
				fail("levitating: true, difference: " + MathLib.roundDouble(difference, 3), true);
			}
			return;
		}

		final double lastY = motion.getFrom().getY();
		final boolean wasMathGround = MathLib.computeGrounded(lastY);
		final boolean stepping = MathLib.computeGrounded(deltaY) && wasMathGround;
		final double jumpModifier = MathLib.getPotionLevel(this.wrapper.getPlayer(), PotionEffectType.JUMP) * 0.1f;
		final double jumpMotion = 0.42f + jumpModifier;

		/**
		 * Checks if we're leaving the ground but
		 * the Y delta does not match a jump motion
		 */
		if (deltaY != jumpMotion && deltaY > 0.0 && !motion.isOnGround() && wasMathGround && !stepping
				&& this.wrapper.getMotionProcessor().bopTimer.hasPassed(2)) {
			fail("stepping: false, delta: " + MathLib.roundDouble(deltaY, 2), RUBBERBAND.getBoolean());
			return;
		}

		/**
		 * Some clients still have a step height = 1.0f module
		 */
		if (stepping && deltaY > 0.6f) {
			fail("stepping: true, delta: " + MathLib.roundDouble(deltaY, 2), RUBBERBAND.getBoolean());
			return;
		}

		if (deltaY > 0.6f + jumpModifier) {
			fail("delta: " + MathLib.roundDouble(deltaY, 2), RUBBERBAND.getBoolean());
			return;
		}
	}

}
