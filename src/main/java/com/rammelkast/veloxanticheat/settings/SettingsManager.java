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
package com.rammelkast.veloxanticheat.settings;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;

import com.google.common.collect.ClassToInstanceMap;
import com.rammelkast.veloxanticheat.VeloxAnticheat;
import com.rammelkast.veloxanticheat.checks.Check;
import com.rammelkast.veloxanticheat.settings.Setting.SettingType;

public final class SettingsManager {

	private final List<String> disabledChecks = new ArrayList<>();
	
	private FileConfiguration configuration;
	
	// General settings
	private final Setting testMode = new Setting("test-mode", SettingType.BOOLEAN);
	private final Setting connectTime = new Setting("connect-time", SettingType.INTEGER);
	private final Setting velocityTime = new Setting("velocity-time", SettingType.INTEGER);
	private final Setting rubberbandTicks = new Setting("rubberband-ticks", SettingType.INTEGER);
	private final Setting lagExemptTps = new Setting("lag-exempt-tps", SettingType.DOUBLE);
	
	// Violation settings
	private final Setting decreaseTime = new Setting("decrease-time", SettingType.LONG);
	private final Setting decreaseFactor = new Setting("decrease-factor", SettingType.DOUBLE);
	
	public void load(final Plugin plugin) {
		// Create data folder
		plugin.getDataFolder().mkdirs();
	}
	
	/**
	 * Enables the settings manager
	 * @param instance the {@link OpenAntiCheat} instance
	 */
	public void enable(final Plugin plugin) {
		// Save default config
		plugin.saveDefaultConfig();
		
		// Load config
		this.configuration = plugin.getConfig();
	}
	
	public void update(final VeloxAnticheat velox) {
		final ClassToInstanceMap<Check<?>> checks = velox.getCheckManager().createChecks(null);
		try {
			// General settings
			this.testMode.setValue(getBoolean("general.test-mode"));
			this.connectTime.setValue(getInt("general.connect-time"));
			this.velocityTime.setValue(getInt("general.velocity-time"));
			this.rubberbandTicks.setValue(getInt("general.rubberband-ticks"));
			this.lagExemptTps.setValue(getDouble("general.lag-exempt-tps"));
			
			// Violation settings
			this.decreaseTime.setValue(getLong("violation.decrease-time"));
			this.decreaseFactor.setValue(getDouble("violation.decrease-factor"));
			
			// Check related settings
			for (final Check<?> check : checks.values()) {
				final String name = check.getName();
				final char type = check.getType();
				
				// Set values for settings
				for (final Field field : check.getClass().getDeclaredFields()) {
					if (field.getType().equals(Setting.class)) {
						final boolean accessible = field.canAccess(null);
						field.setAccessible(true);
						{
							final Setting setting = (Setting) field.get(null);
							switch (setting.getType()) {
							case BOOLEAN:
								setting.setValue(getBoolean(
										"checks." + name.toLowerCase() + ".type" + type + "." + setting.getName()));
								break;
							case DOUBLE:
								setting.setValue(getDouble(
										"checks." + name.toLowerCase() + ".type" + type + "." + setting.getName()));
								break;
							case INTEGER:
								setting.setValue(getInt(
										"checks." + name.toLowerCase() + ".type" + type + "." + setting.getName()));
								break;
							case LONG:
								setting.setValue(getLong(
										"checks." + name.toLowerCase() + ".type" + type + "." + setting.getName()));
								break;
							case STRING:
								setting.setValue(getString(
										"checks." + name.toLowerCase() + ".type" + type + "." + setting.getName()));
								break;
							default:
								velox.consoleWarning("Invalid setting type " + setting.getType().name() + " for check "
										+ name + type);
								break;
							}
						}
						field.setAccessible(accessible);
					}
				}
				
				// Confirm if check is enabled or disabled
				final boolean enabled = getBoolean("checks." + name.toLowerCase() + ".type" + type + ".enabled");
				if (!enabled) {
					this.disabledChecks.add(name + type);
				}
			}
		} catch (final Exception exception) {
			velox.consoleError("Failed to update configuration");
			exception.printStackTrace();
		}
	}
	
	public boolean getBoolean(final String path) {
		return this.configuration.getBoolean(path);
	}
	
	public double getDouble(final String path) {
		return this.configuration.getDouble(path);
	}
	
	public int getInt(final String path) {
		return this.configuration.getInt(path);
	}
	
	public long getLong(final String path) {
		return this.configuration.getLong(path);
	}
	
	public String getString(final String path) {
		return this.configuration.getString(path);
	}
	
	/**
	 * Gets if a check is enabled
	 * 
	 * @param check The check
	 * @return true if enabled
	 */
	public boolean isCheckEnabled(final Check<?> check) {
		return !this.disabledChecks.contains(check.getName() + check.getType());
	}
	
	/**
	 * Gets if test mode is enabled
	 * 
	 * @return true if test mode enabled
	 */
	public boolean isTestMode() {
		return this.testMode.getBoolean();
	}
	
	/**
	 * Gets the amount of ticks to account for connecting
	 * 
	 * @return the amount of connecting ticks
	 */
	public int getConnectTime() {
		return this.connectTime.getInt();
	}
	
	/**
	 * Gets the amount of ticks to account for velocity
	 * 
	 * @return the amount of velocity ticks
	 */
	public int getVelocityTime() {
		return this.velocityTime.getInt();
	}
	
	/**
	 * Gets the amount of ticks to rubberband a player
	 * 
	 * @return the amount of rubberband ticks
	 */
	public int getRubberbandTicks() {
		return this.rubberbandTicks.getInt();
	}

	/**
	 * Gets the maximum TPS value that gives a server lag exemption
	 * 
	 * @return the server lag exemption TPS value
	 */
	public double getLagExemptTPS() {
		return this.lagExemptTps.getDouble();
	}
	
	/**
	 * Gets how often should a violation decrease be applied
	 * 
	 * @return how often violations should be decreased
	 */
	public long getDecreaseTime() {
		return this.decreaseTime.getLong();
	}
	
	/**
	 * Gets the multiplier which is applied to violations
	 * 
	 * @return the violation multiplier
	 */
	public double getDecreaseFactor() {
		return this.decreaseFactor.getDouble();
	}
	
	/**
	 * Disables the settings manager
	 */
	public void disable() {
		
	}
	
}
