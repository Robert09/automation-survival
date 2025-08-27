package me.formercanuck.automationsurvival.graphics.mesh;

import me.formercanuck.automationsurvival.graphics.Renderable;
import me.formercanuck.automationsurvival.util.Color;
import me.formercanuck.automationsurvival.world.Face;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.system.MemoryUtil;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.*;

public class Mesh implements Renderable {
    private int vaoId, vboId, eboId, vertexCount;

    private boolean visible = true;

    private float x, y, z;

    private final float h = 0.5f; // half-size

    public int[] backIndices = {6, 7, 4, 6, 4, 5}; // back (-Z)
    public int[] frontIndices = {2, 0, 3, 2, 1, 0}; // front (+Z)
    public int[] leftIndices = {3, 4, 7, 3, 0, 4}; // left (-X)
    public int[] rightIndices = {1, 6, 5, 1, 2, 6}; // right (+X)
    public int[] topIndices = {1, 4, 0, 1, 5, 4}; // top (+Y)
    public int[] bottomIndices = {6, 3, 7, 6, 2, 3}; // bottom (-Y)

    private final float[] vertices = {
            -h, h, h, 1, 0, 0, // Front top-left     0
            h, h, h, 0, 1, 0,  // Front top-right    1
            h, -h, h, 0, 0, 1, // Front bottom-right 2
            -h, -h, h, 1, 1, 0,// Front bottom-left  3
            -h, h, -h, 1, 0, 1,// Back top-left      4
            h, h, -h, 0, 1, 1, // Back top-right     5
            h, -h, -h, 1, 1, 1,// Back bottom-right  6
            -h, -h, -h, 0, 0, 0// Back bottom-left   7
    };

    public Mesh() {}

    public static float[] normalizeRGB(int r, int g, int b) {
        return new float[]{
                r / 255f,
                g / 255f,
                b / 255f
        };
    }

    public Mesh createCube(float h, Set<Face> visibleFaces, Color colorIn) {
        float[] color = colorIn.toArray();
        float[] vertices = {
                -h, h, h, color[0], color[1], color[2], // Front top-left     0
                h, h, h, color[0], color[1], color[2],  // Front top-right    1
                h, -h, h, color[0], color[1], color[2], // Front bottom-right 2
                -h, -h, h, color[0], color[1], color[2],// Front bottom-left  3
                -h, h, -h, color[0], color[1], color[2],// Back top-left      4
                h, h, -h, color[0], color[1], color[2], // Back top-right     5
                h, -h, -h, color[0], color[1], color[2],// Back bottom-right  6
                -h, -h, -h, color[0], color[1], color[2]// Back bottom-left   7
        };

        List<Integer> indexList = new ArrayList<>();
        if (visibleFaces.contains(Face.FRONT)) indexList.addAll(toList(frontIndices));
        if (visibleFaces.contains(Face.BACK)) indexList.addAll(toList(backIndices));
        if (visibleFaces.contains(Face.LEFT)) indexList.addAll(toList(leftIndices));
        if (visibleFaces.contains(Face.RIGHT)) indexList.addAll(toList(rightIndices));
        if (visibleFaces.contains(Face.TOP)) indexList.addAll(toList(topIndices));
        if (visibleFaces.contains(Face.BOTTOM)) indexList.addAll(toList(bottomIndices));

        int[] indices = indexList.stream().mapToInt(i -> i).toArray();
        return new Mesh(vertices, indices);
    }

    private List<Integer> toList(int[] array) {
        List<Integer> list = new ArrayList<>(array.length);
        for (int i : array) list.add(i);
        return list;
    }

    public Mesh(float[] vertices, int[] indices) {
        vertexCount = indices.length;

        vaoId = glGenVertexArrays();
        glBindVertexArray(vaoId);

        // Vertex buffer
        FloatBuffer vbuf = MemoryUtil.memAllocFloat(vertices.length).put(vertices).flip();
        vboId = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vboId);
        glBufferData(GL_ARRAY_BUFFER, vbuf, GL_STATIC_DRAW);

        // Index buffer
        IntBuffer ibuf = MemoryUtil.memAllocInt(indices.length).put(indices).flip();
        eboId = glGenBuffers();
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, eboId);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, ibuf, GL_STATIC_DRAW);

        // layout (location=0) position, (location=1) color
        int stride = 9 * Float.BYTES;
        glVertexAttribPointer(0, 3, GL_FLOAT, false, stride, 0L); // position
        glEnableVertexAttribArray(0);
        glVertexAttribPointer(1, 3, GL_FLOAT, false, stride, 3L * Float.BYTES); // color
        glEnableVertexAttribArray(1);
        glVertexAttribPointer(2, 3, GL_FLOAT, false, stride, 6L * Float.BYTES); // normal
        glEnableVertexAttribArray(2);

        MemoryUtil.memFree(vbuf);
        MemoryUtil.memFree(ibuf);
        glBindVertexArray(0);
    }

    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
    }

    public float getZ() {
        return z;
    }

    public void setZ(float z) {
        this.z = z;
    }

    public void render() {
        glBindVertexArray(vaoId);
        glDrawElements(GL_TRIANGLES, vertexCount, GL_UNSIGNED_INT, 0);
        glBindVertexArray(0);
    }

    public void delete() {
        glDeleteBuffers(vboId);
        glDeleteBuffers(eboId);
        glDeleteVertexArrays(vaoId);
    }

    @Override
    public Mesh getMesh() {
        return this;
    }

    @Override
    public Matrix4f getModelMatrix() {
        return new Matrix4f().translation(new Vector3f(x, y, z)); // place it;
    }

    public void setPosition(float v, float v1, float v2) {
        this.x = v;
        this.y = v1;
        this.z = v2;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }
}
