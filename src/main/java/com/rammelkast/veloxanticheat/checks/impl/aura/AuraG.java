package com.rammelkast.veloxanticheat.checks.impl.aura;

import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.Vector;

import com.rammelkast.veloxanticheat.VeloxAnticheat;
import com.rammelkast.veloxanticheat.checks.CheckInfo;
import com.rammelkast.veloxanticheat.checks.type.CombatCheck;
import com.rammelkast.veloxanticheat.player.PlayerWrapper;
import com.rammelkast.veloxanticheat.utils.Exemption;
import com.rammelkast.veloxanticheat.utils.MathLib;

/**
 * Experimental
 */
@CheckInfo(name = "Aura", type = 'G')
public final class AuraG extends CombatCheck {

	public AuraG(final PlayerWrapper wrapper) {
		super(wrapper);
	}

	@Override
	public void process(final LivingEntity target) {
		if (exempt(Exemption.CONNECTING, Exemption.GLIDING, Exemption.CREATIVE, Exemption.SERVER_LAG)) {
			resetBuffer();
			return;
		}

		final LivingEntity lastTarget = this.wrapper.getCombatProcessor().getPrevious();
		if (lastTarget == null || target.equals(lastTarget)) {
			// Only check when fast swapping targets
			return;
		}

		final double targetVelocity = Math.hypot(target.getVelocity().getX(), target.getVelocity().getZ());
		final double lastTargetVelocity = Math.hypot(lastTarget.getVelocity().getX(), lastTarget.getVelocity().getZ());
		if (targetVelocity > 0.4 || lastTargetVelocity > 0.4) {
			// Velocity too high to reliably run this check
			decreaseBuffer();
			return;
		}
		
		final Location location = this.wrapper.getPlayer().getLocation();
		final double x = location.getX();
		final double z = location.getZ();
		final Vector origin = new Vector(x, 0.0, z);
		final Vector targetVector = target.getLocation().toVector().clone().setY(0.0);
		final Vector lastTargetVector = lastTarget.getLocation().toVector().clone().setY(0.0);
		final Vector destinationVector = targetVector.clone().subtract(origin);
		final Vector lastDestinationVector = lastTargetVector.clone().subtract(origin);
		final double angle = destinationVector.angle(lastDestinationVector);
		final double distance = destinationVector.distance(lastDestinationVector);
		final int time = VeloxAnticheat.getInstance().getTicks() - this.wrapper.getCombatProcessor().getLastHit();
		if (time < 10 && distance > 6.0 && angle > 2.0) {
			// Verbose for debugging
			verbose("angle: " + angle + ", distance: " + distance + ", time: " + time);
			
			if (increaseBuffer() > 3) {
				fail("angle: " + MathLib.roundDouble(angle, 1) + ", distance: " + MathLib.roundDouble(distance, 1) + ", time: " + time);
				decreaseBuffer(0.5);
			}
		} else {
			decreaseBuffer(0.05);
		}
	}

}