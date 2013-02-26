package org.x3chaos.rtd;

import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.logging.Logger;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.java.JavaPlugin;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class RTDPlugin extends JavaPlugin {

	RTDExecutor executor;

	LinkedHashMap<String, List<String>> allOutcomes;
	Logger log;

	@Override
	public void onEnable() {
		log = this.getLogger();
		executor = new RTDExecutor(this);
		this.getCommand("rtd").setExecutor(executor);

		this.saveDefaultConfig();
		allOutcomes = getAllOutcomes();
		
		log.info(getName() + " enabled.");
	}

	public LinkedHashMap<String, List<String>> getAllOutcomes() {
		ConfigurationSection section = getOutcomeSection();

		Set<String> keys = section.getKeys(false);
		LinkedHashMap<String, List<String>> result = Maps.newLinkedHashMap();
		for (String key : keys) {
			List<String> values = section.getStringList(key);
			result.put(key, values);
		}

		return result;
	}

	public ConfigurationSection getOutcomeSection() {
		return getConfig().getConfigurationSection("outcomes");
	}

	public Map.Entry<String, List<String>> getRandomOutcome() {
		int random = new Random().nextInt(allOutcomes.size());
		List<Entry<String, List<String>>> list = Lists.newArrayList();
		list.addAll(allOutcomes.entrySet());
		return list.get(random);
	}

	public void setLastRoll(Player player, long when) {
		player.setMetadata("rtd-lastroll", new FixedMetadataValue(this, when));
	}

	public long getLastRoll(Player player) {
		List<MetadataValue> list = player.getMetadata("rtd-lastroll");
		if (list == null) return 0;
		for (MetadataValue value : list) {
			if (value.getOwningPlugin().getName().equals(this.getName()))
				return value.asLong();
		}

		return 0;
	}

	public long now() {
		return Calendar.getInstance().getTimeInMillis();
	}

	public long getCooldown() {
		return getConfig().getLong("options.cooldown");
	}

}
