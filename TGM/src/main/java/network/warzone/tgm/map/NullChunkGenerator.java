package network.warzone.tgm.map;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.generator.ChunkGenerator;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

/**
 * Created by MatrixTunnel on 10/25/18.
 */
public class NullChunkGenerator extends ChunkGenerator {
    @Override
    public @NotNull ChunkData generateChunkData(@NotNull World world, @NotNull Random random, int x, int z, @NotNull BiomeGrid biomeGrid) {
        ChunkData data = createChunkData(world);
        for (int i = 0; i < 16; i++) {
            for (int j = 0; j < 256; j++) {
                for (int k = 0; k < 16; k++) {
                    biomeGrid.setBiome(i, j, k, Biome.PLAINS);
                }
            }
        }
        return data;
    }

    @Override
    public final Location getFixedSpawnLocation(@NotNull World world, @NotNull Random random) {
        return new Location(world, 0.0, 65.0, 0.0);
    }
}
