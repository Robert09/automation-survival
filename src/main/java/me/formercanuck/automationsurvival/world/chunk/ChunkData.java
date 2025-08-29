package me.formercanuck.automationsurvival.world.chunk;

public class ChunkData {
    public final int[] voxels;
    public final int chunkX, chunkZ;

    public ChunkData(int[] voxels, int chunkX, int chunkZ) {
        this.voxels = voxels;
        this.chunkX = chunkX;
        this.chunkZ = chunkZ;
    }
}