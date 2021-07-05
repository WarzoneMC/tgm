package network.warzone.tgm.modules.filter.type;

import network.warzone.tgm.match.Match;

public interface FilterType {
    default void load(Match match) {}
    default void unload() {}
}
