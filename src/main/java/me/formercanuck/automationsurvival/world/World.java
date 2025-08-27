package me.formercanuck.automationsurvival.world;

import me.formercanuck.automationsurvival.graphics.Renderer;

import java.util.ArrayList;
import java.util.List;

public class World {

    public List<Chunk> chunks;

    private Renderer renderer;

    private int seed = 3812;

    private TerrainNoise terrainNoise;

    public World(Renderer renderer) {
        this.terrainNoise = new TerrainNoise(seed);
        this.renderer = renderer;
        // Initialize chunks list and load/generate chunks as needed
        chunks = new ArrayList<>();
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                chunks.add(new Chunk(terrainNoise, renderer, x, z));
            }
        }
    }
}
