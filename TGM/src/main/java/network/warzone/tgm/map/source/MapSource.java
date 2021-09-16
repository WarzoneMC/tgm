package network.warzone.tgm.map.source;

import java.io.File;

public abstract class MapSource {
    protected final String name;
    protected final File destinationDirectory;

    public MapSource(String name, File destinationDirectory) {
        this.name = name;
        this.destinationDirectory = destinationDirectory;
    }

    public final String getSourceName() {
        return this.name;
    }

    public File getDestinationDirectory() {
        return this.destinationDirectory;
    }

    public abstract void refreshMaps();
}
