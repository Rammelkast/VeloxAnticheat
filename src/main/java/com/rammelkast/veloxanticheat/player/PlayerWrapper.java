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
package com.rammelkast.veloxanticheat.player;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;
import org.bukkit.util.NumberConversions;

import com.google.common.collect.ClassToInstanceMap;
import com.rammelkast.veloxanticheat.VeloxAnticheat;
import com.rammelkast.veloxanticheat.checks.Check;
import com.rammelkast.veloxanticheat.checks.CheckManager;
import com.rammelkast.veloxanticheat.events.impl.VeloxFlagEvent;
import com.rammelkast.veloxanticheat.player.processor.CombatProcessor;
import com.rammelkast.veloxanticheat.player.processor.MotionProcessor;
import com.rammelkast.veloxanticheat.player.processor.VelocityProcessor;
import com.rammelkast.veloxanticheat.utils.EvictingList;
import com.rammelkast.veloxanticheat.utils.MathLib;
import com.rammelkast.veloxanticheat.utils.TimedLocation;

import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;

public final class PlayerWrapper {

	private static final CheckManager CHECK_MANAGER = VeloxAnticheat.getInstance().getCheckManager();

	private final Player player;
	private final List<TimedLocation> locations = new EvictingList<>(20);

	// Processors
	private final MotionProcessor motionProcessor = new MotionProcessor();
	private final CombatProcessor combatProcessor = new CombatProcessor();
	private final VelocityProcessor velocityProcessor = new VelocityProcessor();

	// Checks map
	private final ClassToInstanceMap<Check<?>> checks;

	// Violation map
	private final Map<String, Float> violationMap = new HashMap<String, Float>();

	private long connectionTime = MathLib.now();
	private String brand = "unknown";

	/**
	 * Creates a player wrapper
	 * 
	 * @param player The Bukkit player object
	 */
	public PlayerWrapper(final Player player) {
		this.player = player;
		this.checks = CHECK_MANAGER.createChecks(this);
		// Populate violation map
		getChecks().stream().filter(check -> !this.violationMap.containsKey(check.getName().toLowerCase()))
				.forEach(check -> this.violationMap.put(check.getName().toLowerCase(), 0.0f));
	}

