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
import org.bukkit.entity.Player;
import org.x3chaos.Utils;

public class RTDExecutor implements CommandExecutor {
	private final RTDPlugin main;

	/* Failure messages */
	private static final String FAILURE_COOLDOWN = "You cannot roll for another %d seconds.";

	/* Error messages */
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

	public RTDExecutor(RTDPlugin main) {
		this.main = main;
		this.server = main.getServer();
		this.log = main.getLogger();
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String lbl,
			String[] args) {
		// Get the outcome before anything else
		Entry<String, List<String>> outcome = main.getRandomOutcome();
		String outcomeName = outcome.getKey();

		// Echo outcome name if console sender
		if (!(sender instanceof Player)) {
			sender.sendMessage("Outcome: " + outcomeName);
			return true;
		}

		// Return false if there are any arguments
		if (args.length != 0) return false;

		/* Execute if above checks have been passed */
		Player player = (Player) sender;
		CommandSender console = server.getConsoleSender();

		String typeOfCommand = "console";
		String[] errorMessage = null;
		Boolean success = true;
		String failedCommand = "";

		// Don't roll if still cooling down
		long lastRoll = main.getLastRoll(player);
		if (lastRoll != 0) {
			lastRoll /= 1000;

			if (isCoolingDown(lastRoll)) {
				long left = getTimeLeft(lastRoll);
				String message = String.format(FAILURE_COOLDOWN, left);
				player.sendMessage(ChatColor.RED + message);
				return true;
			}
		}

		// Iterate through and execute each command
		// List<String> commands = outcome.getValue();
		List<String> commands = outcome.getValue();
		for (int i = 0; i < commands.size(); i++) {
			String command = commands.get(i);

			// Replace {player}
			if (command.contains("{player}")) {
				String name = player.getDisplayName();
				command = command.replace("{player}", name);
			}

			// Replace {world}
			if (command.contains("{world}")) {
				String world = player.getWorld().getName();
				command = command.replace("{world}", world);
			}

			// Replace {rplayer}
			if (command.contains("{rplayer}")) {
				String rplayer = getRandomPlayer();
				command = command.replace("{rplayer}", rplayer);
			}

			// Replace {rtime:xx-xx}
			// TODO CLEAN THIS MOTHERFUCKER
			if (command.contains("{rtime:")) {
				String[] cmdArgs = command.split(" ");
				String[] newCmd = new String[cmdArgs.length];
				for (int w = 0; w < cmdArgs.length; w++) {
					String arg = cmdArgs[w];
					if (arg.matches("\\{rtime:\\d{1,5}-\\d{1,5}\\}")) {
						String rStr = arg.split(":")[1];
						String[] rRaw = rStr.substring(0, rStr.length() - 1)
								.split("-");

						int[] range = new int[rRaw.length];
						for (int r = 0; r < rRaw.length; r++) {
							range[r] = Integer.parseInt(rRaw[r]);
						}

						int min = Utils.getMin(range, 0);
						int max = Utils.getMax(range, 24000);

						newCmd[w] = getRandomTime(min, max) + "";
					} else {
						newCmd[w] = cmdArgs[w];
					}
				}
				command = Utils.mergeStringArray(newCmd);
			}

			// Determine command type (default: console)
			if (command.startsWith("player=") || command.startsWith("console=")) {
				String[] parts = command.split("=");
				command = Utils.splitStringArray(parts, 1);
				typeOfCommand = parts[0];
			}

			// Remove leading slash if present
			if (command.startsWith("/")) command = command.substring(1);

			// Execute command
			if (typeOfCommand.equals("player")) success = server
					.dispatchCommand(sender, command);
			else success = server.dispatchCommand(console, command);

			// If command returned false, log syntax error
			if (!success) {
				errorMessage = ERROR_SYNTAX;
				failedCommand = command;
				break;
			}

			// Set last roll to now. Doesn't count if there's an error.
			main.setLastRoll(player, main.now());
		}

		// If nothing went wrong, we're done.
		if (success) return true;

		// Error is unknown if it's still null
		if (errorMessage == null) errorMessage = ERROR_UNKNOWN;

		// Report any errors to player and log
		log.severe("Failed to execute command \"" + failedCommand + "\"");
		for (int i = 0; i < errorMessage.length; i++) {
			String message = errorMessage[i];
			message = String.format(message, outcomeName);
			if (message.startsWith("log=")) {
				log.severe(message.split("=")[1]);
			} else {
				player.sendMessage(ChatColor.RED + message);
			}
		}

		return true;
	}

	/**
	 * Determines whether the player is still cooling down
	 * @param lastRoll The player's last roll
	 * @return Whether the player is still cooling down
	 */
	private boolean isCoolingDown(long lastRoll) {
		return isCoolingDown(lastRoll, main.now());
	}

	/**
	 * Determines whether the player is still cooling down
	 * @param lastRoll The player's last roll
	 * @param now The current time in millis
	 * @return Whether the player is still cooling down
	 */
	private boolean isCoolingDown(long lastRoll, long now) {
		long since = now - lastRoll;
		long cooldown = main.getCooldown();

		return (since < cooldown);
	}

	/**
	 * Get time left for cooldown
	 * @param lastRoll The player's last roll
	 * @return How much time is left
	 */
	private long getTimeLeft(long lastRoll) {
		long since = main.now() - lastRoll;
		long cooldown = main.getCooldown();
		return cooldown - since;
	}

	/**
	 * Gets a random player on the server
	 * @return A random player's display name
	 */
	private String getRandomPlayer() {
		Player[] players = main.getServer().getOnlinePlayers();
		int randomIndex = new Random().nextInt(players.length);
		return players[randomIndex].getDisplayName();
	}

	private int getRandomTime(int min, int max) {
		return (new Random().nextInt(max - min)) + min;
	}

}
