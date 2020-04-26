package network.warzone.warzoneapi.models;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Created by Jorge on 10/22/2017.
 */
@AllArgsConstructor @Getter
public class RankUpdateRequest {

    private String rankName;

    public enum Action {
        ADD, REMOVE;

        public static Action byName(String name) {
            for (Action action : values()) {
                if (action.name().equalsIgnoreCase(name)) {
                    return action;
                }
            }
            return null;
        }
    }

}
