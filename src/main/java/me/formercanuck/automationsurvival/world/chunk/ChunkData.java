package me.formercanuck.automationsurvival.world.chunk;

import me.formercanuck.automationsurvival.world.biomes.BiomeWeights;

/**
 * Holds voxel data and biome map for a chunk.
 */
public class ChunkData {
    public final int[] voxels;
    public final int chunkX, chunkZ;
    public final BiomeWeights[] biomeMap;

    public ChunkData(int[] voxels, int chunkX, int chunkZ, BiomeWeights[] biomeMap) {
        this.voxels = voxels;
        this.chunkX = chunkX;
        this.chunkZ = chunkZ;
        this.biomeMap = biomeMap;
    }
}