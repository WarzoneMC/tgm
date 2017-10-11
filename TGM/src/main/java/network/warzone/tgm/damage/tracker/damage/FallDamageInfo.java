package network.warzone.tgm.damage.tracker.damage;


import network.warzone.tgm.damage.tracker.DamageInfo;

/**
 * Represents a damage where the entity hit the ground.
 */
public interface FallDamageInfo extends DamageInfo {
    /**
     * Gets the distance that the entity fell in order to earn this damage.
     *
     * Contract states that distance will always be greater than or equal to 0.
     *
     * @return Distance fallen in blocks
     */
    float getFallDistance();
}
