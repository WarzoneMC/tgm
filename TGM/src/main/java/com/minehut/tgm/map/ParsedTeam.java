package com.minehut.tgm.map;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.ChatColor;

/**
 * Created by luke on 4/27/17.
 */
@AllArgsConstructor
public class ParsedTeam {
    @Getter private String id;
    @Getter private String alias;
    @Getter private ChatColor teamColor;
    @Getter private int max;
    @Getter private int min;
}
