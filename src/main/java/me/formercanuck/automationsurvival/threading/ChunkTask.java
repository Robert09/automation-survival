package me.formercanuck.automationsurvival.threading;

import me.formercanuck.automationsurvival.graphics.Renderer;
import me.formercanuck.automationsurvival.world.Chunk;
import me.formercanuck.automationsurvival.world.TerrainNoise;

import java.util.concurrent.Callable;

public class ChunkTask implements Callable<Chunk> {
    private final int chunkX, chunkZ;
    private final TerrainNoise terrainNoise;
    private final Renderer renderer;

    public ChunkTask(int chunkX, int chunkZ, TerrainNoise terrainNoise, Renderer renderer) {
        this.chunkX = chunkX;
        this.chunkZ = chunkZ;
        this.terrainNoise = terrainNoise;
        this.renderer = renderer;
    }

    @Override
    public Chunk call() {
        return new Chunk(terrainNoise, renderer, chunkX, chunkZ);
    }
}
