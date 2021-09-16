package network.warzone.tgm.modules.kit.classes.abilities;

import lombok.Getter;
import org.bukkit.entity.Player;

import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by yikes on 09/27/19
 */
public class AbilityManager {
    public enum AbilityStore {
        PHOENIX(PhoenixAbility.class),
        NINJA(NinjaAbility.class),
        BUILDER(BuilderAbility.class);

        @Getter private Class hostAbility;
        AbilityStore(Class hostAbility) {
            this.hostAbility = hostAbility;
        }
    }
    
    private Set<Ability> abilities = new HashSet<>();

    public AbilityManager(Set<Class<? extends Ability>> abilitySet) {
        for (Class<? extends Ability> ability : abilitySet)
            try {
                abilities.add((Ability) ability.getConstructors()[0].newInstance());
            } catch (InstantiationException | IllegalAccessException | IllegalArgumentException
                | InvocationTargetException | SecurityException e) {
                e.printStackTrace();
            }
    }

    public void destroyAbilities() {
        for(Ability ability : abilities) {
            ability.terminate();
        }
        abilities = null;
    }

    public void removePlayerFromAbilityCache(Ability ability, Player player) {
        ability.getRegisteredPlayers().remove(player.getUniqueId());
    }

    @SuppressWarnings("unchecked")
    public <T extends Ability> T getAbility(Class<T> clazz) {
        for(Ability ability : abilities) {
            if (clazz.isInstance(ability)) return ((T) ability);
        }
        return null;
    }
}
