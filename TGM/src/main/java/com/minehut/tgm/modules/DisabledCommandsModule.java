package com.minehut.tgm.modules;

import com.minehut.tgm.match.MatchModule;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.util.ArrayList;
import java.util.List;

public class DisabledCommandsModule extends MatchModule implements Listener {
    private final List<String> disabledCommands = new ArrayList<>();

    public DisabledCommandsModule() {
        disabledCommands.add("/me");
        disabledCommands.add("/minecraft:me");
    }

    @EventHandler
    public void onCommand(PlayerCommandPreprocessEvent event) {
        if (disabledCommands.contains(event.getMessage().split(" ")[0].toLowerCase())) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(ChatColor.RED + event.getMessage().split(" ")[0] + " is a disabled command.");
        }
    }
}
