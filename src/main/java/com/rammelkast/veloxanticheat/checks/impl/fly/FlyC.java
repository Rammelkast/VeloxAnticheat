package com.rammelkast.veloxanticheat.checks.impl.fly;

import com.rammelkast.veloxanticheat.checks.CheckInfo;
import com.rammelkast.veloxanticheat.checks.type.MotionCheck;
import com.rammelkast.veloxanticheat.player.PlayerWrapper;
import com.rammelkast.veloxanticheat.settings.Setting;
import com.rammelkast.veloxanticheat.settings.Setting.SettingType;
import com.rammelkast.veloxanticheat.utils.Exemption;
import com.rammelkast.veloxanticheat.utils.MathLib;
import com.rammelkast.veloxanticheat.utils.Motion;

@CheckInfo(name = "Fly", type = 'C')
public final class FlyC extends MotionCheck {
	
	// Settings
	private static final Setting RUBBERBAND = new Setting("rubberband", SettingType.BOOLEAN);
	
	public FlyC(final PlayerWrapper wrapper) {
		super(wrapper);
	}

	@Override
	public void process(final Motion motion) {
		if (exempt(Exemption.CONNECTING, Exemption.CHUNK_LOADING, Exemption.GLIDING, Exemption.LEVITATING,
				Exemption.LIQUID, Exemption.CLIMBABLE)) {
			decreaseBuffer();
			return;
		}
		
		final double horizontal = motion.getHorizontal();
		final double motionY = motion.getY();
		final double previousY = this.wrapper.getMotionProcessor().getPrevious().getY();
		final double acceleration = Math.abs(motionY - previousY);
		final boolean freeInAir = this.wrapper.getMotionProcessor().isFreeInAir();
		final int airTicks = this.wrapper.getMotionProcessor().getAirTicks();
		if (freeInAir) {
			/**
			 * After 6 ticks in the air, Y axis acceleration likely isn't 0.0
			 */
			if (airTicks > 6 && horizontal > 0.1 && acceleration == 0.0) {
				if (increaseBuffer() > 3) {
					fail("ticks: " + airTicks + ", horizontal: " + MathLib.roundDouble(horizontal, 2), RUBBERBAND.getBoolean());
					decreaseBuffer();
				}
			} else {
				decreaseBuffer(0.25);
			}
		} else {
			resetBuffer();
		}
	}

}
