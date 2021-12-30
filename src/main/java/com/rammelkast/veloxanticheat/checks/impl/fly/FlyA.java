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
 * Gravity prediction check
 */
@CheckInfo(name = "Fly", type = 'A')
public final class FlyA extends MotionCheck {

	private static final float GRAVITY_CONSTANT = 0.98f;
	
	// Settings
	private static final Setting RUBBERBAND = new Setting("rubberband", SettingType.BOOLEAN);
	
	public FlyA(final PlayerWrapper wrapper) {
		super(wrapper);
	}

	@Override
	public void process(final Motion motion) {
		if (exempt(Exemption.CONNECTING, Exemption.CHUNK_LOADING, Exemption.GLIDING, Exemption.LEVITATING,
				Exemption.LIQUID, Exemption.CLIMBABLE)) {
			decreaseBuffer(0.25);
			return;
		}
		
		final double motionY = motion.getY();
		if (Math.abs(motionY) + 0.098f < 0.05) {
			// Not interesting
			return;
		}
		
		/**
		 * Compares against expected client code gravity
		 */
		final double previousY = this.wrapper.getMotionProcessor().getPrevious().getY();
		final double prediction = (previousY - 0.08) * GRAVITY_CONSTANT;
		final double difference = Math.abs(prediction - motionY);
		final int airTicks = this.wrapper.getMotionProcessor().getAirTicks();
		if (airTicks > 5 && difference > 1E-10) {
			if (increaseBuffer() > 4) {
				fail("difference: " + MathLib.roundDouble(difference, 3), RUBBERBAND.getBoolean());
				this.buffer /= 2;
			}
		} else {
			decreaseBuffer(0.75);
		}
	}

}
