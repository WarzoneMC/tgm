package com.minehut.tgm.damage.tracker.timer;

import org.bukkit.plugin.java.JavaPlugin;

public class TickTimer {
    private final JavaPlugin plugin;
    private int taskId;
    private long ticks = 0;
    private boolean running;

    public TickTimer(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void setTicks(long ticks) {
        this.ticks = ticks;
    }

    public long getTicks() {
        return this.ticks;
    }

    public boolean isRunning() {
        return this.running;
    }

    public void start() {
        if (this.running) {
            throw new RuntimeException("Timer already running");
        }

        this.taskId = this.plugin.getServer().getScheduler().scheduleSyncRepeatingTask(this.plugin, () -> TickTimer.this.ticks++, 1L, 1L);

        this.running = true;
    }

    public void stop() {
        if (!this.running) {
            throw new RuntimeException("Timer not running");
        }

        this.plugin.getServer().getScheduler().cancelTask(this.taskId);
    }
}
