package network.warzone.warzoneapi.models;

import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;

import java.util.List;

/**
 * Created by Jorge on 2/22/2018.
 */
@AllArgsConstructor
public class RankManageRequest {

    private String name;
    private int priority;
    private String prefix;
    private List<String> permissions;
    private boolean staff;
    @SerializedName("default")
    private boolean def;

    public RankManageRequest(String name) {
        this.name = name;
    }

    public enum Action {
        CREATE(),
        DELETE()
    }
}
