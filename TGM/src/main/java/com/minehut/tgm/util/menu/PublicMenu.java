package com.minehut.tgm.util.menu;

import org.bukkit.plugin.java.JavaPlugin;

/**
 * Created by katie on 2/14/17.
 */
public class PublicMenu extends Menu {

    /**
     * Remains loaded unless forcefully disabled.
     *
     * @param javaPlugin
     * @param name
     * @param slots
     */
    public PublicMenu(JavaPlugin javaPlugin, String name, int slots) {
        super(javaPlugin, name, slots);
    }
}
