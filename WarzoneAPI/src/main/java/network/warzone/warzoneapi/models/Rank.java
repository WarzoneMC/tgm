package network.warzone.warzoneapi.models;

import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.bson.types.ObjectId;

import java.util.List;

/**
 * Created by Jorge on 10/22/2017.
 */

@AllArgsConstructor @Getter @Setter
public class Rank {

    @SerializedName("_id")
    private ObjectId id;
    private String name;
    private int priority;
    private String prefix;
    private List<String> permissions;
    private boolean staff;
    @SerializedName("default")
    private boolean def;

    public void set(Rank rank) {
        this.name = rank.name;
        this.priority = rank.priority;
        this.prefix = rank.prefix;
        this.permissions = rank.permissions;
        this.staff = rank.staff;
        this.def = rank.def;
    }

}
