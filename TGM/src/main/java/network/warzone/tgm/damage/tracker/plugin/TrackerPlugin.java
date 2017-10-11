package network.warzone.tgm.damage.tracker.plugin;

import network.warzone.tgm.damage.tracker.DamageResolverManager;
import network.warzone.tgm.damage.tracker.DamageResolvers;
import network.warzone.tgm.damage.tracker.TrackerManager;
import network.warzone.tgm.damage.tracker.Trackers;
import network.warzone.tgm.damage.tracker.damage.resolvers.*;
import network.warzone.tgm.damage.tracker.timer.OnGroundTask;
import network.warzone.tgm.damage.tracker.timer.TickTimer;
import network.warzone.tgm.damage.tracker.trackers.*;
import network.warzone.tgm.damage.tracker.trackers.base.*;
import network.warzone.tgm.damage.tracker.trackers.base.gravity.SimpleGravityKillTracker;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import javax.annotation.Nullable;

public class TrackerPlugin {
    private JavaPlugin plugin;
    public @Nullable
    TickTimer tickTimer;
    public @Nullable
    OnGroundTask onGroundTask;

    public TrackerPlugin(JavaPlugin plugin) {
        this.plugin = plugin;

        // basic operation listeners
        this.registerEvents(new LifetimeListener());
        this.registerEvents(new WorldListener(Trackers.getManager()));
        this.registerEvents(new CustomEventListener());

        EntityDamageEventListener damageEventListener = new EntityDamageEventListener();
        damageEventListener.register(plugin);

        // initialize timer
        this.tickTimer = new TickTimer(plugin);
        this.tickTimer.start();

        // on ground task
        this.onGroundTask = new OnGroundTask(plugin);
        this.onGroundTask.start();

        // tracker setup
        TrackerManager tm = Trackers.getManager();

        ExplosiveTracker explosiveTracker = new SimpleExplosiveTracker();
        SimpleGravityKillTracker gravityKillTracker = new SimpleGravityKillTracker(plugin, this.tickTimer);

        explosiveTracker.enable();
        gravityKillTracker.enable();

        this.registerEvents(new ExplosiveListener(explosiveTracker));
        this.registerEvents(new GravityListener(this, gravityKillTracker));

        tm.setTracker(ExplosiveTracker.class, explosiveTracker);
        tm.setTracker(SimpleGravityKillTracker.class, gravityKillTracker);

        DispenserTracker dispenserTracker = new SimpleDispenserTracker();
        dispenserTracker.enable();

        this.registerEvents(new DispenserListener(dispenserTracker));
        tm.setTracker(DispenserTracker.class, dispenserTracker);

        ProjectileDistanceTracker projectileDistanceTracker = new SimpleProjectileDistanceTracker();
        projectileDistanceTracker.enable();

        this.registerEvents(new ProjectileDistanceListener(projectileDistanceTracker));
        tm.setTracker(ProjectileDistanceTracker.class, projectileDistanceTracker);

        OwnedMobTracker ownedMobTracker = new SimpleOwnedMobTracker();
        ownedMobTracker.enable();

        this.registerEvents(new OwnedMobListener(ownedMobTracker));
        tm.setTracker(OwnedMobTracker.class, ownedMobTracker);

        AnvilTracker anvilTracker = new SimpleAnvilTracker();
        anvilTracker.enable();

        this.registerEvents(new AnvilListener(anvilTracker));
        tm.setTracker(AnvilTracker.class, anvilTracker);

        // register damage resolvers
        DamageResolverManager drm = DamageResolvers.getManager();

        drm.register(new BlockDamageResolver());
        drm.register(new FallDamageResolver());
        drm.register(new LavaDamageResolver());
        drm.register(new MeleeDamageResolver());
        drm.register(new ProjectileDamageResolver(projectileDistanceTracker));
        drm.register(new TNTDamageResolver(explosiveTracker, dispenserTracker));
        drm.register(new VoidDamageResolver());
        drm.register(new GravityDamageResolver(gravityKillTracker));
        drm.register(new DispensedProjectileDamageResolver(projectileDistanceTracker, dispenserTracker));
        drm.register(new OwnedMobDamageResolver(ownedMobTracker));
        drm.register(new AnvilDamageResolver(anvilTracker));

        // debug
        // this.registerEvents(new DebugListener());
    }

    private void registerEvents(Listener listener) {
        Bukkit.getPluginManager().registerEvents(listener, plugin);
    }
}
