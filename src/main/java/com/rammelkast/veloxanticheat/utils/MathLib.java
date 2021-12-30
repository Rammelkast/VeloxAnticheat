/**
 * Velox Anticheat | Simple, stable and accurate anticheat
 * Copyright (C) 2021-2022 Marco Moesman ("Rammelkast")
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.rammelkast.veloxanticheat.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 * Library for mathematical functions
 */
public final class MathLib {

	public static final double EXPANDER = Math.pow(2, 24);
	
	/**
	 * Gets the current time in milliseconds
	 * 
	 * @return the current time
	 */
	public static long now() {
		return System.currentTimeMillis();
	}

	/**
	 * Rounds a float value to a scale
	 * 
	 * @param value Value to round
	 * @param scale Scale
	 * @return rounded value
	 */
	public static float roundFloat(final float value, final int scale) {
		return new BigDecimal(value).setScale(scale, RoundingMode.HALF_UP).floatValue();
	}

	/**
	 * Rounds a double value to a scale
	 * 
	 * @param value Value to round
	 * @param scale Scale
	 * @return rounded value
	 */
	public static double roundDouble(final double value, final int scale) {
		return new BigDecimal(value).setScale(scale, RoundingMode.HALF_UP).doubleValue();
	}

	/**
	 * Wraps a degree measure to 180 degrees
	 * 
	 * @param value The input angle value
	 * @return The wrapped angle value
	 */
	public static float getAngleWrapped(float angle) {
		angle %= 360F;

		if (angle >= 180.0F) {
			angle -= 360.0F;
		}

		if (angle < -180.0F) {
			angle += 360.0F;
		}
		return angle;
	}
	
	/**
	 * Calculates if a Y position should be on ground
	 * 
	 * @param positionY the Y position of the player
	 * @return if the player is on ground mathematically
	 */
	public static boolean computeGrounded(final double positionY) {
		return positionY % 0.015625 == 0;
	}

	/**
    * @param current - The current value
    * @param previous - The previous value
    * @return - The GCD of those two values
    */
   public static long getGcd(final long current, final long previous) {
       return (previous <= 16384L) ? current : getGcd(previous, current % previous);
   }

	/**
    * @param A - The A value
    * @param B - The B value
    * @return - The GCD of those two values
    */
   public static double getGcd(final double a, final double b) {
       if (a < b) {
           return getGcd(b, a);
       }

       if (Math.abs(b) < 0.001) {
           return a;
       } else {
           return getGcd(b, a - Math.floor(a / b) * b);
       }
   }
   
	/**
	 * Gets the level of the given potion effect for the player
	 * 
	 * @param player The player
	 * @param type   The potion type
	 * @return the potion level
	 */
	@SuppressWarnings("deprecation")
	public static int getPotionLevel(final Player player, final PotionEffectType type) {
		if (!player.hasPotionEffect(type)) {
			return 0;
		}

		return player.getActivePotionEffects().stream().filter(potionEffect -> potionEffect.getType().getId() == type.getId())
				.map(PotionEffect::getAmplifier).findAny().orElse(0) + 1;
	}


}
