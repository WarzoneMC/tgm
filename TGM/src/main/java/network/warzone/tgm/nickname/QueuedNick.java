package network.warzone.tgm.nickname;

import lombok.AllArgsConstructor;
import lombok.Getter;
import network.warzone.warzoneapi.models.Skin;
import org.bukkit.entity.Player;

@Getter @AllArgsConstructor
public class QueuedNick {

    private String name;
    private Skin skin;
    private Player player;

}
