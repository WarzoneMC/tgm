package network.warzone.tgm.broadcast;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Created by Jorge on 4/14/2018.
 */
@AllArgsConstructor @Getter
public class Broadcast {
    private String id;
    private String message;
    private String permission;
}
