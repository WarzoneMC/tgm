package network.warzone.tgm.util;

import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.InvocationTargetException;
import java.util.UUID;

public class Players {

    public static void reset(Player player, boolean heal) {
        if (heal) player.setHealth(player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());
        player.setFoodLevel(20);
        player.setSaturation(20);
        player.getInventory().clear();
        player.getInventory().setArmorContents(new ItemStack[]{new ItemStack(Material.AIR), new ItemStack(Material.AIR), new ItemStack(Material.AIR), new ItemStack(Material.AIR)});
        player.setItemOnCursor(new ItemStack(Material.AIR));

        player.getActivePotionEffects().forEach(potionEffect -> {
            try {
                player.removePotionEffect(potionEffect.getType());
            } catch (NullPointerException ignored) {}
        });

        player.setFireTicks(0);
        player.setFallDistance(0);
        player.setTotalExperience(0);
        player.setExp(0);
        player.setLevel(0);
        player.setWalkSpeed(0.2F);
        player.setFlySpeed(0.1F);

        player.setInvulnerable(false);
        player.setCanPickupItems(true);
        player.setCollidable(true);
        player.setFlying(false);
        player.setAllowFlight(false);

        player.resetTitle();

        for (Attribute attribute : Attribute.values()) {
            if (player.getAttribute(attribute) == null) continue;
            for (AttributeModifier modifier : player.getAttribute(attribute).getModifiers()) {
                player.getAttribute(attribute).removeModifier(modifier);
            }
        }
        player.getAttribute(Attribute.GENERIC_ATTACK_SPEED).addModifier(new AttributeModifier(UUID.randomUUID(), "generic.attackSpeed", 24.000D, AttributeModifier.Operation.ADD_SCALAR));

        player.updateInventory();
    }
    public static int getPing(Player player) {
        int ping = -1;
        try {
            Object entityPlayer = player.getClass().getMethod("getHandle").invoke(player);
            ping = (int) entityPlayer.getClass().getField("ping").get(entityPlayer);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException | NoSuchFieldException e) {
            e.printStackTrace();
        }
        return ping;
    }
}
