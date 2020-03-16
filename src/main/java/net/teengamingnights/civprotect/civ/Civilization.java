package net.teengamingnights.civprotect.civ;

import net.devtech.utilib.duples.Pair;
import net.teengamingnights.civprotect.cities.City;
import net.teengamingnights.civprotect.perms.Rank;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.*;

public class Civilization implements PowerHolder {
	private final CivilizationManager civilizations;
	private final String name;
	private Map<UUID, Rank> members = new HashMap<>();
	private List<City> cities = new ArrayList<>();

	public Civilization(CivilizationManager civilizations, String name) {
		this.civilizations = civilizations;
		this.name = name;
	}

	public boolean canModify(Player player, Location location) {
		Rank rank = this.members.get(player.getUniqueId());
		if (rank != null) {
			for (City city : this.cities) {
				if (city.canModify(rank, location)) { return true; }
			}
		}
		return false;
	}

	public String getName() {
		return this.name;
	}

	public boolean hasPlayer(Player player) {
		return this.members.containsKey(player.getUniqueId());
	}

	public List<City> getCities() {
		return this.cities;
	}

	public void promote(Player player, Rank rank) {
		this.members.put(player.getUniqueId(), rank);
	}

	public void createCity(City city) {
		this.cities.add(city);
	}

	public boolean canCreateCity(Player player) {
		Rank rank = this.getRank(player);
		if(rank == Rank.LEADER) { // only leaders may create cities
			double power = this.getPower();
			if(this.powerForNextCity() > power)
				return false;
		}
		return false;
	}

	/**
	 * compute the power needed for the next city
	 */
	public double powerForNextCity() {
		return Math.pow(4, this.cities.size()); // becomes exponentially harder to make cities
	}

	@Nullable
	public Rank getRank(@NotNull UUID uuid) {
		return this.members.get(uuid);
	}

	@Nullable
	public Rank getRank(@NotNull Player player) {
		return this.getRank(player.getUniqueId());
	}

	/**
	 * give power to the civilization
	 *
	 * @param death if null, all cities are given equal power
	 */
	public void grantPower(@Nullable
	                       Location death, double power) {
		if (death == null) {
			for (City city : this.cities) {
				city.addPower(power / this.cities.size());
			}
		} else {
			double combinedDistance = 0;
			List<Pair<City, Double>> validCities = new ArrayList<>();
			for (City city : this.cities) {
				Location location = city.getLocation();
				if (location.getWorld().equals(death.getWorld())) { // only if
					double distance = location.distance(death);
					combinedDistance += distance;
					validCities.add(new Pair<>(city, distance));
				}
			}

			for (Pair<City, Double> city : validCities) {
				double share = city.getB() / combinedDistance;
				city.getA().addPower(share * power); // add power
			}
		}

		// remove invalid cities
		this.cities.removeIf(next -> next.getPower() <= 0);
		if (this.cities.size() < 1) { this.civilizations.collapseCivilization(this); }
	}

	@Override
	public double getPower() {
		double power = 0;
		for (City city : this.cities) {
			power += city.getPower();
		}
		return power;
	}
}
