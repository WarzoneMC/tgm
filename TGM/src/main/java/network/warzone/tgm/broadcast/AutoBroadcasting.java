package network.warzone.tgm.broadcast;

import java.util.Arrays;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public class Broadcast extends JavaPlugin implements Listener {

	public void onEnable() {
		getServer().getPluginManager().registerEvents(this, this);
		broadcast();
	}
	public void broadcast() {
		final String[] messages = {
				ChatColor.AQUA + "\n\n\nJoin our discord community by using command /discord ",
				ChatColor.AQUA + "\n\n\nCheck out our website and store using /buy and /website or direct link https://warz.one/ "
		};
		Bukkit.getScheduler().scheduleSyncRepeatingTask(this, new Runnable(){
			public void run(){
				Bukkit.broadcastMessage(Arrays.asList(messages).get(new Random().nextInt(messages.length)));
			}
		}, 0, 4800);
	}
}
