package network.warzone.tgm.util;

/**
 * Created by luke on 10/17/15.
 */
public enum SlotType {
    HELMET(103),
    CHESTPLATE(102),
    LEGGINGS(101),
    BOOTS(100);

    public int slot;

    private SlotType(int slot) {
        this.slot = slot;
    }
}
