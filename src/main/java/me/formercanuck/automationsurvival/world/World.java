package me.formercanuck.automationsurvival.world;

import me.formercanuck.automationsurvival.graphics.Renderer;

import java.util.ArrayList;
import java.util.List;

public class World {

    public List<Chunk> chunks;

    private Renderer renderer;

    private int seed = 3812;

    private TerrainNoise terrainNoise;

    private int numChunksHalf = 8;

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
}
