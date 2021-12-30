package com.rammelkast.veloxanticheat.checks.impl.fly;

import com.rammelkast.veloxanticheat.checks.CheckInfo;
import com.rammelkast.veloxanticheat.checks.type.MotionCheck;
import com.rammelkast.veloxanticheat.player.PlayerWrapper;
import com.rammelkast.veloxanticheat.settings.Setting;
import com.rammelkast.veloxanticheat.settings.Setting.SettingType;
import com.rammelkast.veloxanticheat.utils.Exemption;
import com.rammelkast.veloxanticheat.utils.MathLib;
import com.rammelkast.veloxanticheat.utils.Motion;

@CheckInfo(name = "Fly", type = 'E')
public final class FlyE extends MotionCheck {

	// Settings
	private static final Setting RUBBERBAND = new Setting("rubberband", SettingType.BOOLEAN);
	
	private double previousAcceleration;
	private double previousJolt;

	public FlyE(final PlayerWrapper wrapper) {
		super(wrapper);
	}

	@Override
	public void process(final Motion motion) {
		if (exempt(Exemption.CONNECTING, Exemption.CHUNK_LOADING, Exemption.GLIDING, Exemption.LEVITATING,
				Exemption.LIQUID, Exemption.CLIMBABLE)) {
			resetBuffer();
			return;
		}

		final double motionY = motion.getY();
		final double previousY = this.wrapper.getMotionProcessor().getPrevious().getY();
		final double acceleration = Math.abs(motionY - previousY);
		final double jolt = Math.abs(acceleration - this.previousAcceleration);
		final double joltChange = jolt - this.previousJolt;
		final double verticalVelocity = this.wrapper.getVelocityProcessor().getVertical();
		final boolean freeInAir = this.wrapper.getMotionProcessor().isFreeInAir();
		final int airTicks = this.wrapper.getMotionProcessor().getAirTicks();
		if (freeInAir) {
			/**
			 * After 6 ticks in the air, this can't happen
			 * Targets jetpack, air jumps, etc.
			 */
			if (airTicks > 6 && motionY != 0.0 && jolt > verticalVelocity && joltChange > verticalVelocity) {
				if (increaseBuffer() > 2) {
					fail("jolt: " + MathLib.roundDouble(jolt, 3) + ", change: " + MathLib.roundDouble(joltChange, 3),
							RUBBERBAND.getBoolean());
				}
			} else {
				decreaseBuffer(0.05);
			}
		} else {
			decreaseBuffer(motion.isOnGround() ? 1.25 : 1.0);
		}

		this.previousAcceleration = acceleration;
		this.previousJolt = jolt;
	}

}
