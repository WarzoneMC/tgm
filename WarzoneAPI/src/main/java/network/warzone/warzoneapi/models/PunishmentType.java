package network.warzone.warzoneapi.models;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Created by Jorge on 08/24/2019
 */
@AllArgsConstructor @Getter
public enum PunishmentType {
    BAN  (true , true , "tgm.punish.ban"   ),
    BANIP(true , true , "tgm.punish.ban-ip"),
    KICK (true , false, "tgm.punish.kick"  ),
    MUTE (false, true , "tgm.punish.mute"  ),
    WARN (false, false, "tgm.punish.warn"  );

    private boolean shouldKick;
    private boolean timed;
    private String permission;
}
