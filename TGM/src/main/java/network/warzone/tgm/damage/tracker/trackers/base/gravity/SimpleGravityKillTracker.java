package network.warzone.tgm.damage.tracker.trackers.base.gravity;
import network.warzone.tgm.damage.tracker.base.AbstractTracker;
import network.warzone.tgm.damage.tracker.timer.TickTimer;
import network.warzone.tgm.damage.tracker.util.PlayerBlockChecker;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.plugin.java.JavaPlugin;

import javax.annotation.Nonnull;
import java.util.HashMap;

public class SimpleGravityKillTracker extends AbstractTracker {
    private final HashMap<Location, BrokenBlock> brokenBlocks = new HashMap<>();
    private final HashMap<Player, Fall> falls = new HashMap<>();

    // A player must leave the ground within this many ticks of being attacked for
    // the fall to be caused by knockback from that attack
    public static final long MAX_KNOCKBACK_TIME = 50;

    // A player must leave the ground within this many ticks of a block being broken
    // under them for the fall to be caused by a spleef from that block
    public static final long MAX_SPLEEF_TIME = 20;

    // A player's fall is cancelled if they touch the ground more than this many times
    public static final long MAX_GROUND_TOUCHES = 2;

    // A player's fall is cancelled if they are in water for more than this many ticks
    public static final long MAX_SWIMMING_TIME = 20;

    // A player's fall is cancelled if they are climbing something for more than this many ticks
    public static final long MAX_CLIMBING_TIME = 10;

    private final JavaPlugin plugin;
    private final TickTimer timer;

    public SimpleGravityKillTracker(JavaPlugin plugin, TickTimer timer) {
        this.plugin = plugin;
        this.timer = timer;
    }

    public void clear(@Nonnull World world) {
        this.brokenBlocks.keySet().removeIf(location -> location.getWorld() == world);

        this.falls.keySet().removeIf(player -> player.getWorld() == world);
    }

    private boolean isSupported(final Fall fall) {
        return fall.isClimbing || fall.isSwimming || fall.victim.isOnGround();
    }

    private void cancelFall(Fall fall) {
        this.falls.remove(fall.victim);
    }

    private void checkFallCancelled(final Fall fall) {
        long now = this.timer.getTicks();

        if (this.falls.get(fall.victim) == fall) {
            if (fall.isFalling) {
                if (!fall.isInLava) {
                    if (fall.victim.isOnGround() && fall.groundTouchCount > MAX_GROUND_TOUCHES)
                        this.cancelFall(fall);

                    if (fall.isSwimming && now - fall.swimmingTime > MAX_SWIMMING_TIME)
                        this.cancelFall(fall);

                    if (fall.isClimbing && now - fall.climbingTime > MAX_CLIMBING_TIME)
                        this.cancelFall(fall);
                }
            }
            else {
                if (fall.victim.isOnGround() && now - fall.attackTime > MAX_KNOCKBACK_TIME)
                    this.cancelFall(fall);

                if (fall.isSwimming && now - fall.attackTime > MAX_KNOCKBACK_TIME)
                    this.cancelFall(fall);

                if (fall.isClimbing && now - fall.attackTime > MAX_KNOCKBACK_TIME)
                    this.cancelFall(fall);
            }
        }
    }

    private void scheduleCheckFallCancelled(final Fall fall, final long delay) {
        this.plugin.getServer().getScheduler().runTaskLater(this.plugin, () -> SimpleGravityKillTracker.this.checkFallCancelled(fall), delay + 1);
    }

    /**
     * Called whenever the player becomes "unsupported" to check if they were attacked recently enough for the attack
     * to be responsible for the fall
     */
    private void playerBecameAirborne(Fall fall) {
        if(!fall.isFalling && !this.isSupported(fall) && this.timer.getTicks() - fall.attackTime <= MAX_KNOCKBACK_TIME) {
            fall.isFalling = true;
        }
    }

    /**
     * Called when a player is damaged in a way that could initiate a Fall,
     * i.e. damage from another entity that causes knockback
     */
    public void playerAttacked(Player victim, Entity attacker) {
        if (this.falls.containsKey(victim)) {
            // A new fall can't be initiated if the victim is already falling
            return;
        }

        Location loc = victim.getLocation();
        boolean isInLava = PlayerBlockChecker.isSwimming(loc, Material.LAVA);
        boolean isClimbing = PlayerBlockChecker.isClimbing(loc);
        boolean isSwimming = PlayerBlockChecker.isSwimming(loc, Material.WATER);

        // Figure out the entity responsible for the attack and bail if it's not living
        Fall.Cause cause;
        if (attacker instanceof Projectile && ((Projectile) attacker).getShooter() instanceof LivingEntity) {
            attacker = (LivingEntity) ((Projectile) attacker).getShooter();
            cause = Fall.Cause.SHOOT;
        }
        else {
            cause = Fall.Cause.HIT;
        }

        if (!(attacker instanceof LivingEntity)) {
            return;
        }

        // Note the victim's situation when the attack happened
        Fall.From from;
        if (isClimbing) {
            from = Fall.From.LADDER;
        }
        else if(isSwimming) {
            from = Fall.From.WATER;
        }
        else {
            from = Fall.From.FLOOR;
        }

        Fall fall = new Fall((LivingEntity) attacker, cause, victim, from, this.timer.getTicks());
        this.falls.put(victim, fall);

        fall.isClimbing = isClimbing;
        fall.isSwimming = isSwimming;
        fall.isInLava = isInLava;

        // If the victim is already in the air, immediately confirm that they are falling.
        // Otherwise, the fall will be confirmed when they leave the ground, if it happens
        // within the time window.
        fall.isFalling = !this.isSupported(fall);

        if(!fall.isFalling) {
            this.scheduleCheckFallCancelled(fall, MAX_KNOCKBACK_TIME);
        }
    }

