package network.warzone.tgm.util;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.UUID;

public class Players {

    public static void reset(Player player, boolean heal, boolean keepInv) {
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

}
