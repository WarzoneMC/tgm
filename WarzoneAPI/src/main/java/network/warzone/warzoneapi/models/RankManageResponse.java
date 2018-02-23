package network.warzone.warzoneapi.models;

import lombok.Getter;

/**
 * Created by Jorge on 2/23/2018.
 */
@Getter
public class RankManageResponse {
    private String message;
    private boolean error;
    private Rank rank;
}
