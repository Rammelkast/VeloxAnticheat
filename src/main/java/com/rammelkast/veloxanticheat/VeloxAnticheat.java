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
package com.rammelkast.veloxanticheat;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.bstats.bukkit.Metrics;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.bukkit.scheduler.BukkitTask;

import com.rammelkast.veloxanticheat.checks.CheckManager;
import com.rammelkast.veloxanticheat.command.CommandManager;
import com.rammelkast.veloxanticheat.events.PlayerEventListener;
import com.rammelkast.veloxanticheat.player.PlayerManager;
import com.rammelkast.veloxanticheat.player.PlayerWrapper;
import com.rammelkast.veloxanticheat.settings.SettingsManager;
import com.rammelkast.veloxanticheat.utils.MathLib;

public final class VeloxAnticheat extends JavaPlugin implements PluginMessageListener {

	private static VeloxAnticheat instance;

	// Executor service
	private ExecutorService executor;

	// Managers
	private SettingsManager settingsManager;
	private CheckManager checkManager;
	private PlayerManager playerManager;

	// Tick management
	private BukkitTask tickTask;
	private long second;
	private long currentSecond;
	private int ticks, totalTicks;
	private double tps;
	
	// Violation management
	private long lastViolationDecrease = MathLib.now();

	@Override
	public void onLoad() {
		VeloxAnticheat.instance = this;

		// Create executor service
		// Determine thread count based on available CPU cores/threads, max of 4
		final int threads = Math.max(Math.min(Runtime.getRuntime().availableProcessors() / 4, 4), 1);
		this.executor = Executors.newFixedThreadPool(threads);
		{
			consoleInfo("Pool size is " + threads + " threads");
		}

		this.settingsManager = new SettingsManager();
		{
			this.settingsManager.load(instance);
		}
		this.checkManager = new CheckManager();
		this.playerManager = new PlayerManager();
	}

	@Override
	public void onEnable() {
		// Enable managers
		this.settingsManager.enable(instance);
		{
			// Update configuration
			this.settingsManager.update(instance);
		}
		this.checkManager.enable(instance);
		this.playerManager.enable(instance);

		// Register events
		getServer().getPluginManager().registerEvents(new PlayerEventListener(), instance);

		// Create command manager
		new CommandManager().enable(this, "veloxanticheat");

		// Create tick task
		this.tickTask = getServer().getScheduler().runTaskTimer(this, () -> {
			this.second = (MathLib.now() / 1000L);
			this.totalTicks++;
			if (this.currentSecond == this.second) {
				this.ticks += 1;
			} else {
				this.currentSecond = this.second;
				this.tps = (this.tps == 0.0D ? this.ticks : (this.tps + this.ticks) / 2.0D);
				this.ticks = 1;
			}
			
			// Violation decrease
			final long violationTime = MathLib.now() - this.lastViolationDecrease;
			if (violationTime > this.settingsManager.getDecreaseTime() * 1000L) {
				// Decrease violations for all players
				if (getServer().getOnlinePlayers().size() > 0) {
					this.playerManager.getWrappers().forEach(wrapper -> {
						wrapper.decreaseViolations(this.settingsManager.getDecreaseFactor());
					});
					consoleInfo("Violations have been decreased");
				}
				this.lastViolationDecrease = MathLib.now();
			}
		}, 20L, 1L);

		// Listen for brands
		getServer().getMessenger().registerIncomingPluginChannel(this, "minecraft:brand", this);
		consoleInfo("Listening for client brands");

		// Enable Metrics
		new Metrics(this, 13773);

		consoleInfo("Startup complete");
	}

	@Override
	public void onDisable() {
		// Stop tick task
		this.tickTask.cancel();

		// Shut down executor service
		this.executor.shutdown();

		// Disable managers
		this.playerManager.disable();
		this.checkManager.disable();
		this.settingsManager.disable();

		// Nullify objects
		instance = null;
		this.executor = null;
		this.checkManager = null;

		consoleInfo("Shutdown complete");
	}

	@Override
	public void onPluginMessageReceived(final String channel, final Player player, final byte[] data) {
		final PlayerWrapper wrapper = this.playerManager.fetch(player);
		if (wrapper == null) {
			return;
		}

		// Brand is appended with a weird character which we have to strip
		final String brand = new String(data, StandardCharsets.UTF_8).substring(1);
		wrapper.setBrand(brand);
	}

	/**
	 * Sends an informational message to the console
	 * 
	 * @param message the message to send
	 */
	public void consoleInfo(final String message) {
		getServer().getConsoleSender().sendMessage(
				ChatColor.RED + "" + ChatColor.BOLD + "VAC " + ChatColor.DARK_GRAY + "> " + ChatColor.GRAY + message);
	}

	/**
	 * Sends a warning message to the console
	 * 
	 * @param message the message to send
	 */
	public void consoleWarning(final String message) {
		getServer().getConsoleSender().sendMessage(
				ChatColor.RED + "" + ChatColor.BOLD + "VAC " + ChatColor.DARK_GRAY + "> " + ChatColor.YELLOW + message);
	}

	/**
	 * Sends an error message to the console
	 * 
	 * @param message the message to send
	 */
	public void consoleError(final String message) {
		getServer().getConsoleSender().sendMessage(
				ChatColor.RED + "" + ChatColor.BOLD + "VAC " + ChatColor.DARK_GRAY + "> " + ChatColor.RED + message);
	}

	/**
	 * Gets the executor service
	 * 
	 * @return the {@link ExecutorService} for this instance
	 */
	public ExecutorService getExecutor() {
		return this.executor;
	}

	/**
	 * Gets the settings manager
	 * 
	 * @return the {@link SettingsManager} for this instance
	 */
	public SettingsManager getSettingsManager() {
		return this.settingsManager;
	}
	
	/**
	 * Gets the check manager
	 * 
	 * @return the {@link CheckManager} for this instance
	 */
	public CheckManager getCheckManager() {
		return this.checkManager;
	}

	/*
	 * Gets the player manager
	 * 
	 * @return the {@link PlayerManager} for this instance
	 */
	public PlayerManager getPlayerManager() {
		return this.playerManager;
	}

	/**
	 * Gets the amount of ticks passed since startup
	 * 
	 * @return the amount of ticks passed
	 */
	public int getTicks() {
		return this.totalTicks;
	}

	/**
	 * Gets the amount of ticks per second
	 * 
	 * @return the amount of ticks per second
	 */
	public double getTPS() {
		return Math.max(Math.min(this.tps, 20.0), 0.0);
	}

	/**
	 * Gets the version of this OpenAntiCheat instance
	 * 
	 * @return the plugin version
	 */
	public static String getVersion() {
		return instance.getDescription().getVersion();
	}

	/**
	 * Gets the current {@link VeloxAnticheat} instance
	 * 
	 * @return the {@link VeloxAnticheat} instance
	 */
	public static VeloxAnticheat getInstance() {
		return instance;
	}

}
