package network.warzone.warzoneapi.models;

import lombok.AllArgsConstructor;

import java.util.UUID;

@AllArgsConstructor
public class IssuePunishmentRequest {

    private String name;
    private String ip;
    private boolean ip_ban;
    private UUID punisherUuid;

    private String type;
    private long length;

    private String reason;

}
