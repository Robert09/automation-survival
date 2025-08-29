package me.formercanuck.automationsurvival.world;

import me.formercanuck.automationsurvival.graphics.Renderer;
import me.formercanuck.automationsurvival.graphics.mesh.Mesh;
import me.formercanuck.automationsurvival.world.chunk.ChunkBuilder;
import me.formercanuck.automationsurvival.world.chunk.ChunkData;
import me.formercanuck.automationsurvival.world.chunk.GpuChunkGenerator;
import org.joml.Vector2i;
import org.joml.Vector3f;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class World {
    public final Map<Vector2i, Chunk> chunks = new HashMap<>();
    private final TerrainNoise terrainNoise;
    private final Renderer renderer;

    private final int chunkSize = 16;
    private final float voxelSize = 0.1f;
    private final int radius = 4;

    private GpuChunkGenerator gpuGen = new GpuChunkGenerator();

    public World(TerrainNoise terrainNoise, Renderer renderer) {
        this.terrainNoise = terrainNoise;
        this.renderer = renderer;
    }

    public void update(Vector3f camPos) {
        int camChunkX = (int) Math.floor(camPos.x / (chunkSize * voxelSize));
        int camChunkZ = (int) Math.floor(camPos.z / (chunkSize * voxelSize));

        Set<Vector2i> active = new HashSet<>();

        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                Vector2i coord = new Vector2i(camChunkX + dx, camChunkZ + dz);
                active.add(coord);

                if (!chunks.containsKey(coord)) {
                    Future<ChunkData> future = executor.submit(new ChunkBuilder(coord.x, coord.y, terrainNoise));
                    pending.add(future);
                }
            }
        }

        // Unload distant chunks
        chunks.keySet().removeIf(coord -> {
            if (!active.contains(coord)) {
                Chunk chunk = chunks.get(coord);
                chunk.setVisible(false);
                renderer.toRemove.add(chunk.getMesh());
                return true;
            }
            return false;
        });

        // Process finished chunk tasks
        while (!pending.isEmpty()) {
            Future<ChunkData> future = pending.peek();
            if (future.isDone()) {
                try {
                    ChunkData data = future.get();
                    Chunk chunk = new Chunk(data.chunkX, data.chunkZ);
                    Mesh mesh = new Mesh(data.vertices, data.indices);
                    chunk.setMesh(mesh);
                    chunks.put(new Vector2i(data.chunkX, data.chunkZ), chunk);
                    renderer.meshes.add(mesh);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                pending.poll();
            } else {
                break;
            }
        }
    }
}