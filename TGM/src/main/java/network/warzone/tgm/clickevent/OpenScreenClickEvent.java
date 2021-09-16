package network.warzone.tgm.clickevent;

import network.warzone.tgm.match.Match;
import network.warzone.tgm.modules.screens.ScreenManagerModule;
import org.bukkit.entity.Player;

/**
 * Created by Jorge on 10/11/2019
 */
public class OpenScreenClickEvent extends ClickEvent {

    private String screen;

    public OpenScreenClickEvent(String id) {
        this.screen = id;
    }

    @Override
    public void run(Match match, Player player) {
        match.getModule(ScreenManagerModule.class).getScreen(this.screen).openInventory(player);
    }
}
