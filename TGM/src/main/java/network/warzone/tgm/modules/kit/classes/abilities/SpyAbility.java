package network.warzone.tgm.modules.kit.classes.abilities;

import java.util.List;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.DyeColor;
import org.bukkit.FireworkEffect;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.LeatherArmorMeta;

import net.md_5.bungee.api.ChatColor;
import network.warzone.tgm.TGM;
import network.warzone.tgm.modules.damage.DamageInfo;
import network.warzone.tgm.modules.team.MatchTeam;
import network.warzone.tgm.util.ColorConverter;
import network.warzone.tgm.util.FireworkUtil;
import network.warzone.tgm.util.itemstack.ItemFactory;

/**
 * Created by lucas on 4/28/16.
 */
public class SpyAbility extends Ability {

    private final Random random;
    private ConcurrentLinkedQueue<Player> disguised = new ConcurrentLinkedQueue<>();

    public SpyAbility() {
        super("Disguise", 20 * 25, Material.INK_SAC,  ChatColor.WHITE + "Disguise");
        this.random = new Random();
    }

    private Color generateAlternate(Player player) {
        MatchTeam playerTeam = this.teamManagerModule.getTeam(player);
        List<MatchTeam> otherTeams = this.teamManagerModule.getTeams().stream().filter((team) -> !team.isSpectator() && !team.equals(playerTeam)).collect(Collectors.toList());
        if (otherTeams.size() == 0) return Color.BLACK;
        Color selectedColor = ColorConverter.getColor(otherTeams.get(random.nextInt(otherTeams.size())).getColor()).mixDyes(DyeColor.BLACK);
        return selectedColor;
    }

    @Override
    public void onClick(Player player) {
        Color alternate = this.generateAlternate(player);

        FireworkUtil.spawnFirework(player.getLocation(), FireworkEffect.builder().withColor(alternate).build(), 0);

        ItemStack helmet = ItemFactory.createItem(Material.LEATHER_HELMET);
        LeatherArmorMeta helmetMeta = (LeatherArmorMeta) helmet.getItemMeta();
        helmetMeta.setColor(alternate);
        helmet.setItemMeta(helmetMeta);
        player.getInventory().setHelmet(helmet);

        ItemStack chestplate = ItemFactory.createItem(Material.LEATHER_CHESTPLATE);
        LeatherArmorMeta chestplateMeta = (LeatherArmorMeta) chestplate.getItemMeta();
        chestplateMeta.setColor(alternate);
        chestplate.setItemMeta(chestplateMeta);
        player.getInventory().setChestplate(chestplate);

        ItemStack leggings = ItemFactory.createItem(Material.LEATHER_LEGGINGS);
        LeatherArmorMeta leggingsMeta = (LeatherArmorMeta) leggings.getItemMeta();
        leggingsMeta.setColor(alternate);
        leggings.setItemMeta(leggingsMeta);
        player.getInventory().setLeggings(leggings);

        ItemStack boots = ItemFactory.createItem(Material.LEATHER_BOOTS);
        LeatherArmorMeta bootsMeta = (LeatherArmorMeta) boots.getItemMeta();
        bootsMeta.setColor(alternate);
        boots.setItemMeta(bootsMeta);
        player.getInventory().setBoots(boots);

        disguised.add(player);
//        player.setCustomName(alternate == Color.RED ? C.dred + player.getName() : C.dblue + player.getName());
//        for (ScoreboardModule scoreboard : GameHandler.getGameHandler().getMatch().getModules().getModules(ScoreboardModule.class)) {
//            scoreboard.getSimpleScoreboard().getScoreboard().getTeam(Teams.getTeamByPlayer(player).get().getId() + "_spy").addEntry(player.getName());
//        }
        super.putOnCooldown(player);

        for (int i = 0; i < 40; i++) {
            final int n = i;
            Bukkit.getScheduler().runTaskLater(TGM.get(), () -> {
                if (n == 39) {
                    player.sendActionBar(ChatColor.RED + "No longer disguised!");

                    if (!teamManagerModule.getTeam(player).isSpectator()) {
                        Color current = ColorConverter.getColor(teamManagerModule.getTeam(player).getColor());

                        helmetMeta.setColor(current);
                        helmet.setItemMeta(helmetMeta);
                        player.getInventory().setHelmet(helmet);

                        chestplateMeta.setColor(current);
                        chestplate.setItemMeta(chestplateMeta);
                        player.getInventory().setChestplate(chestplate);

                        leggingsMeta.setColor(current);
                        leggings.setItemMeta(leggingsMeta);
                        player.getInventory().setLeggings(leggings);

                        bootsMeta.setColor(current);
                        boots.setItemMeta(bootsMeta);
                        player.getInventory().setBoots(boots);

                        FireworkUtil.spawnFirework(player.getLocation(), FireworkEffect.builder().withColor(current).build(), 0);

                    }
                    disguised.remove(player);
                } else {
                    String loadingBar = ChatColor.GREEN.toString();
                    for (int j = n; j < 40; j ++) {
                        loadingBar += ":";
                    }
                    loadingBar += ChatColor.RED;
                    for (int j = 0; j < n; j ++) {
                        loadingBar += ":";
                    }

                    player.sendActionBar(loadingBar);
                }
            }, n * 5L);
        }
    }

    @EventHandler
    public void onAttack(EntityDamageByEntityEvent event) {
        DamageInfo damageInfo = new DamageInfo(event);
        if (damageInfo.getHurtPlayer() != null && damageInfo.getDamagerPlayer() != null && damageInfo.getProjectile() == null) {
            Player damager = damageInfo.getDamagerPlayer();
            Player hurt = damageInfo.getHurtPlayer();
            if (disguised.contains(damager)) {
                if (isWithinAngle(damager.getEyeLocation().getYaw(), hurt.getEyeLocation().getYaw(), 30)) {
                    event.setDamage(event.getDamage() * 2.0);
                    damager.getWorld().playSound(damager.getLocation(), Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1F, 1F);
                    damager.getWorld().playSound(damager.getLocation(), Sound.ENTITY_PLAYER_ATTACK_STRONG, 1F, 1F);
                }
            }
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getInventory() instanceof PlayerInventory && event.getWhoClicked() instanceof Player && disguised.contains(event.getWhoClicked())) {
            Player player = (Player) event.getWhoClicked();
            if (disguised.contains(player) && event.getSlotType().equals(InventoryType.SlotType.ARMOR)) {
                event.setCancelled(true);
            }
        }
    }

    private boolean isWithinAngle(float yaw1, float yaw2, int range) {
        yaw1 += 180;
        yaw2 += 180;
        return yaw1 > yaw2 - range && yaw1 < yaw2 + range;
    }

}
