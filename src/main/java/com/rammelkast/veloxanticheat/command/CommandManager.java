package com.rammelkast.veloxanticheat.command;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import com.rammelkast.veloxanticheat.VeloxAnticheat;
import com.rammelkast.veloxanticheat.checks.Check;
import com.rammelkast.veloxanticheat.player.PlayerWrapper;
import com.rammelkast.veloxanticheat.utils.MathLib;

public final class CommandManager implements CommandExecutor {
	
    private final Map<String, BiConsumer<CommandSender, String[]>> commands = new HashMap<>();

    public void enable(final Plugin plugin, final String command) {
    	// Create subcommands
		register("", (sender, params) -> {
			sender.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "VAC " + ChatColor.DARK_GRAY + "> " + ChatColor.GRAY
					+ "Powered by " + ChatColor.RED + "Velox Anticheat" + ChatColor.GRAY + " version " + ChatColor.YELLOW + VeloxAnticheat.getVersion());
		});
		
		register("help", (sender, params) -> {
			if (!sender.hasPermission("velox.help")) {
				sender.sendMessage(ChatColor.RED + "You're not allowed to use this command.");
				return;
			}
			
			sender.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "VAC " + ChatColor.DARK_GRAY + "> " + ChatColor.GRAY
					+ "List of commands");
			sender.sendMessage(ChatColor.YELLOW + "/vac help" + ChatColor.DARK_GRAY + " > " + ChatColor.WHITE
					+ "Gives a list of usable commands");
			sender.sendMessage(ChatColor.YELLOW + "/vac report [player]" + ChatColor.DARK_GRAY + " > " + ChatColor.WHITE
					+ "Get a detailed report for the given player");
			sender.sendMessage(ChatColor.YELLOW + "/vac verbose [check]" + ChatColor.DARK_GRAY + " > " + ChatColor.WHITE
					+ "Receive verbose logging for the given check");
		});
		
		register("report", (sender, params) -> {
			if (!sender.hasPermission("velox.report")) {
				sender.sendMessage(ChatColor.RED + "You're not allowed to use this command.");
				return;
			}
			
			if (params.length != 1) {
				sender.sendMessage(ChatColor.RED + "Correct usage: /vac report [name]");
				return;
			}
			
			final Player player = Bukkit.getServer().getPlayer(params[0]);
			if (player == null || !player.isOnline()) {
				sender.sendMessage(ChatColor.RED + "Player " + params[0] + " is not online.");
				return;
			}
			
			final PlayerWrapper wrapper = VeloxAnticheat.getInstance().getPlayerManager().fetch(player);
			sender.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "VAC " + ChatColor.DARK_GRAY + "> "
					+ ChatColor.GRAY + "Report for " + ChatColor.WHITE + sender.getName());
			sender.sendMessage(ChatColor.YELLOW + "Joined" + ChatColor.DARK_GRAY + " > " + ChatColor.WHITE
					+ ((MathLib.now() - wrapper.getConnectionTime()) / (60 * 1000L)) + "m ago");
			sender.sendMessage(ChatColor.YELLOW + "Client " + ChatColor.DARK_GRAY + " > " + ChatColor.WHITE
					+ wrapper.getBrand());
			sender.sendMessage(ChatColor.YELLOW + "Ping   " + ChatColor.DARK_GRAY + " > " + ChatColor.WHITE
					+ wrapper.getPlayer().getPing() + "ms");
			
			// Create levels string
			String levels = "";
			for (final String check : wrapper.getViolationMap().keySet()) {
				final float value = wrapper.getViolationMap().get(check);
				final ChatColor color = ((value > 10.0f) ? ChatColor.RED : (value > 5.0f ? ChatColor.YELLOW : ChatColor.WHITE));
				levels += (check.substring(0, 1).toUpperCase() + check.substring(1) + color + " ("
						+ MathLib.roundFloat(wrapper.getViolationMap().get(check), 1) + ")" + ChatColor.WHITE + ", ");
			}
			
			sender.sendMessage(ChatColor.YELLOW + "Levels" + ChatColor.DARK_GRAY + " > " + ChatColor.WHITE
					+ levels.substring(0, levels.length() - 2));
		});
		
		register("verbose", (sender, params) -> {
			if (!sender.hasPermission("velox.verbose")) {
				sender.sendMessage(ChatColor.RED + "You're not allowed to use this command.");
				return;
			}
			
			if (!(sender instanceof Player)) {
				sender.sendMessage(ChatColor.RED + "This command is only for players.");
				return;
			}
			
			if (params.length != 1) {
				sender.sendMessage(ChatColor.RED + "Correct usage: /vac verbose [check]");
				return;
			}

			final PlayerWrapper wrapper = VeloxAnticheat.getInstance().getPlayerManager().fetch((Player) sender);
			final Check<?> check = wrapper.getChecks().stream()
					.filter(filter -> (filter.getName() + filter.getType()).equalsIgnoreCase(params[0])).findAny()
					.orElseGet(null);
			if (check == null) {
				sender.sendMessage(ChatColor.RED + "Could not find check '" + params[0] + "'");
				return;
			}
			
			final boolean state = check.toggleVerbose();
			sender.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "VAC " + ChatColor.DARK_GRAY + "> "
					+ ChatColor.GRAY + "Toggled verbose " + (state ? (ChatColor.GREEN + "on") : (ChatColor.RED + "off"))
					+ ChatColor.GRAY + " for " + check.getName() + " (" + check.getType() + ")");
		});

    	// Set manager as executor for command
    	plugin.getServer().getPluginCommand(command).setExecutor(this);
    }

    public void register(final String command, final BiConsumer<CommandSender, String[]> event) {
    	this.commands.put(command.toLowerCase(), event);
    }

	@Override
	public boolean onCommand(final CommandSender sender, final Command command, final String label,
			final String[] args) {
		if (args.length == 0 && this.commands.containsKey("")) {
			this.commands.get("").accept(sender, args);
			return true;
		}

		final String fullExecution = String.join(" ", args).toLowerCase();
		final Optional<Map.Entry<String, BiConsumer<CommandSender, String[]>>> match = this.commands.entrySet().stream()
				.filter(entry -> !entry.getKey().equals("")).filter(entry -> fullExecution.startsWith(entry.getKey()))
				.findAny();
		if (match.isPresent()) {
			final String[] param = args.length == 0 ? new String[0]
					: Arrays.copyOfRange(args, match.get().getKey().split(" ").length, args.length);
			match.get().getValue().accept(sender, param);
		} else {
			sender.sendMessage(ChatColor.RED + "Unknown command.");
		}
		return true;
	}

}