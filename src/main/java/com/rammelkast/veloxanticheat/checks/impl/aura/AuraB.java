package com.rammelkast.veloxanticheat.checks.impl.aura;

import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.Vector;

import com.rammelkast.veloxanticheat.checks.CheckInfo;
import com.rammelkast.veloxanticheat.checks.type.CombatCheck;
import com.rammelkast.veloxanticheat.player.PlayerWrapper;
import com.rammelkast.veloxanticheat.utils.Exemption;
import com.rammelkast.veloxanticheat.utils.MathLib;

/**
 * TODO better player/server lag adjustments
 */
@CheckInfo(name = "Aura", type = 'B')
public final class AuraB extends CombatCheck {

	private static final double MAX_ANGLE = 0.6;
	
	public AuraB(final PlayerWrapper wrapper) {
		super(wrapper);
	}

	@Override
	public void process(final LivingEntity target) {
		if (exempt(Exemption.CONNECTING, Exemption.SERVER_LAG, Exemption.BAD_CONNECTION)) {
			resetBuffer();
			return;
		}
		
		final Location location = this.wrapper.getPlayer().getLocation();
		final double x = location.getX();
		final double z = location.getZ();
		final Vector origin = new Vector(x, 0.0, z);
		final Vector targetVector = target.getLocation().toVector().clone().setY(0.0);
		if (origin.distanceSquared(targetVector) < 1.8) {
			// Do not run check when in close proximity
			decreaseBuffer(0.5);
			return;
		}
		
		/**
		 * Checks attack angle and flags if larger than MAX_ANGLE
		 */
		final Vector destinationVector = targetVector.clone().subtract(origin);
		final Vector playerVector = this.wrapper.getPlayer().getEyeLocation().getDirection().clone().setY(0.0);
		final double angle = destinationVector.angle(playerVector);
		if (angle > MAX_ANGLE) {
			// Verbose for debugging
			verbose("angle: " + MathLib.roundDouble(angle, 1));
			
			if (increaseBuffer() > 4) {
				fail("angle: " + MathLib.roundDouble(angle, 1));
				this.buffer /= 2;
			}
		} else {
			decreaseBuffer(0.25);
		}
	}
	
}
