package com.minehut.tgm.map;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.ChatColor;

/**
 * Created by luke on 4/27/17.
 */
@AllArgsConstructor @Getter
public class ParsedTeam {
    private String id;
    private String alias;
    private ChatColor teamColor;
    private int max;
    private int min;
}
