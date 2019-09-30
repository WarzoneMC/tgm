package network.warzone.tgm.modules.kit.classes;

import network.warzone.tgm.modules.kit.classes.abilities.Ability;
import network.warzone.tgm.util.SlotType;
import network.warzone.tgm.util.itemstack.ItemFactory;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class NinjaClass extends GameClass {
    public NinjaClass(Ability... abilities) {
        super(abilities);
        super.setItem(0, ItemFactory.createItem(Material.WOODEN_SWORD));
        super.setItem(1, ItemFactory.createItem(Material.GOLDEN_APPLE));

        super.setItem(3, abilities[0].getAbilityItem());

        super.setItem(6, ItemFactory.createItem(Material.OAK_PLANKS, 64));
        super.setItem(7, ItemFactory.createItem(Material.WOODEN_AXE));

        super.setItem(SlotType.HELMET.slot, ItemFactory.createItem(Material.LEATHER_HELMET));
        super.setItem(SlotType.BOOTS.slot, ItemFactory.createItem(Material.LEATHER_BOOTS));
    }

    @Override
    protected void extraApply(Player player) {
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 1));
    }
}
