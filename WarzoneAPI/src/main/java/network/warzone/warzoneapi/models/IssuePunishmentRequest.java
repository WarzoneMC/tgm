package network.warzone.warzoneapi.models;

import lombok.AllArgsConstructor;

import java.util.UUID;

@AllArgsConstructor
public class IssuePunishmentRequest {

    private String name;
    private UUID punisherUuid;

    private String type;
    private long length;

    private String reason;

}
