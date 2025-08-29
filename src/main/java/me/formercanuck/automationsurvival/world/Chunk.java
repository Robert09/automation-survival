package me.formercanuck.automationsurvival.world;

import me.formercanuck.automationsurvival.graphics.mesh.Mesh;

/**
 * Represents a single chunk in the world.
 * Stores its mesh and visibility state.
 */
public class Chunk {
    private final int chunkX, chunkZ;
    private Mesh mesh;
    private boolean visible = true;

    public Chunk(int chunkX, int chunkZ) {
        this.chunkX = chunkX;
        this.chunkZ = chunkZ;
    }

    public int getChunkX() {
        return chunkX;
    }

    public int getChunkZ() {
        return chunkZ;
    }

    public void setMesh(Mesh mesh) {
        this.mesh = mesh;
    }

    public Mesh getMesh() {
        return mesh;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public boolean isVisible() {
        return visible;
    }
}