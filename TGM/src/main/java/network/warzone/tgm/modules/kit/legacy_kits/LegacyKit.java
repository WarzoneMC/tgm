package network.warzone.tgm.modules.kit.legacy_kits;

import lombok.Getter;
import network.warzone.tgm.modules.kit.legacy_kits.abilities.Ability;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public abstract class LegacyKit {
    @Getter private Set<Ability> abilities;
    LegacyKit(Ability... abilities) {
        this.abilities = new HashSet<>(Arrays.asList(abilities));
    }

    /**
        Applies items and effects
     */
    public abstract void apply(Player p);

    /**
     * Adds player's UUID to each of the kits' abilities registeredPlayers
     * @param p
     */
    public void addToAbilityCaches(Player p) {
        UUID playerUUID = p.getUniqueId();
        for (Ability ability : abilities) {
            ability.getRegisteredPlayers().add(playerUUID);
        }
    }

    /**
     * Removes player's UUID to each of the kits' abilities registeredPlayers
     * @param p
     */
    public void removeFromAbilityCaches(Player p) {
        UUID playerUUID = p.getUniqueId();
        for (Ability ability : abilities) {
            ability.getRegisteredPlayers().remove(playerUUID);
        }
    }
}