    /**
     * Called when a block is broken in a way that could initiate a spleef fall
     */
    public void blockBroken(Block block, Player breaker) {
        Material material = block.getType();
        if (!material.isSolid()) {
            return;
        }
        // Bukkit considers these "solid" for some reason
        switch(material) {
            case SIGN_POST:
            case WALL_SIGN:
            case WOOD_PLATE:
            case STONE_PLATE:
            case IRON_PLATE:
            case GOLD_PLATE:
                return;
        }

        final BrokenBlock brokenBlock = new BrokenBlock(block, breaker, this.timer.getTicks());
        final Location location = brokenBlock.block.getLocation();
        this.brokenBlocks.put(location, brokenBlock);

        this.plugin.getServer().getScheduler().runTaskLater(this.plugin, () -> {
            // Only remove the BrokenBlock if it's the same one we added. It may have been replaced since then.
            if (SimpleGravityKillTracker.this.brokenBlocks.get(location) == brokenBlock) {
                SimpleGravityKillTracker.this.brokenBlocks.remove(location);
            }
        }, MAX_SPLEEF_TIME + 1);
    }

    /**
     * Called when a player moves in a way that could affect their fall i.e. landing on a ladder or in liquid
     */
    public void playerMoved(Player player, Location to) {
        if (this.falls.containsKey(player)) {
            Fall fall = this.falls.get(player);

            boolean isClimbing = PlayerBlockChecker.isClimbing(to);
            boolean isSwimming = PlayerBlockChecker.isSwimming(to, Material.WATER);
            boolean isInLava = PlayerBlockChecker.isSwimming(to, Material.LAVA);
            boolean becameAirborne = false;

            if (isClimbing != fall.isClimbing) {
                if ((fall.isClimbing = isClimbing)) {
                    // Player moved onto a ladder, cancel the fall if they are still on it after MAX_CLIMBING_TIME
                    fall.climbingTime = this.timer.getTicks();
                    this.scheduleCheckFallCancelled(fall, MAX_CLIMBING_TIME + 1);
                }
                else {
                    becameAirborne = true;
                }
            }

            if (isSwimming != fall.isSwimming) {
                if((fall.isSwimming = isSwimming)) {
                    // Player moved into water, cancel the fall if they are still in it after MAX_SWIMMING_TIME
                    fall.swimmingTime = this.timer.getTicks();
                    this.scheduleCheckFallCancelled(fall, MAX_SWIMMING_TIME + 1);
                } else {
                    becameAirborne = true;
                }
            }

            if (becameAirborne) {
                // Player moved out of water or off a ladder, check if it was caused by the attack
                this.playerBecameAirborne(fall);
            }

            if (isInLava != fall.isInLava) {
                if ((fall.isInLava = isInLava)) {
                    fall.inLavaTime = this.timer.getTicks();
                }
                else {
                    // Because players continue to "fall" as long as they are in lava, moving out of lava
                    // can immediately end their fall
                    this.checkFallCancelled(fall);
                }
            }
        }
    }

    /**
     * Called when the player touches or leaves the ground
     */
    public void playerOnOrOffGround(Player player, boolean onGround) {
        Fall fall = this.falls.get(player);
        if (fall != null) {
            if (!onGround) {
                // Falling player left the ground, check if it was caused by the attack
                this.playerBecameAirborne(fall);
            }
        }
        else if(!onGround) {
            // Player that is not currently falling left the ground, check if it was caused by a spleef
            BrokenBlock brokenBlock = BrokenBlock.lastBlockBrokenUnderPlayer(player, this.brokenBlocks);
            if(brokenBlock != null && this.timer.getTicks() - brokenBlock.time <= MAX_SPLEEF_TIME) {
                fall = new Fall(brokenBlock.breaker, Fall.Cause.SPLEEF, player, Fall.From.FLOOR, brokenBlock.time );

                Location loc = player.getLocation();
                fall.isClimbing = PlayerBlockChecker.isClimbing(loc);
                fall.isSwimming = PlayerBlockChecker.isSwimming(loc, Material.WATER);
                fall.isInLava = PlayerBlockChecker.isSwimming(loc, Material.LAVA);

                fall.isFalling = true;

                this.falls.put(player, fall);
            }
        }
    }

    /**
     * Called when anything happens to a player that should cancel their fall i.e. death, gamemode change, etc.
     */
    public void cancelFall(Player victim) {
        this.falls.remove(victim);
    }

    /**
     * Get the Fall that caused the given damage to the given player,
     * or null if the damage was not caused by a Fall.
     */
    public Fall getCausingFall(Player victim, EntityDamageEvent.DamageCause damageCause) {
        Fall fall = this.falls.get(victim);

        if (fall == null || !fall.isFalling) {
            return null;
        }

        // Do an extra check to see if the fall should be cancelled
        this.checkFallCancelled(fall);
        if (!this.falls.containsKey(victim)) {
            return null;
        }

        switch (damageCause) {
            case VOID:
            case FALL:
            case LAVA:
            case SUICIDE:
                return fall;
            case FIRE_TICK:
                return fall.isInLava ? fall : null;
            default:
                return null;
        }
    }
}
