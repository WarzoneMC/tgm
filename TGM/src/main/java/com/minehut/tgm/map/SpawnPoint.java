package com.minehut.tgm.map;

import com.minehut.tgm.team.MatchTeam;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.Location;

import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
public class SpawnPoint {
    @Getter
    private final Location location;
}
