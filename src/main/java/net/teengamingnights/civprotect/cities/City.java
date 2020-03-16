package net.teengamingnights.civprotect.cities;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.teengamingnights.civprotect.civ.PowerHolder;
import net.teengamingnights.civprotect.perms.Rank;
import org.bukkit.Location;

public class City implements PowerHolder {
	private double power;
	private Long2ObjectMap<Rank> permission = new Long2ObjectOpenHashMap<>();
	private final Location location;

	public City(Location location, double power) {
		this.power = power;
		this.location = location;
	}

	public City(Location location) {this.location = location;}

	public boolean canModify(Rank rank, Location location) {
		Rank minimum = this.permission.get(this.getKey(location));
		if (minimum == null) {
			return true; // default is true
		}
		return rank.ordinal() >= minimum.ordinal();
	}

	public void setPermission(Location location, Rank rank) {
		this.permission.put(this.getKey(location), rank);
	}

	private long getKey(Location location) {
		int x = location.getBlockX() << 4;
		int z = location.getBlockZ() << 4;
		return (long) x << 32 | z;
	}


	public Location getLocation() {
		return this.location;
	}

	public void addPower(double power) {
		this.power += power;
	}

	public void setPower(double power) {
		this.power = power;
	}

	@Override
	public double getPower() {
		return this.power;
	}
}
