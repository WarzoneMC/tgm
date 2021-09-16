package network.warzone.tgm.modules.kit.classes.abilities;

import network.warzone.tgm.TGM;
import network.warzone.tgm.modules.respawn.RespawnModule;
import network.warzone.tgm.modules.team.MatchTeam;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ThreadLocalRandom;

public class PhoenixAbility extends Ability {

    public Map<FallingBlock, BukkitTask> tasks = new HashMap<>();
    private Queue<BukkitTask> tempPrimaryTasks = new ConcurrentLinkedQueue<>();
    private RespawnModule respawnModule;

    public PhoenixAbility() {
        super("Fire Breath", 20 * 15, Material.BLAZE_POWDER, ChatColor.GOLD.toString() + ChatColor.BOLD + "FIRE BREATH");
        respawnModule = TGM.get().getModule(RespawnModule.class);
    }

    @Override
    public void onClick(final Player player) {
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_BLAZE_SHOOT, 1, 1);
        MatchTeam team = teamManagerModule.getTeam(player);
        ThreadLocalRandom rand = ThreadLocalRandom.current();

        for (int i = 0; i < 20; i++) {
            tempPrimaryTasks.add(
                Bukkit.getScheduler().runTaskLater(TGM.get(), () -> createFire(player, player.getLocation().getDirection().multiply(new Vector(rand.nextDouble(0.8, 1.2), rand.nextDouble(0.8, 1.2), rand.nextDouble(0.8, 1.2))), team), i * 2L)
            );
        }

        super.putOnCooldown(player);
    }

    @EventHandler
    public void onBlockChangeForm(EntityChangeBlockEvent event) {
        if (event.getEntityType() == EntityType.FALLING_BLOCK && event.getTo() == Material.FIRE) {
            event.setCancelled(true);
        }
    }

    private void createFire(Player player, Vector velocity, MatchTeam team) {
        if (tempPrimaryTasks == null || tempPrimaryTasks.poll() == null || respawnModule.isDead(player)) return;
        final FallingBlock fallingBlock = player.getWorld().spawnFallingBlock(player.getLocation().clone().add(0, 0.4, 0), Material.FIRE.createBlockData());
        fallingBlock.setVelocity(velocity);
        player.getWorld().playSound(player.getLocation(), Sound.BLOCK_SAND_FALL, 1, 1);
        tasks.put(fallingBlock, Bukkit.getScheduler().runTaskTimer(TGM.get(), () -> {
            if (fallingBlock.isDead() && tasks.containsKey(fallingBlock)) {
                tasks.get(fallingBlock).cancel();
            } else if (fallingBlock.isOnGround()) {
                fallingBlock.remove();
            } else {
                for (Entity entity : fallingBlock.getNearbyEntities(0.7, 1.0, 0.7)) {
                    if (entity.equals(player)) continue;
                    if (entity instanceof LivingEntity) {
                        if (entity instanceof Player) {
                            Player targetPlayer = (Player) entity;
                            if (team.containsPlayer(targetPlayer) || teamManagerModule.getTeam(targetPlayer).isSpectator()) {
                                continue;
                            }
                        } else {
                            if (entity instanceof Creature && entity.getCustomName() != null
                                    && entity.getCustomName().contains(team.getColor().toString())) {
                                continue;
                            }
                        }
                        entity.setFireTicks(240);
                        ((LivingEntity) entity).damage(6.0, player);
                    }
                }
            }
        }, 1L, 1L));
    }


    @Override
    public void terminate() {
        super.terminate();
        for (BukkitTask bukkitTask : tempPrimaryTasks) bukkitTask.cancel();
        tempPrimaryTasks = null;
        for (BukkitTask bukkitTask : tasks.values()) bukkitTask.cancel();
        tasks = null;
    }


}
