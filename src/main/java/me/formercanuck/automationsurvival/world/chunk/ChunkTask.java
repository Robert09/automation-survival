package me.formercanuck.automationsurvival.world.chunk;

import me.formercanuck.automationsurvival.world.TerrainNoise;

public class ChunkTask implements Runnable {
    private final int chunkX, chunkZ;
    private final TerrainNoise terrainNoise;
    private final int chunkSize, chunkHeight;
    private final ChunkCallback callback;

    public interface ChunkCallback {
        void onChunkReady(ChunkData data);
    }

    public ChunkTask(int chunkX, int chunkZ, TerrainNoise terrainNoise, int chunkSize, int chunkHeight, ChunkCallback callback) {
        this.chunkX = chunkX;
        this.chunkZ = chunkZ;
        this.terrainNoise = terrainNoise;
        this.chunkSize = chunkSize;
        this.chunkHeight = chunkHeight;
        this.callback = callback;
    }

    @Override
    public void run() {
        int[] voxels = new int[chunkSize * chunkHeight * chunkSize];

        for (int x = 0; x < chunkSize; x++) {
            for (int z = 0; z < chunkSize; z++) {
                int worldX = chunkX * chunkSize + x;
                int worldZ = chunkZ * chunkSize + z;
                float height = terrainNoise.getHeight(worldX, worldZ) * 10f + 40f;

                for (int y = 0; y < chunkHeight; y++) {
                    int index = x + y * chunkSize + z * chunkSize * chunkHeight;
                    voxels[index] = y < height ? 1 : 0;
                }
            }
        }

        callback.onChunkReady(new ChunkData(voxels, chunkX, chunkZ));
    }
}
