package me.formercanuck.automationsurvival.world;

import me.formercanuck.automationsurvival.graphics.Renderer;
import me.formercanuck.automationsurvival.graphics.mesh.Mesh;
import me.formercanuck.automationsurvival.world.chunk.ChunkData;
import me.formercanuck.automationsurvival.world.chunk.ChunkTask;
import me.formercanuck.automationsurvival.world.chunk.MeshBuilder;
import org.joml.Vector2i;
import org.joml.Vector3f;

import java.util.*;
import java.util.concurrent.*;

public class World {
    public final Map<Vector2i, Chunk> chunks = new ConcurrentHashMap<>();
    private final Queue<ChunkData> readyChunks = new ConcurrentLinkedQueue<>();
    private final TerrainNoise terrainNoise;
    private final Renderer renderer;
    private final ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

    private final int chunkSize = 16;
    private final float voxelSize = 0.1f;
    private final int chunkHeight = 128;
    private final int radius = 16;

    private int lastChunkX = Integer.MIN_VALUE;
    private int lastChunkZ = Integer.MIN_VALUE;

    public World(TerrainNoise terrainNoise, Renderer renderer) {
        this.terrainNoise = terrainNoise;
        this.renderer = renderer;
    }

    public void update(Vector3f camPos) {
        int camChunkX = (int) Math.floor(camPos.x / (chunkSize * voxelSize));
        int camChunkZ = (int) Math.floor(camPos.z / (chunkSize * voxelSize));

        if (camChunkX == lastChunkX && camChunkZ == lastChunkZ) {
            processReadyChunks();
            return;
        }

        lastChunkX = camChunkX;
        lastChunkZ = camChunkZ;

        Set<Vector2i> active = new HashSet<>();

        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                Vector2i coord = new Vector2i(camChunkX + dx, camChunkZ + dz);
                active.add(coord);

                if (!chunks.containsKey(coord)) {
                    ChunkTask task = new ChunkTask(coord.x, coord.y, terrainNoise, chunkSize, chunkHeight, readyChunks::add);
                    executor.submit(task);
                }
            }
        }

        chunks.keySet().removeIf(coord -> {
            if (!active.contains(coord)) {
                Chunk chunk = chunks.get(coord);
                chunk.setVisible(false);
                renderer.toRemove.add(chunk.getMesh());
                return true;
            }
            return false;
        });

        processReadyChunks();
    }

    private void processReadyChunks() {
        while (!readyChunks.isEmpty()) {
            ChunkData data = readyChunks.poll();
            Mesh mesh = MeshBuilder.buildMeshFromVoxelData(data.voxels, data.chunkX, data.chunkZ, chunkSize, chunkHeight, voxelSize);
            Chunk chunk = new Chunk(data.chunkX, data.chunkZ);
            chunk.setMesh(mesh);
            chunks.put(new Vector2i(data.chunkX, data.chunkZ), chunk);
            renderer.meshes.add(mesh);
        }
    }
}