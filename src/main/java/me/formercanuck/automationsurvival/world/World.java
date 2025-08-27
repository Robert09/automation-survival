package me.formercanuck.automationsurvival.world;

import me.formercanuck.automationsurvival.graphics.Renderer;
import org.joml.Math;

import java.util.ArrayList;
import java.util.List;

public class World {

    public List<Chunk> chunks;

    private Renderer renderer;

    private int seed = 3812;

    private TerrainNoise terrainNoise;

    private int numChunksHalf = 16;

    public World(Renderer renderer) {
        this.terrainNoise = new TerrainNoise(seed);
        this.renderer = renderer;
        // Initialize chunks list and load/generate chunks as needed
        chunks = new ArrayList<>();
        for (int x = -numChunksHalf; x <= numChunksHalf; x++) {
            for (int z = -numChunksHalf; z <= numChunksHalf; z++) {
                chunks.add(new Chunk(terrainNoise, renderer, x, z));
            }
        }
    }

    public void update(double deltaTime) {
        int chunkX = Math.round(renderer.getCamera().position.x / Chunk.CHUNK_SIZE);
        int chunkZ = Math.round(renderer.getCamera().position.z / Chunk.CHUNK_SIZE);

        for (Chunk chunk : chunks) {
            int distX = chunk.chunkX - chunkX;
            int distZ = chunk.chunkZ - chunkZ;
            int distance = Math.abs(distX) + Math.abs(distZ);
            boolean shouldBeVisible = distance <= 1;
            System.out.println("Chunk (" + chunk.chunkX + ", " + chunk.chunkZ + ") Distance: " + distance + " Visible: " + shouldBeVisible);
            chunk.setVisible(shouldBeVisible);
        }
    }

    public Chunk getChunkAt(int chunkX, int chunkZ) {
        for (Chunk chunk : chunks) {
            if (chunk.chunkX == chunkX && chunk.chunkZ == chunkZ) {
                return chunk;
            }
        }
        return null;
    }
}
