package me.formercanuck.automationsurvival.world;

import me.formercanuck.automationsurvival.graphics.Renderer;
import me.formercanuck.automationsurvival.util.Constants;
import org.joml.Math;
import org.joml.Vector2i;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class World {

    public Map<Vector2i, Chunk> chunks = new HashMap<>();

    private Renderer renderer;

    private int seed = 3812;

    private TerrainNoise terrainNoise;

    private int numChunksHalf = 16;

    public World(Renderer renderer) {
        this.terrainNoise = new TerrainNoise(seed);
        this.renderer = renderer;
        for (int x = -numChunksHalf; x <= numChunksHalf; x++) {
            for (int z = -numChunksHalf; z <= numChunksHalf; z++) {
                chunks.put(new Vector2i(x, z), new Chunk(terrainNoise, renderer, x, z));
            }
        }
    }

    public void update(double deltaTime) {
        Vector3f cameraPos = renderer.getCamera().position;
        int camChunkX = (int) Math.floor(cameraPos.x / (Constants.CHUNK_SIZE * Constants.VOXEL_SIZE));
        int camChunkZ = (int) Math.floor(cameraPos.z / (Constants.CHUNK_SIZE * Constants.VOXEL_SIZE));

        Set<Vector2i> active = new HashSet<>();

        int renderRadius = Constants.RENDER_DISTANCE_CHUNKS;

        for (int dx = -renderRadius; dx <= renderRadius; dx++) {
            for (int dz = -renderRadius; dz <= renderRadius; dz++) {
                Vector2i coord = new Vector2i(camChunkX + dx, camChunkZ + dz);
                active.add(coord);

                if (!chunks.containsKey(coord)) {
                    Chunk chunk = new Chunk(terrainNoise, renderer, coord.x, coord.y);
                    chunks.put(coord, chunk);
                }
            }
        }

        // Unload distant chunks
        chunks.keySet().removeIf(coord -> {
            if (!active.contains(coord)) {
                Chunk chunk = chunks.get(coord);
                chunk.setVisible(false);
                return true;
            }
            return false;
        });

    }

    public Chunk getChunkAt(int chunkX, int chunkZ) {
        for (Chunk chunk : chunks.values()) {
            if (chunk.chunkX == chunkX && chunk.chunkZ == chunkZ) {
                return chunk;
            }
        }
        return null;
    }
}