	/**
	 * Registers a violation for the given check
	 * 
	 * @param check The check
	 */
	public void registerViolation(final Check<?> check) {
		final String name = check.getName().toLowerCase();
		if (!this.violationMap.containsKey(name)) {
			throw new RuntimeException("Invalid check '" + name + "' for violation registration!");
		}

		final float currentValue = this.violationMap.get(name);
		// Add weight for new value
		final float weight = (float) VeloxAnticheat.getInstance().getSettingsManager()
				.getDouble("checks." + name + ".type" + check.getType() + ".weight");
		final float newValue = currentValue + weight;
		// Store new violation level
		this.violationMap.put(name, MathLib.roundFloat(newValue, 2));

		// Create and call event
		final VeloxFlagEvent event = new VeloxFlagEvent(check, newValue);
		Bukkit.getServer().getPluginManager().callEvent(event);

		final int oldFloored = NumberConversions.floor(currentValue);
		final int newFloored = NumberConversions.floor(newValue);
		final int notify = VeloxAnticheat.getInstance().getSettingsManager().getInt("checks." + name + ".notify-vl");
		final int kick = VeloxAnticheat.getInstance().getSettingsManager().getInt("checks." + name + ".kick-vl");
		if (oldFloored < notify && newFloored >= notify) {
			// Send to all players with the alert permissions
			// TODO this code is an ugly incomprehensible mess
			Bukkit.getServer().getOnlinePlayers().forEach(player -> {
				if (player.hasPermission("velox.alert")) {
					final TextComponent component = new TextComponent(ChatColor.RED + "" + ChatColor.BOLD + "VAC "
							+ ChatColor.DARK_GRAY + "> " + ChatColor.YELLOW + getPlayer().getName() + ChatColor.GRAY
							+ " failed " + ChatColor.RED + check.getName() + ChatColor.GRAY + " (" + check.getType()
							+ ") " + ChatColor.YELLOW + "[" + check.getViolatons() + "x]" + ChatColor.DARK_GRAY + " | "
							+ ChatColor.GRAY + "ping: " + getPlayer().getPing() + "ms, tps: "
							+ MathLib.roundDouble(VeloxAnticheat.getInstance().getTPS(), 1));
					final float confidence = MathLib.roundFloat((newFloored / (float) check.getViolatons()), 1);
					final String confidenceString = ((confidence > 2.0f) ? (ChatColor.RED + "very high")
							: ((confidence > 1.0f) ? (ChatColor.RED + "high") : (ChatColor.YELLOW + "medium")));
					final ChatColor level = ((newValue > 10.0f) ? ChatColor.RED
							: (newValue > 5.0f ? ChatColor.YELLOW : ChatColor.WHITE));
					component.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
							new Text(ChatColor.GRAY + "Total violation level for " + ChatColor.YELLOW + check.getName()
									+ ChatColor.GRAY + " is " + level + newValue),
							new Text("\n" + ChatColor.GRAY + "Check confidence is " + confidenceString),
							new Text("\n\n" + ChatColor.YELLOW + getPlayer().getName() + ChatColor.GRAY
									+ " has a ping of " + ChatColor.YELLOW + getPlayer().getPing() + "ms")));
					player.spigot().sendMessage(component);
				}
			});
		} else if (oldFloored < kick && newFloored >= kick) {
			getPlayer().kickPlayer(ChatColor.DARK_GRAY + "« " + ChatColor.RED + "" + ChatColor.BOLD + "Velox Anticheat "
					+ ChatColor.DARK_GRAY + "»\n\n" + ChatColor.GRAY + "You have been removed from the server:\n"
					+ ChatColor.WHITE + "Unfair advantage " + ChatColor.YELLOW + "[" + check.getName() + "]");
		}
	}

	/**
	 * Decreases all violation levels by the given factor
	 * 
	 * @param factor The factor
	 */
	public void decreaseViolations(final double factor) {
		getChecks().stream().forEach(check -> check.decreaseViolations(factor));
		this.violationMap.forEach((name, level) -> {
			this.violationMap.put(name, MathLib.roundFloat((float) (level * factor), 2));
		});
	}

	/**
	 * Gets the Bukkit {@link Player} object for this wrapper
	 * 
	 * @return the player object
	 */
	public Player getPlayer() {
		return this.player;
	}

	/**
	 * Gets a collection of {@link Check} instances for this wrapper
	 * 
	 * @return the wrapper's checks
	 */
	public Collection<Check<?>> getChecks() {
		return this.checks.values();
	}

	/**
	 * Gets the timestamp when this player connected
	 * 
	 * @return the connection timestamp
	 */
	public long getConnectionTime() {
		return connectionTime;
	}

	/**
	 * Sets the timestamp when this player connected
	 * 
	 * @param connectionTime The connection timestamp
	 */
	public void setConnectionTime(final long connectionTime) {
		this.connectionTime = connectionTime;
	}

	/**
	 * Gets the motion processor
	 * 
	 * @return the motion processor
	 */
	public MotionProcessor getMotionProcessor() {
		return this.motionProcessor;
	}

	/**
	 * Gets the combat processor
	 * 
	 * @return the combat processor
	 */
	public CombatProcessor getCombatProcessor() {
		return this.combatProcessor;
	}

	/**
	 * Gets the velocity processor
	 * 
	 * @return the velocity processor
	 */
	public VelocityProcessor getVelocityProcessor() {
		return this.velocityProcessor;
	}

	/**
	 * Gets the allowed player movement speed through the "GENERIC_MOVEMENT_SPEED"
	 * attribute
	 * 
	 * @return the allowed player movement speed
	 */
	public double getMovementSpeed() {
		final AttributeInstance attribute = this.player.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED);
		return attribute.getValue();
	}

	/**
	 * Get the players history of {@link TimedLocation}s
	 * 
	 * @return the list of timed locations
	 */
	public List<TimedLocation> getLocations() {
		return this.locations;
	}

	/**
	 * Updates the client brand
	 * 
	 * @param brand The client brand
	 */
	public void setBrand(final String brand) {
		this.brand = brand;
	}

	/**
	 * Gets the client brand
	 * 
	 * @return the client brand
	 */
	public String getBrand() {
		return this.brand;
	}

	/**
	 * Gets the violation map
	 * 
	 * @return the violation map
	 */
	public Map<String, Float> getViolationMap() {
		return this.violationMap;
	}

}
