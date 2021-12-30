package com.rammelkast.veloxanticheat.checks.impl.aura;

import com.rammelkast.veloxanticheat.checks.CheckInfo;
import com.rammelkast.veloxanticheat.checks.type.MotionCheck;
import com.rammelkast.veloxanticheat.player.PlayerWrapper;
import com.rammelkast.veloxanticheat.utils.Exemption;
import com.rammelkast.veloxanticheat.utils.MathLib;
import com.rammelkast.veloxanticheat.utils.Motion;

@CheckInfo(name = "Aura", type = 'C')
public final class AuraC extends MotionCheck {

	public AuraC(final PlayerWrapper wrapper) {
		super(wrapper);
	}

	@Override
	public void process(final Motion motion) {
		if (exempt(Exemption.CONNECTING)) {
			return;
		}
		
		if (!motion.hasRotation()) {
			// No rotation -> do not check
			return;
		}
		
		final float pitch = motion.getTo().getPitch();
		final float deltaPitch = motion.getPitch();
		final float lastDeltaPitch = this.wrapper.getMotionProcessor().getPrevious().getPitch();

		final boolean cinematic = false; // TODO this.wrapper.getRotationProcessor().isCinematic();
		final boolean attacking = this.wrapper.getCombatProcessor().isInCombat();

		/**
		 * Can't have an anticheat without a GCD aura/aim check
		 */
		final long gcd = MathLib.getGcd((long) (deltaPitch * MathLib.EXPANDER), (long) (lastDeltaPitch * MathLib.EXPANDER));
		final boolean invalid = gcd < 131072L && deltaPitch > 0.0f && deltaPitch < 30.0f && pitch < 65.0f && !cinematic;
        if (attacking) {
        	if (invalid) {
        		// Verbose for debugging
        		verbose("gcd: " + gcd + ", pitch: " + pitch + ", delta: " + deltaPitch);
        		 
                if (increaseBuffer() > 7.5) {
                    fail("gcd: " + gcd);
                    decreaseBuffer(2.5);
                }
            } else {
                decreaseBuffer();
            }
        }
	}

}
