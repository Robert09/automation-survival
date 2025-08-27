package me.formercanuck.automationsurvival.graphics;

import me.formercanuck.automationsurvival.graphics.mesh.Mesh;
import me.formercanuck.automationsurvival.graphics.shader.ShaderProgram;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;

public class Renderer {

    public List<Mesh> meshes = new ArrayList<>();
    public List<Mesh> toRemove = new ArrayList<>();

    private ShaderProgram shader;
    private Camera camera;

    private int width = 1280, height = 720;

    public void init() {
        shader = new ShaderProgram("src/main/resources/shaders/voxel/shader.vert", "src/main/resources/shaders/voxel/shader.frag");
        GL11.glEnable(GL11.GL_DEPTH_TEST); // Enable depth testing
        GL11.glClearColor(0.5f, 0.7f, 1.0f, 0.0f); // Set clear color (blue sky)
        System.out.println(meshes.size());

        shader.setVec3("uLightDir", new Vector3f(-0.5f, -1.0f, -0.3f).normalize());
    }

    public void setCamera(Camera camera) {
        this.camera = camera;
    }

    public void render() {
        System.out.println(meshes.size());
        clear();
        shader.use();
        shader.setMat4("uProjection", camera.getProjection(width, height));
        shader.setMat4("uView", camera.getView());
        for (Mesh mesh : meshes) {
            shader.setMat4("uModel", mesh.getModelMatrix());
            mesh.render();
        }
        meshes.removeAll(toRemove);
        toRemove.clear();
    }

    public void clear() {
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
    }
}
