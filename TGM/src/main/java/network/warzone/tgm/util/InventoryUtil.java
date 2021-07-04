package network.warzone.tgm.util;

import net.minecraft.server.v1_17_R1.NBTTagList;
import net.minecraft.server.v1_17_R1.NBTTagString;
import network.warzone.tgm.TGM;
import network.warzone.tgm.modules.killstreak.KillstreakModule;
import network.warzone.tgm.util.itemstack.Effects;
import network.warzone.tgm.util.itemstack.ItemFactory;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.craftbukkit.v1_17_R1.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

/**
 * Created by Jorge on 10/16/2019
 */
public class InventoryUtil {
    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("0.0");

    public static Inventory clone(Inventory i, String title) {
        Inventory inventory;
        if (title == null) {
            if (i.getType() == InventoryType.CHEST) inventory = Bukkit.createInventory(i.getHolder(), i.getSize());
            else inventory = Bukkit.createInventory(i.getHolder(), i.getType());
        }
        else {
            if (i.getType() == InventoryType.CHEST) inventory = Bukkit.createInventory(i.getHolder(), i.getSize(), title);
            else inventory = Bukkit.createInventory(i.getHolder(), i.getType(), title);
        }
        inventory.setContents(i.getContents());
        return inventory;
    }

    public static Inventory clone(Inventory i) {
        return clone(i, null);
    }

    public static Inventory clonePlayerInventory(Player player) {
        Inventory inventory = Bukkit.createInventory(null, 5*9, player.getName() + "'" + (player.getName().endsWith("s") ? "" : "s") + " inventory");
        setPlayerInventoryContents(player, inventory);
        return inventory;
    }

    public static void setPlayerInventoryContents(Player player, Inventory inventory) {
        ItemStack[] contents = player.getInventory().getContents();
        ItemStack[] armorContents = player.getInventory().getArmorContents();
        for (int i = 0; i < 36; i++) {
            ItemStack itemStack = contents[i];
            if (itemStack == null) continue;
            if (i < 9) inventory.setItem(36 + i, itemStack);
            else inventory.setItem(i, itemStack);
        }
        for (int i = 0; i < 4; i++) {
            ItemStack itemStack = armorContents[3 - i];
            if (itemStack == null) continue;
            inventory.setItem(i, itemStack);
        }
        inventory.setItem(4, player.getInventory().getItemInOffHand());

        KillstreakModule killstreakModule = TGM.get().getModule(KillstreakModule.class);
        int killstreak = killstreakModule.getKillstreak(player.getUniqueId().toString());
        if (killstreak > 0) {
            inventory.setItem(6, getKillstreakItem(killstreak));
        }

        inventory.setItem(7, getHealthItem(player));
        inventory.setItem(8, getPotionsItem(player.getActivePotionEffects()));
    }

    public static Color colorFromTime() {
        return Color.fromRGB(0);
    }

    private static ItemStack getKillstreakItem(int killstreak) {
        int amount = killstreak > 64 ? 1 : killstreak;
        Material material = killstreak > 64 ? Material.DIAMOND_SWORD : Material.IRON_SWORD;
        return ItemFactory.createItem(material, ChatColor.GREEN + "Killstreak: " + ChatColor.DARK_GREEN + killstreak, amount);
    }

    private static ItemStack getHealthItem(Player player) {
        return ItemFactory.createItem(Material.APPLE, ChatColor.RED + "Player health", Arrays.asList(
                ChatColor.GRAY + "Health: " + ChatColor.WHITE + ((int) player.getHealth()) + " / " + ((int) player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue()),
                ChatColor.GRAY + "Food: " + ChatColor.WHITE + player.getFoodLevel(),
                ChatColor.GRAY + "Saturation: " + ChatColor.WHITE + DECIMAL_FORMAT.format(player.getSaturation())
        ));
    }

    private static ItemStack getPotionsItem(Collection<PotionEffect> potionEffects) {
        if (potionEffects == null || potionEffects.isEmpty()) {
            ItemStack itemStack = ItemFactory.createItem(Material.GLASS_BOTTLE, ChatColor.AQUA + "Active potions effects");
            ItemMeta meta = itemStack.getItemMeta();
            meta.setLore(Collections.singletonList(ChatColor.GRAY + "" + ChatColor.ITALIC + "None"));
            itemStack.setItemMeta(meta);
            return itemStack;
        } else {
            ItemStack itemStack = ItemFactory.createItem(Material.POTION, ChatColor.AQUA + "Active potions effects");
            PotionMeta meta = (PotionMeta) itemStack.getItemMeta();
            meta.setColor(Color.BLUE);
            meta.addItemFlags(ItemFlag.values());
            itemStack.setItemMeta(meta);
            net.minecraft.server.v1_17_R1.ItemStack nmsItem = CraftItemStack.asNMSCopy(itemStack);
            if (nmsItem.getTag() != null) {
                NBTTagList nmsLore = new NBTTagList();
                for (PotionEffect potionEffect : potionEffects) {
                    nmsLore.add(NBTTagString.a(String.format("[\"%s\",{\"translate\": \"effect.minecraft.%s\"},\" \",{\"translate\":\"%s\"},\" (%s)\"]",
                            ChatColor.GRAY.toString(),
                            Effects.toMinecraftID(potionEffect.getType()),
                            potionEffect.getAmplifier() > 10 ? "" + potionEffect.getAmplifier() : "enchantment.level." + (potionEffect.getAmplifier() + 1),
                            Strings.formatTime(potionEffect.getDuration() / 20)
                    )));
                }
                nmsItem.getTag().getCompound("display").set("Lore", nmsLore);
            }
            return CraftItemStack.asBukkitCopy(nmsItem);
        }
    }

    public static int calculateSize(int columns, int amount) {
        int rows = (int) Math.ceil((amount * 1.0) / columns);
        return Math.max(9, rows * 9);
    }

    public static int calculateSlot(int columns, int count) {
        int y = (int) Math.floor((count * 1.0) / columns);
        int x = count % columns;
        return (y * 9) + x + 2;
    }

}
