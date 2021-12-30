package com.rammelkast.veloxanticheat.player;

import java.util.Collection;
import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public final class PlayerManager {

	private final ConcurrentHashMap<UUID, PlayerWrapper> playerMap = new ConcurrentHashMap<>();

	public void enable(final Plugin plugin) {
		// Register all currently online players
		plugin.getServer().getOnlinePlayers().forEach(this::register);
	}

	public void disable() {
		this.playerMap.clear();
	}

	/**
	 * Creates and registers a new {@link PlayerWrapper} for this player
	 * 
	 * @param player The player
	 * @return the newly created player wrapper object
	 */
	public PlayerWrapper register(final Player player) {
		final PlayerWrapper wrapper = new PlayerWrapper(player);
		this.playerMap.put(player.getUniqueId(), wrapper);
		return wrapper;
	}

	/**
	 * Destroys the wrapper object for this player
	 * 
	 * @param player The player
	 * @return true if the player was registered
	 */
	public boolean destroy(final Player player) {
		return this.playerMap.remove(player.getUniqueId()) != null;
	}

	/**
	 * Gets if we have a wrapper object for this player
	 * 
	 * @param player The player
	 * @return true if the player is registered
	 */
	public boolean has(final Player player) {
		return this.playerMap.containsKey(player.getUniqueId());
	}

	/**
	 * Gets the {@link PlayerWrapper} for this player
	 * 
	 * @param player The player
	 * @return the player wrapper object
	 */
	public PlayerWrapper fetch(final Player player) {
		return this.playerMap.get(player.getUniqueId());
	}

	/**
	 * Gets a collection of all {@link PlayerWrapper} objects registered
	 * 
	 * @return a collection of player wrappers
	 */
	public Collection<PlayerWrapper> getWrappers() {
		return Collections.unmodifiableCollection(this.playerMap.values());
	}

}
