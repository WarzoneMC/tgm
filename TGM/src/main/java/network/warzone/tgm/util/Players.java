package network.warzone.tgm.util;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.bukkit.BukkitPlayer;
import com.sk89q.worldedit.util.Location;
import network.warzone.tgm.user.PlayerContext;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class Players {

    public static void reset(Player player, boolean heal, boolean keepInv, boolean attackSpeed) {
        if (heal) {
            player.setHealth(player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());
            player.setFoodLevel(20);
            player.setSaturation(20);
        }
        if (!keepInv) {
            player.getInventory().clear();
            player.getInventory().setArmorContents(new ItemStack[]{new ItemStack(Material.AIR), new ItemStack(Material.AIR), new ItemStack(Material.AIR), new ItemStack(Material.AIR)});
            player.setItemOnCursor(new ItemStack(Material.AIR));
        }

        player.getActivePotionEffects().forEach(potionEffect -> {
            try {
                player.removePotionEffect(potionEffect.getType());
            } catch (NullPointerException ignored) {}
        });

        player.setFireTicks(-20);
        player.setFallDistance(0);
        player.setTotalExperience(0);
        player.setExp(0);
        player.setLevel(0);
        player.setWalkSpeed(0.2F);
        player.setFlySpeed(0.1F);
        player.setRemainingAir(300);

        player.setInvulnerable(false);
        player.setCanPickupItems(true);
        player.setCollidable(true);
        player.setFlying(false);
        player.setAllowFlight(false);

        player.resetTitle();

        if (!keepInv) {
            for (Attribute attribute : Attribute.values()) {
                if (player.getAttribute(attribute) == null) continue;
                for (AttributeModifier modifier : player.getAttribute(attribute).getModifiers()) {
                    player.getAttribute(attribute).removeModifier(modifier);
                }
            }
        }

        if (attackSpeed)
            player.getAttribute(Attribute.GENERIC_ATTACK_SPEED).addModifier(new AttributeModifier(UUID.randomUUID(), "generic.attackSpeed", 24.000D, AttributeModifier.Operation.ADD_SCALAR));

        player.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 1, 0)); // Weird lava bug
        player.updateInventory();
    }

    public static int getPing(Player player) {
        return player.spigot().getPing();
    }

    public static void sendMessage(Player player, String message, Object... objects) {
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', String.format(message, objects)));
    }

    public static void findFreePosition(Player player) {
        if (Plugins.isWorldEditPresent()) {
            BukkitPlayer bukkitPlayer = BukkitAdapter.adapt(player);
            Location pos = bukkitPlayer.getSolidBlockTrace(300);
            if (pos != null) {
                bukkitPlayer.findFreePosition(pos);
            }
        }
    }

    public static void passThroughForwardWall(Player player) {
        if (Plugins.isWorldEditPresent()) {
            BukkitAdapter.adapt(player).passThroughForwardWall(6);
        }
    }

    public static org.bukkit.Location location(PlayerContext context) {
        return context.getPlayer().getLocation();
    }

    public static Optional<PlayerContext> getNearestPlayer(PlayerContext player, List<PlayerContext> players) {
        return players.stream().min((p1, p2) -> {
            org.bukkit.Location loc = location(player);
            org.bukkit.Location loc1 = location(p1);
            org.bukkit.Location loc2 = location(p2);
            return Integer.compare((int) loc.distanceSquared(loc1), (int) loc.distanceSquared(loc2));
        });
    }

    public static boolean isFallingIntoVoid(Player player) {
        if (player.getVelocity().lengthSquared() <= 0.006146560239257815) return false; // Motionless
        org.bukkit.Location location = player.getLocation().clone();
        if (location.getY() < 0) return true;
        for (; location.getY() >= 0; location.add(0, -1, 0)) {
            Material material = player.getWorld().getBlockAt(location).getType();
            if (material.isSolid()) return false;
        }
        return true;
    }

}
