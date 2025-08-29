package me.formercanuck.automationsurvival.world;

import me.formercanuck.automationsurvival.graphics.Renderer;
import me.formercanuck.automationsurvival.graphics.mesh.Mesh;
import me.formercanuck.automationsurvival.world.biomes.BiomeGenerator;
import me.formercanuck.automationsurvival.world.chunk.ChunkData;
import me.formercanuck.automationsurvival.world.chunk.ChunkTask;
import me.formercanuck.automationsurvival.world.chunk.MeshBuilder;
import org.joml.Vector2i;
import org.joml.Vector3f;

import java.util.HashSet;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Manages chunk streaming, threaded generation, and rendering.
 * Uses worker threads for terrain generation and queues completed chunks for main-thread mesh upload.
 */
public class World {
    public final Map<Vector2i, Chunk> chunks = new ConcurrentHashMap<>();
    private final Queue<ChunkData> readyChunks = new ConcurrentLinkedQueue<>();
    private final TerrainNoise terrainNoise;
    private final BiomeGenerator biomeGenerator;
    private final Renderer renderer;
    private final ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

    private final int chunkSize = 16;
    private final float voxelSize = 0.1f;
    private final int chunkHeight = 128;
    private final int radius = 16;

    private int lastChunkX = Integer.MIN_VALUE;
    private int lastChunkZ = Integer.MIN_VALUE;

    public World(TerrainNoise terrainNoise, BiomeGenerator biomeGenerator, Renderer renderer) {
        this.terrainNoise = terrainNoise;
        this.biomeGenerator = biomeGenerator;
        this.renderer = renderer;
    }

    /**
     * Called once per frame. Updates visible chunks based on camera position.
     * Generates new chunks if the camera moved to a new chunk.
     */
    public void update(Vector3f camPos) {
        int camChunkX = (int) Math.floor(camPos.x / (chunkSize * voxelSize));
        int camChunkZ = (int) Math.floor(camPos.z / (chunkSize * voxelSize));

        // Only regenerate chunks if camera moved to a new chunk
        if (camChunkX == lastChunkX && camChunkZ == lastChunkZ) {
            processReadyChunks();
            return;
        }

        lastChunkX = camChunkX;
        lastChunkZ = camChunkZ;

        Set<Vector2i> active = new HashSet<>();

        // Queue generation tasks for all chunks within radius
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                Vector2i coord = new Vector2i(camChunkX + dx, camChunkZ + dz);
                active.add(coord);

                if (!chunks.containsKey(coord)) {
                    ChunkTask task = new ChunkTask(coord.x, coord.y, terrainNoise, biomeGenerator, chunkSize, chunkHeight, readyChunks::add);
                    executor.submit(task);
                }
            }
        }

        // Unload chunks outside the active radius
        chunks.keySet().removeIf(coord -> {
            if (!active.contains(coord)) {
                Chunk chunk = chunks.get(coord);
                chunk.setVisible(false);
                renderer.toRemove.add(chunk.getMesh());
                return true;
            }
            return false;
        });

        // Upload any completed chunks from worker threads
        processReadyChunks();
    }

    /**
     * Called on the main thread. Converts completed voxel data into meshes and adds them to the renderer.
     */
    private void processReadyChunks() {
        while (!readyChunks.isEmpty()) {
            ChunkData data = readyChunks.poll();
            Mesh mesh = MeshBuilder.buildMeshFromVoxelData(
                    data.voxels,
                    data.biomeMap, // ← required for biome shading
                    data.chunkX,
                    data.chunkZ,
                    chunkSize,
                    chunkHeight,
                    voxelSize
            );
            Chunk chunk = new Chunk(data.chunkX, data.chunkZ);
            chunk.setMesh(mesh);
            chunks.put(new Vector2i(data.chunkX, data.chunkZ), chunk);
            renderer.meshes.add(mesh);
        }
    }
}