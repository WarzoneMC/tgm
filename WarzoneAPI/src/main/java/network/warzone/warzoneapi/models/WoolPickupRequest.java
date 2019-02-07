package network.warzone.warzoneapi.models;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@AllArgsConstructor
public class WoolPickupRequest {
    @Getter private UUID uuid; //player uuid
}
