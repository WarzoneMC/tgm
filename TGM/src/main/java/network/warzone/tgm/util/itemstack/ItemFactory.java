package network.warzone.tgm.util.itemstack;

import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.potion.Potion;
import org.bukkit.potion.PotionType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by luke on 10/17/15.
 */
public class ItemFactory {

    public static ItemStack createItem(Material material) {
        return new ItemStack(material);
    }

    public static ItemStack createItem(Material material, int amount) {
        return new ItemStack(material, amount);
    }

    public static ItemStack createItem(Material material, String name) {
        ItemStack item = createItem(material, 1);

        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        meta.setLore(null);
        item.setItemMeta(meta);

        return item;
    }

    public static ItemStack createItem(Material material, String name, List<String> lore, int amount) {
        ItemStack item = createItem(material, name);
        item.setAmount(amount);

        ItemMeta meta = item.getItemMeta();
        meta.setLore(lore);
        item.setItemMeta(meta);

        return item;
    }
    
    public static ItemStack createItem(Material material, String name, List<String> lore, int amount, byte data) {
        ItemStack item = new ItemStack(material, amount, data);

        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        meta.setLore(lore);
        item.setItemMeta(meta);
        
        return item;
    }

    public static ItemStack createItem(Material material, String name, List<String> lore) {
        ItemStack item = createItem(material, name);

        ItemMeta meta = item.getItemMeta();
        meta.setLore(lore);
        item.setItemMeta(meta);

        return item;
    }

    public static ItemStack createItem(Material material, String name, ChatColor start, String... strings) {
        ItemStack item = createItem(material, name);

        ItemMeta meta = item.getItemMeta();
        List<String> lore = new ArrayList<>();
        for (String string : strings) {
            lore.add(start + string);
        }
        meta.setLore(lore);

        return item;
    }

    public static ItemStack createItem(Material material, String name, List<String> lore, List<Enchantment> enchantments) {
        ItemStack itemStack = createItem(material, name, lore);
        for (Enchantment enchantment : enchantments) {
            if (itemStack.getEnchantments().containsKey(enchantment)) {
                itemStack.getEnchantments().put(enchantment, itemStack.getEnchantmentLevel(enchantment));
            } else {
                itemStack.getEnchantments().put(enchantment, 1);
            }
        }
        return itemStack;
    }

    public static ItemStack createItem(Material material, List<Enchantment> enchantments) {
        ItemStack itemStack = createItem(material);
        for (Enchantment enchantment : enchantments) {
            if (itemStack.getEnchantments().containsKey(enchantment)) {
                itemStack.getEnchantments().put(enchantment, itemStack.getEnchantmentLevel(enchantment));
            } else {
                itemStack.addEnchantment(enchantment, 1);
            }
        }
        return itemStack;
    }

    public static ItemStack createPotion(PotionType potionType, int level, String name) {
        Potion potion = new Potion(potionType);
        potion.setLevel(level);

        ItemStack itemStack = potion.toItemStack(1);
        ItemMeta meta = itemStack.getItemMeta();
        meta.setDisplayName(name);
        itemStack.setItemMeta(meta);

        return itemStack;
    }

    public static ItemStack getPlayerSkull(String name) {
        ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) skull.getItemMeta();
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(name);
        if (offlinePlayer != null) {
            meta.setOwningPlayer(offlinePlayer);
        } else {
            meta.setOwner(name);
        }
        skull.setItemMeta(meta);
        skull.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        return skull;
    }

    public static ItemStack getPlayerSkull(String name, String displayName, String... lore) {
        ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) skull.getItemMeta();
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(name);
        if (offlinePlayer != null) {
            meta.setOwningPlayer(offlinePlayer);
        } else {
            meta.setOwner(name);
        }
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', displayName));
        if (lore != null && lore.length > 0) {
            meta.setLore(Arrays.stream(lore).map(str -> ChatColor.translateAlternateColorCodes('&', str)).collect(Collectors.toList()));
        }
        skull.setItemMeta(meta);
        skull.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        return skull;
    }

}
