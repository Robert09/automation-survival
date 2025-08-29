package me.formercanuck.automationsurvival.world;

import me.formercanuck.automationsurvival.graphics.Renderer;
import me.formercanuck.automationsurvival.graphics.mesh.Mesh;
import me.formercanuck.automationsurvival.world.chunk.GpuChunkGenerator;
import me.formercanuck.automationsurvival.world.chunk.MeshBuilder;
import org.joml.Vector2i;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class World {
    public final Map<Vector2i, Chunk> chunks = new HashMap<>();
    private final TerrainNoise terrainNoise;
    private final Renderer renderer;
    private final GpuChunkGenerator gpuGen;

    private final int chunkSize = 16;
    private final float voxelSize = 0.1f;
    private final int chunkHeight = 128;
    private final int radius = 8;

    private int lastChunkX = Integer.MIN_VALUE;
    private int lastChunkZ = Integer.MIN_VALUE;

    public World(TerrainNoise terrainNoise, Renderer renderer) {
        this.terrainNoise = terrainNoise;
        this.renderer = renderer;
        this.gpuGen = new GpuChunkGenerator();
    }

    public void update(Vector3f camPos) {
        int camChunkX = (int) Math.floor(camPos.x / (chunkSize * voxelSize));
        int camChunkZ = (int) Math.floor(camPos.z / (chunkSize * voxelSize));

        if (camChunkX == lastChunkX && camChunkZ == lastChunkZ) return; // No movement

        lastChunkX = camChunkX;
        lastChunkZ = camChunkZ;

        Set<Vector2i> active = new HashSet<>();

        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                Vector2i coord = new Vector2i(camChunkX + dx, camChunkZ + dz);
                active.add(coord);

                if (!chunks.containsKey(coord)) {
                    int[] voxelData = gpuGen.generateChunk(coord.x, coord.y);
                    Mesh mesh = MeshBuilder.buildMeshFromVoxelData(voxelData, coord.x, coord.y, chunkSize, chunkHeight, voxelSize);
                    Chunk chunk = new Chunk(coord.x, coord.y);
                    chunk.setMesh(mesh);
                    chunks.put(coord, chunk);
                    renderer.meshes.add(mesh);
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
    }
}