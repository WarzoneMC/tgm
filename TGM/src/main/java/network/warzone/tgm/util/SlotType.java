package network.warzone.tgm.util;

import lombok.Getter;

/**
 * Created by luke on 10/17/15.
 */
public enum SlotType {
    HELMET(103),
    CHESTPLATE(102),
    LEGGINGS(101),
    BOOTS(100);

    @Getter private int slot;

    SlotType(int slot) {
        this.slot = slot;
    }
}
