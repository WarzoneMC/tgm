package network.warzone.tgm.modules.kit;

import lombok.Getter;
import lombok.Setter;
import network.warzone.tgm.match.Match;
import network.warzone.tgm.match.MatchModule;
import network.warzone.tgm.util.menu.KitEditorMenu;

import java.util.HashMap;
import java.util.UUID;

public class KitEditorModule extends MatchModule {

    @Getter
    @Setter
    private boolean isKitEditable = true;

    @Getter
    @Setter
    private static boolean enabled = true;

    @Getter
    private HashMap<UUID, KitEditorMenu> editorMenus;

    @Override
    public void load(Match match) {
        editorMenus = new HashMap<>();
    }

    @Override
    public void unload() {
        editorMenus.clear();
    }
}
