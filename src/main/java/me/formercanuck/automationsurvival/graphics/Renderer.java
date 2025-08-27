package me.formercanuck.automationsurvival.graphics;

import me.formercanuck.automationsurvival.Main;
import me.formercanuck.automationsurvival.graphics.mesh.Mesh;
import me.formercanuck.automationsurvival.graphics.shader.ShaderProgram;
import me.formercanuck.automationsurvival.graphics.shadow.ShadowMap;
import me.formercanuck.automationsurvival.world.Chunk;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL30;

import java.util.ArrayList;
import java.util.List;

public class Renderer {

    public List<Mesh> meshes = new ArrayList<>();
    public List<Mesh> toRemove = new ArrayList<>();

    private ShaderProgram shader;
    private ShaderProgram depthShader;
    private ShadowMap shadowMap;
    private Camera camera;

    private int width = 1280, height = 720;

    public void init() {
        shader = new ShaderProgram(
                "src/main/resources/shaders/voxel/shader.vert",
                "src/main/resources/shaders/voxel/shader.frag"
        );

        depthShader = new ShaderProgram(
                "src/main/resources/shaders/voxel/shadow_depth.vert",
                null // no fragment shader needed for depth pass
        );

        shadowMap = new ShadowMap();
        shadowMap.init(new Vector3f(-0.5f, -1.0f, -0.3f).normalize());

        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glClearColor(0.5f, 0.7f, 1.0f, 0.0f);

        shader.setVec3("uLightDir", new Vector3f(-0.5f, -1.0f, -0.3f).normalize());

        setTimeOfDay("midday");
    }

    public void setCamera(Camera camera) {
        this.camera = camera;
    }

    public void render() {
        renderShadowPass();
        renderMainPass();

        meshes.removeAll(toRemove);
        toRemove.clear();
    }

    private void renderShadowPass() {
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, shadowMap.fbo);
        GL11.glViewport(0, 0, shadowMap.SHADOW_WIDTH, shadowMap.SHADOW_HEIGHT);
        GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT);

        depthShader.use();
        depthShader.setMat4("uLightSpaceMatrix", shadowMap.lightSpaceMatrix);

        for (Mesh mesh : meshes) {
            depthShader.setMat4("uModel", mesh.getModelMatrix());
            mesh.render();
        }

        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);
    }

    public void setTimeOfDay(String time) {
        switch (time) {
            case "morning" -> {
                shader.setVec3("uLightDir", new Vector3f(-0.3f, -1.0f, 0.5f).normalize());
                GL11.glClearColor(0.8f, 0.6f, 0.4f, 1.0f);
                shader.setFloat("uAmbient", 0.3f);
            }
            case "midday" -> {
                shader.setVec3("uLightDir", new Vector3f(0.0f, -1.0f, 0.0f));
                GL11.glClearColor(0.5f, 0.7f, 1.0f, 1.0f);
                shader.setFloat("uAmbient", 0.1f);
            }
            case "night" -> {
                shader.setVec3("uLightDir", new Vector3f(0.0f, -0.2f, -1.0f).normalize());
                GL11.glClearColor(0.05f, 0.05f, 0.1f, 1.0f);
                shader.setFloat("uAmbient", 0.05f);
            }
        }
    }

    private void renderMainPass() {
        GL11.glViewport(0, 0, width, height);
        clear();

        shader.use();
        shader.setMat4("uProjection", camera.getProjection(width, height));
        shader.setMat4("uView", camera.getView());
        shader.setMat4("uLightSpaceMatrix", shadowMap.lightSpaceMatrix);
        setTimeOfDay("morning");

        GL13.glActiveTexture(GL13.GL_TEXTURE0);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, shadowMap.depthTexture);
        shader.setInt("shadowMap", 0);

        for (Chunk chunk : Main.getInstance().getWorld().loadedChunks.values()) {
            Mesh mesh = chunk.getMesh();
            if (mesh.isVisible()) {
                shader.setMat4("uModel", mesh.getModelMatrix());
                mesh.render();
            }
        }
    }

    public Camera getCamera() {
        return camera;
    }

    public void clear() {
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
    }
}