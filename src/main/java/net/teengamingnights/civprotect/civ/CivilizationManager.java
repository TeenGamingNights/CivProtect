package net.teengamingnights.civprotect.civ;

import net.teengamingnights.civprotect.perms.Rank;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class CivilizationManager implements Listener {
	private static final double POWER_PER_KILL = 1; // todo add config
	private final List<Civilization> civilizations = new ArrayList<>();

	/**
	 * returns true if the player has permissions to break a block in that area
	 * @param player the player
	 * @param location the location
	 */
	public boolean canBreak(Player player, Location location) {
		for (Civilization civilization : this.civilizations) {
			if (civilization.hasPlayer(player)) { // overlapping claims are contested, players from both civilizations may modify
				if(civilization.canModify(player, location))
					return true;
			}
		}
		return true; // unclaimed territory is free for all
	}

	/**
	 * create a new civilization with 1 leader and a name
	 * @param player the leader of the new civilization
	 * @param name the name of the civilization
	 * @return the newly created civilization
	 */
	public Civilization foundCivilization(Player player, String name) {
		Civilization civilization = new Civilization(this, name);
		civilization.promote(player, Rank.LEADER);
		this.civilizations.add(civilization);
		return civilization;
	}

	/**
	 * collapse a civilization
	 * @param civilization the civilization
	 */
	public void collapseCivilization(Civilization civilization) {
		this.civilizations.remove(civilization);
	}

	/**
	 * @param player the player in question
	 * @return returns the list of civilizations a player is in
	 */
	public Collection<Civilization> getCivilizations(Player player) {
		List<Civilization> civilizations = new ArrayList<>();
		for (Civilization civilization : this.civilizations) {
			if(civilization.getRank(player) != null)
				civilizations.add(civilization);
		}
		return civilizations;
	}

	@EventHandler
	public void kill(PlayerDeathEvent event) {
		Player player = event.getEntity();
		Entity attacker = player.getLastDamageCause().getEntity();
		if (attacker instanceof Player) { // killed by player
			this.grantPower((Player) attacker, POWER_PER_KILL); // killer gains power
			this.grantPower(player, -POWER_PER_KILL); // killed looses power
		}
	}

	@EventHandler(priority = EventPriority.LOW) // must occur before AsynCore listeners
	public void destroy(BlockBreakEvent event) {
		if(!this.canBreak(event.getPlayer(), event.getBlock().getLocation()))
			event.setCancelled(true);
	}

	/**
	 * distributes gained power equally among all the civilizations a player is a part of
	 */
	private void grantPower(Player player, double power) {
		Collection<Civilization> attackerCivilizations = this.getCivilizations(player);
		for (Civilization civilization : attackerCivilizations) { // distribute power to all civilizations
			civilization.grantPower(player.getLocation(), power/attackerCivilizations.size());
		}
	}
}
