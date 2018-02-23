package network.warzone.warzoneapi.models;

import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bson.types.ObjectId;

import java.util.List;

/**
 * Created by Jorge on 10/22/2017.
 */

@AllArgsConstructor
public class Rank {

    @SerializedName("_id")
    @Getter private ObjectId id;
    @Getter private String name;
    @Getter private int priority;
    @Getter private String prefix;
    @Getter private List<String> permissions;
    @Getter private boolean staff;

}
