package org.x3chaos.rtd;

import java.util.List;
import java.util.Map.Entry;
import java.util.Random;
import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.x3chaos.Utils;

public class RTDExecutor implements CommandExecutor {
	private final RTDPlugin main;

	private static final String FAILURE_CONSOLE = "This command is only available to players.";
	private static final String FAILURE_COOLDOWN = "You cannot roll for another %d seconds.";

	private static final String[] ERROR_SYNTAX = {
			"You would have rolled for %s, but there was an error.",
			"Contact your server owner and report a problem with RTD's config.yml.",
			"log=Check the syntax of all commands under outcome \"%s\"." };
	private static final String[] ERROR_UNKNOWN = {
			"You would have rolled for %s, but an unknown error occurred.",
			"log=Alert the author of an unknown error in processing outcome \"%s\".",
			"log=Include a Pastebin link to a full copy of config.yml." };

	Server server;
	Logger log;

	ConsoleCommandSender console;

	public RTDExecutor(RTDPlugin main) {
		this.main = main;
		this.server = main.getServer();
		this.log = main.getLogger();

		this.console = server.getConsoleSender();
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String lbl,
			String[] args) {
		// Return true with no action if the console is the sender
		if (!(sender instanceof Player)) {
			sender.sendMessage(FAILURE_CONSOLE);
			return true;
		}

		// Return false if there are any arguments
		if (args.length != 0) return false;

		Player player = (Player) sender;

		long lastRoll = main.getLastRoll(player);
		// If the last roll is defined (see main.getLastRoll(Player))
		if (lastRoll != 0) {
			lastRoll /= 1000;
			long now = main.now() / 1000;
			long since = now - lastRoll;
			long cooldown = main.getCooldown();

			// If cooldown has not passed yet
			if (since < cooldown) {
				long left = cooldown - since;
				player.sendMessage(ChatColor.RED
						+ String.format(FAILURE_COOLDOWN, left));
				return true;
			}
		}

		String typeOfCommand = "console";

		String[] errorMessage = null;
		Boolean success = true;
		String failedCommand = "";

		// Iterate through and execute each command
		Entry<String, List<String>> outcome = main.getRandomOutcome();
		String outcomeName = outcome.getKey();
		List<String> commands = outcome.getValue();
		for (int i = 0; i < commands.size(); i++) {
			String command = commands.get(i);
			command = command.replaceAll("{player}", player.getDisplayName())
					.replaceAll("{world}", player.getWorld().getName());

			if (command.contains("{rplayer}")) {
				Player[] players = main.getServer().getOnlinePlayers();
				int randomIndex = new Random().nextInt(players.length);
				String randomPlayer = players[randomIndex].getDisplayName();
				command.replaceAll("{rplayer}", randomPlayer);
			}

			if (command.startsWith("player=") || command.startsWith("console=")) {
				String[] parts = command.split("=");
				command = Utils.splitStringArray(parts, 1);
				typeOfCommand = parts[0];
			}

			if (command.startsWith("/")) command = command.substring(1);

			if (typeOfCommand.equals("player")) {

			} else success = server.dispatchCommand(console, command);

			if (!success) {
				errorMessage = ERROR_SYNTAX;
				failedCommand = command;
				break;
			}

			player.setMetadata("rtd-lastroll", new FixedMetadataValue(main,
					main.now()));
		}

		// If nothing went wrong, we're done.
		if (success) return true;

		// If something went wrong, but the error messages weren't set, an
		// unknown error occurred.
		if (errorMessage == null) errorMessage = ERROR_UNKNOWN;

		for (int i = 0; i < errorMessage.length; i++) {
			String message = errorMessage[i];
			message = String.format(message, outcomeName);
			if (message.startsWith("log=")) {
				log.severe(message.split("=")[1]);
			} else {
				player.sendMessage(ChatColor.RED + message);
			}
		}

		log.severe("Failed to execute command \"" + failedCommand + "\"");

		return true;
	}
}
