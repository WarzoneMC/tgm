package network.warzone.tgm.modules.generator;

import lombok.Getter;
import lombok.Setter;

public abstract class GeneratorUpgrader {
    @Setter protected Generator hostGenerator;
    @Getter protected int generatorLevel = 1;

    abstract void upgrade();

    protected String parseCurrentBroadcast(String broadcast) {
        return broadcast.replace("%name%", hostGenerator.getName()).replace("%level%", Integer.toString(generatorLevel));
    }

    void unload() {}
}
