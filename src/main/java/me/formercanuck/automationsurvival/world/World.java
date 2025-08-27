package me.formercanuck.automationsurvival.world;

import me.formercanuck.automationsurvival.graphics.Renderer;
import me.formercanuck.automationsurvival.util.Constants;
import org.joml.Math;
import org.joml.Vector2i;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.Map;

public class World {

    public Map<Vector2i, Chunk> loadedChunks = new HashMap<>();

    private Renderer renderer;

    private int seed = 3812;

    private TerrainNoise terrainNoise;

    private int numChunksHalf = 16;

    public World(Renderer renderer) {
        this.terrainNoise = new TerrainNoise(seed);
        this.renderer = renderer;
        for (int x = -numChunksHalf; x <= numChunksHalf; x++) {
            for (int z = -numChunksHalf; z <= numChunksHalf; z++) {
                loadedChunks.put(new Vector2i(x, z), new Chunk(terrainNoise, renderer, x, z));
            }
        }
    }

    public void update(double deltaTime) {
        int chunkSize = 16;
        Vector3f camPos = renderer.getCamera().position;
        int camChunkX = (int) Math.floor(camPos.x / (chunkSize * Constants.VOXEL_SIZE));
        int camChunkZ = (int) Math.floor(camPos.z / (chunkSize * Constants.VOXEL_SIZE));

        int R = Constants.RENDER_DISTANCE_CHUNKS; // radius in chunks

        for (int dx = -R; dx <= R; dx++) {
            for (int dz = -R; dz <= R; dz++) {
                Vector2i chunkCoord = new Vector2i(camChunkX + dx, camChunkZ + dz);
                if (!loadedChunks.containsKey(chunkCoord)) {
                    Chunk chunk = new Chunk(terrainNoise, renderer, chunkCoord.x, chunkCoord.y);
                    loadedChunks.put(chunkCoord, chunk);
                }
            }
        }

        loadedChunks.entrySet().removeIf(entry -> {
            Vector2i coord = entry.getKey();
            int dx = Math.abs(coord.x - camChunkX);
            int dz = Math.abs(coord.y - camChunkZ);
            return dx > R || dz > R;
        });
    }

    public Chunk getChunkAt(int chunkX, int chunkZ) {
        for (Chunk chunk : loadedChunks.values()) {
            if (chunk.chunkX == chunkX && chunk.chunkZ == chunkZ) {
                return chunk;
            }
        }
        return null;
    }
}
