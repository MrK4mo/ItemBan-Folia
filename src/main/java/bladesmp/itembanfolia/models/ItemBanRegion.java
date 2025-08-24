package bladesmp.itembanfolia.models;

import org.bukkit.Location;
import org.bukkit.Material;

import java.util.List;

public class ItemBanRegion {

    private final String name;
    private final Location minLocation;
    private final Location maxLocation;
    private final List<Material> bannedItems;

    public ItemBanRegion(String name, Location minLocation, Location maxLocation, List<Material> bannedItems) {
        this.name = name;
        this.minLocation = minLocation;
        this.maxLocation = maxLocation;
        this.bannedItems = bannedItems;
    }

    public String getName() {
        return name;
    }

    public Location getMinLocation() {
        return minLocation;
    }

    public Location getMaxLocation() {
        return maxLocation;
    }

    public List<Material> getBannedItems() {
        return bannedItems;
    }

    public boolean contains(Location location) {
        if (!location.getWorld().equals(minLocation.getWorld())) {
            return false;
        }

        double x = location.getX();
        double y = location.getY();
        double z = location.getZ();

        return x >= minLocation.getX() && x <= maxLocation.getX() &&
                y >= minLocation.getY() && y <= maxLocation.getY() &&
                z >= minLocation.getZ() && z <= maxLocation.getZ();
    }

    public boolean isItemBanned(Material material) {
        return bannedItems.contains(material);
    }

    public void addBannedItem(Material material) {
        if (!bannedItems.contains(material)) {
            bannedItems.add(material);
        }
    }

    public void removeBannedItem(Material material) {
        bannedItems.remove(material);
    }

    public double getVolume() {
        return (maxLocation.getX() - minLocation.getX() + 1) *
                (maxLocation.getY() - minLocation.getY() + 1) *
                (maxLocation.getZ() - minLocation.getZ() + 1);
    }

    @Override
    public String toString() {
        return "ItemBanRegion{" +
                "name='" + name + '\'' +
                ", world=" + minLocation.getWorld().getName() +
                ", min=" + minLocation.getBlockX() + "," + minLocation.getBlockY() + "," + minLocation.getBlockZ() +
                ", max=" + maxLocation.getBlockX() + "," + maxLocation.getBlockY() + "," + maxLocation.getBlockZ() +
                ", bannedItems=" + bannedItems.size() +
                '}';
    }
}