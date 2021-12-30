/**
 * Copyright (c) 2021-2022 Marco Moesman ("Rammelkast")
 * This file, class and it's contents are licensed differently than VeloxAnticheat
 * 
 * You may NOT distribute this file, it's contents and/or derivatives thereof as
 * your own work, for commercial use, or without proper attribution to the author
 */
package com.rammelkast.veloxanticheat.checks.impl.aura;

import com.rammelkast.veloxanticheat.checks.CheckInfo;
import com.rammelkast.veloxanticheat.checks.type.MotionCheck;
import com.rammelkast.veloxanticheat.player.PlayerWrapper;
import com.rammelkast.veloxanticheat.utils.Exemption;
import com.rammelkast.veloxanticheat.utils.MathLib;
import com.rammelkast.veloxanticheat.utils.Motion;

@CheckInfo(name = "Aura", type = 'F')
public final class AuraF extends MotionCheck {

	/**
	 * This code is published under a different license
	 * Please read the copyright header above carefully
	 */
	public AuraF(final PlayerWrapper wrapper) {
		super(wrapper);
	}

	@Override
	public void process(final Motion motion) {
		if (exempt(Exemption.CONNECTING)) {
			resetBuffer();
			return;
		}
		
		if (!motion.hasRotation()) {
			// No rotation -> do not check
			return;
		}
		
		final boolean cinematic = this.wrapper.getMotionProcessor().isCinematic();
		final boolean attacking = this.wrapper.getCombatProcessor().isInCombat();
		if (!attacking || cinematic) {
			// Do not check
			return;
		}
		
		final float factor = motion.getYaw() / motion.getPitch();
		final float lastFactor = this.wrapper.getMotionProcessor().getPrevious().getYaw()
				/ this.wrapper.getMotionProcessor().getPrevious().getPitch();
		final long expanded = (long) (factor * MathLib.EXPANDER);
        final long lastExpanded = (long) (lastFactor * MathLib.EXPANDER);

        final double divisor = MathLib.getGcd(expanded, lastExpanded);
        final double adjusted = divisor / MathLib.EXPANDER;
		final double mod = motion.getYaw() % adjusted;
		/**
		 * The idea here is that we take pitch deltas as a factor of yaw deltas after
		 * which we go a bit nuts with the data, can't forget good ol' GCD too
		 * 
		 * This check was made at 2am and I have no idea how I got here, but man it's
		 * pretty good at flagging LiquidBounce
		 * 
		 * All values here are magic values obtained during testing with mainly LiquidBounce
		 */
		if (mod < 5e-4 && (factor > 0.0f && factor < 0.8f) && adjusted > 1e-3 && motion.getPitch() > 1.5f
				&& motion.getYaw() > 2.5f) {
			// Verbose for debugging
			verbose("factor: " + MathLib.roundDouble(factor, 2) + ", adjusted: " + MathLib.roundDouble(adjusted, 4)
					+ ", mod: " + MathLib.roundDouble(mod, 5) + ", pitch: " + MathLib.roundDouble(motion.getPitch(), 1));
			
        	if (increaseBuffer() > 2.5) {
        		fail("factor: " + MathLib.roundDouble(factor, 2) + ", adjusted: " + MathLib.roundDouble(adjusted, 4));
        		decreaseBuffer(0.25);
        	}
        } else {
        	decreaseBuffer(0.05);
        }
	}

}