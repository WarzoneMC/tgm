package com.minehut.tgm.damage.tracker.trackers.base.gravity;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public class Fall {
    public enum Cause { HIT, SHOOT, SPLEEF }
    public enum From { FLOOR, LADDER, WATER }

    // The falling player
    final public Player victim;

    // The player who will get credit for any damage caused by the fall
    final public LivingEntity attacker;

    // The kind of attack that initiated the fall
    final public Cause cause;

    // The type of place that the player fell from
    final public From from;

    // The time of the attack or block break that initiated the fall
    final public long attackTime;

    // If the player is on the ground when attacked, this is initially set false and later set true when they leave
    // the ground within the allowed time window. If the player is already in the air when attacked, this is set true.
    // This is used to distinguish the initial knockback/spleef from ground touches that occur during the fall.
    public boolean isFalling;

    // The player's most recent swimming state and the time it was last set true
    public boolean isSwimming;
    public long swimmingTime;

    // The player's most recent climbing state and the time it was last set true
    public boolean isClimbing;
    public long climbingTime;

    // The player's most recent in-lava state and the time it was last set true
    public boolean isInLava;
    public long inLavaTime;

    // The number of times the player has touched the ground during since isFalling was set true
    public int groundTouchCount;

    Fall(LivingEntity attacker, Cause cause, Player victim, From from, long attackTime) {
        this.attacker = attacker;
        this.cause = cause;
        this.victim = victim;
        this.from = from;
        this.attackTime = this.swimmingTime = this.climbingTime = attackTime;
        this.groundTouchCount = 0;
    }
}
