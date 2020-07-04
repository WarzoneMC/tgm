package network.warzone.tgm.map;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class RotationRequirement {
    private final int min;
    private final int max;
}
