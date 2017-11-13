package network.warzone.warzoneapi.models;

import lombok.Getter;

@Getter
public class IssuePunishmentResponse {

    private boolean notFound;

    private Punishment punishment;
    private boolean kickable;

    private String name;

}
