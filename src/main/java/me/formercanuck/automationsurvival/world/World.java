package me.formercanuck.automationsurvival.world;

import me.formercanuck.automationsurvival.graphics.Renderer;
import me.formercanuck.automationsurvival.threading.ChunkTask;
import me.formercanuck.automationsurvival.util.Constants;
import org.joml.Math;
import org.joml.Vector2i;
import org.joml.Vector3f;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class World {

    public Map<Vector2i, Chunk> chunks = new HashMap<>();

    private Renderer renderer;

    private int seed = 3812;

    private TerrainNoise terrainNoise;

    private int numChunksHalf = 16;

    private ExecutorService chunkExecutor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    private Queue<Future<Chunk>> pendingChunks = new ConcurrentLinkedQueue<>();

    public World(Renderer renderer) {
        this.terrainNoise = new TerrainNoise(seed);
        this.renderer = renderer;
        for (int x = -numChunksHalf; x <= numChunksHalf; x++) {
            for (int z = -numChunksHalf; z <= numChunksHalf; z++) {
                Vector2i coord = new Vector2i(x, z);
                if (!chunks.containsKey(coord)) {
                    Future<Chunk> future = chunkExecutor.submit(new ChunkTask(x, z, terrainNoise, renderer));
                    pendingChunks.add(future);
                }
            }
        }
    }

    public void update(double deltaTime) {
        while (!pendingChunks.isEmpty()) {
            Future<Chunk> future = pendingChunks.peek();
            if (future.isDone()) {
                try {
                    Chunk chunk = future.get();
                    chunks.put(new Vector2i(chunk.getChunkX(), chunk.getChunkZ()), chunk);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                pendingChunks.poll();
            } else {
                break;
            }
        }
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
                    Future<Chunk> future = chunkExecutor.submit(new ChunkTask(coord.x, coord.y, terrainNoise, renderer));
                    pendingChunks.add(future);
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
