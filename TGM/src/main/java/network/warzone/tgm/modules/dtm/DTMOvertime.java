package network.warzone.tgm.modules.dtm;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import network.warzone.tgm.modules.team.MatchTeam;

import java.util.List;

@AllArgsConstructor @Getter
public class DTMOvertime {
    @Setter private boolean overtimeEnabled;
    private List<String> noRespawn;
    private String respawnTitle;
    private String respawnSubtitle;
}
