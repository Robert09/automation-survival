package me.formercanuck.automationsurvival.threading;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class ChunkWorker {

    private final ExecutorService pool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

    public Future<ChunkMeshData> submit(int chunkX, int chunkZ) {
        return pool.submit(() -> {
            // 1) generate voxel data
            VoxelData data = ChunkGenerator.generate(chunkX, chunkZ);

            // 2) build mesh (positions, indices, uvs)
            return MeshBuilder.build(data);
        });
    }

    public void shutdown() {
        pool.shutdown();
    }
}
