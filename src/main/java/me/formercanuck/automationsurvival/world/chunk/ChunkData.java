package me.formercanuck.automationsurvival.world.chunk;

public class ChunkData {

    public final float[] vertices;
    public final int[] indices;
    public final int chunkX, chunkZ;

    public ChunkData(float[] vertices, int[] indices, int chunkX, int chunkZ) {
        this.vertices = vertices;
        this.indices = indices;
        this.chunkX = chunkX;
        this.chunkZ = chunkZ;
    }
}
