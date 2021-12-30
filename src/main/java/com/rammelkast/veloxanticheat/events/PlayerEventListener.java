package com.rammelkast.veloxanticheat.events;

import java.util.concurrent.Callable;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerVelocityEvent;

import com.rammelkast.veloxanticheat.VeloxAnticheat;
import com.rammelkast.veloxanticheat.player.PlayerManager;
import com.rammelkast.veloxanticheat.player.PlayerWrapper;
import com.rammelkast.veloxanticheat.utils.MathLib;

public final class PlayerEventListener implements Listener {

	private static final PlayerManager PLAYER_MANAGER = VeloxAnticheat.getInstance().getPlayerManager();
	
	@EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(final PlayerJoinEvent event) {
		final Player player = event.getPlayer();
		try {
			final PlayerWrapper wrapper = VeloxAnticheat.getInstance().getExecutor().submit(new Callable<PlayerWrapper>() {
				@Override
				public PlayerWrapper call() throws Exception {
					// Register player
					return PLAYER_MANAGER.register(player);
				}
			}).get();
			
			// Set connection timestamp
			wrapper.setConnectionTime(MathLib.now());
		} catch (final Exception exception) {
			VeloxAnticheat.getInstance().consoleError("Failed to create player wrapper async: " + exception.getCause());
			exception.printStackTrace();
			return;
		} finally {
			// Force sprint resync
			player.setSprinting(true);
			player.setSprinting(false);
		}
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(final PlayerQuitEvent event) {
		final Player player = event.getPlayer();
		try {
			final boolean registered = VeloxAnticheat.getInstance().getExecutor().submit(new Callable<Boolean>() {
				@Override
				public Boolean call() throws Exception {
					// Destroy player wrapper
					return PLAYER_MANAGER.destroy(player);
				}
			}).get();
			
			if (!registered) {
				// This shouldn't have happened since we track everyone, maybe log?
			}
		} catch (final Exception exception) {
			VeloxAnticheat.getInstance().consoleError("Failed to destroy player wrapper async: " + exception.getCause());
			exception.printStackTrace();
			return;
		}
	}
	
	@EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerMove(final PlayerMoveEvent event) {
		final Player player = event.getPlayer();
		
		// Fetch wrapper
		final PlayerWrapper wrapper = PLAYER_MANAGER.fetch(player);
		if (wrapper == null) {
			return;
		}
		
		wrapper.getMotionProcessor().process(wrapper, event);
	}
	
	@EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerCombat(final EntityDamageByEntityEvent event) {
		if (!(event.getDamager() instanceof Player)) {
			return;
		}
		
		final Player player = (Player) event.getDamager();
		
		// Fetch wrapper
		final PlayerWrapper wrapper = PLAYER_MANAGER.fetch(player);
		if (wrapper == null) {
			return;
		}
		
		wrapper.getCombatProcessor().process(wrapper, event);
	}
	
	@EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerVelocity(final PlayerVelocityEvent event) {
		final Player player = event.getPlayer();
		
		// Fetch wrapper
		final PlayerWrapper wrapper = PLAYER_MANAGER.fetch(player);
		if (wrapper == null) {
			return;
		}
		
		wrapper.getVelocityProcessor().process(wrapper, event);
	}
	
}
