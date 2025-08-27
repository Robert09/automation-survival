package me.formercanuck.automationsurvival.util;

import me.formercanuck.automationsurvival.world.Face;
import org.joml.Vector3f;

public class FaceGeometry {

    public static float[] getFaceVertices(Face face, Vector3f pos, float h, float[] color) {
        float x = pos.x, y = pos.y, z = pos.z;
        float r = color[0], g = color[1], b = color[2];

        switch (face) {
            case FRONT: // +Z
                return new float[]{
                        x - h, y - h, z + h, r, g, b,
                        x + h, y - h, z + h, r, g, b,
                        x + h, y + h, z + h, r, g, b,
                        x - h, y + h, z + h, r, g, b
                };
            case BACK: // -Z
                return new float[]{
                        x + h, y - h, z - h, r, g, b,
                        x - h, y - h, z - h, r, g, b,
                        x - h, y + h, z - h, r, g, b,
                        x + h, y + h, z - h, r, g, b
                };
            case LEFT: // -X
                return new float[]{
                        x - h, y - h, z - h, r, g, b,
                        x - h, y - h, z + h, r, g, b,
                        x - h, y + h, z + h, r, g, b,
                        x - h, y + h, z - h, r, g, b
                };
            case RIGHT: // +X
                return new float[]{
                        x + h, y - h, z + h, r, g, b,
                        x + h, y - h, z - h, r, g, b,
                        x + h, y + h, z - h, r, g, b,
                        x + h, y + h, z + h, r, g, b
                };
            case TOP: // +Y
                return new float[]{
                        x - h, y + h, z + h, r, g, b,
                        x + h, y + h, z + h, r, g, b,
                        x + h, y + h, z - h, r, g, b,
                        x - h, y + h, z - h, r, g, b
                };
            case BOTTOM: // -Y
                return new float[]{
                        x - h, y - h, z - h, r, g, b,
                        x + h, y - h, z - h, r, g, b,
                        x + h, y - h, z + h, r, g, b,
                        x - h, y - h, z + h, r, g, b
                };
            default:
                throw new IllegalArgumentException("Unknown face: " + face);
        }
    }

    public static int[] getFaceIndices(int offset) {
        return new int[]{
                offset, offset + 1, offset + 2,
                offset, offset + 2, offset + 3
        };
    }
}