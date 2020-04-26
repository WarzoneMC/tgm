package network.warzone.tgm.modules.respawn;

import lombok.AllArgsConstructor;
import lombok.Getter;
import network.warzone.tgm.modules.team.MatchTeam;

import java.util.List;

@AllArgsConstructor @Getter
public class RespawnRule {

       private List<MatchTeam> teams;
       private int delay;
       private boolean freeze;
       private boolean blindness;
       private boolean confirm;

}
