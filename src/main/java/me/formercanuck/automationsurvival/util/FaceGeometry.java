package me.formercanuck.automationsurvival.util;

import me.formercanuck.automationsurvival.world.Face;
import org.joml.Vector3f;

public class FaceGeometry {

    public static float[] getFaceVertices(Face face, Vector3f pos, float h, float[] color) {
        float x = pos.x, y = pos.y, z = pos.z;
        float r = color[0], g = color[1], b = color[2];
        float nx = 0, ny = 0, nz = 0;

        switch (face) {
            case FRONT:
                nx = 0;
                ny = 0;
                nz = 1;
                break;
            case BACK:
                nx = 0;
                ny = 0;
                nz = -1;
                break;
            case LEFT:
                nx = -1;
                ny = 0;
                nz = 0;
                break;
            case RIGHT:
                nx = 1;
                ny = 0;
                nz = 0;
                break;
            case TOP:
                nx = 0;
                ny = 1;
                nz = 0;
                break;
            case BOTTOM:
                nx = 0;
                ny = -1;
                nz = 0;
                break;
        }

        return switch (face) {
            case FRONT -> new float[]{
                    x - h, y - h, z + h, r, g, b, nx, ny, nz,
                    x + h, y - h, z + h, r, g, b, nx, ny, nz,
                    x + h, y + h, z + h, r, g, b, nx, ny, nz,
                    x - h, y + h, z + h, r, g, b, nx, ny, nz
            };
            case BACK -> new float[]{
                    x + h, y - h, z - h, r, g, b, nx, ny, nz,
                    x - h, y - h, z - h, r, g, b, nx, ny, nz,
                    x - h, y + h, z - h, r, g, b, nx, ny, nz,
                    x + h, y + h, z - h, r, g, b, nx, ny, nz
            };
            case LEFT -> new float[]{
                    x - h, y - h, z - h, r, g, b, nx, ny, nz,
                    x - h, y - h, z + h, r, g, b, nx, ny, nz,
                    x - h, y + h, z + h, r, g, b, nx, ny, nz,
                    x - h, y + h, z - h, r, g, b, nx, ny, nz
            };
            case RIGHT -> new float[]{
                    x + h, y - h, z + h, r, g, b, nx, ny, nz,
                    x + h, y - h, z - h, r, g, b, nx, ny, nz,
                    x + h, y + h, z - h, r, g, b, nx, ny, nz,
                    x + h, y + h, z + h, r, g, b, nx, ny, nz
            };
            case TOP -> new float[]{
                    x - h, y + h, z + h, r, g, b, nx, ny, nz,
                    x + h, y + h, z + h, r, g, b, nx, ny, nz,
                    x + h, y + h, z - h, r, g, b, nx, ny, nz,
                    x - h, y + h, z - h, r, g, b, nx, ny, nz
            };
            case BOTTOM -> new float[]{
                    x - h, y - h, z - h, r, g, b, nx, ny, nz,
                    x + h, y - h, z - h, r, g, b, nx, ny, nz,
                    x + h, y - h, z + h, r, g, b, nx, ny, nz,
                    x - h, y - h, z + h, r, g, b, nx, ny, nz
            };
        };
    }

    public static int[] getFaceIndices(int offset) {
        return new int[]{
                offset, offset + 1, offset + 2,
                offset, offset + 2, offset + 3
        };
    }
}
