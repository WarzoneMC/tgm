package network.warzone.tgm.modules.generator;

import lombok.Getter;
import lombok.Setter;

public class GeneratorUpgrader {
    @Setter protected Generator hostGenerator;
    @Getter protected int generatorLevel = 1;

    protected String parseCurrentBroadcast(String broadcast) {
        return broadcast.replace("%level%", Integer.toString(generatorLevel));
    }

    void enable() {}
    void unload() {}
}
