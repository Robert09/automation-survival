package me.formercanuck.automationsurvival;

import me.formercanuck.automationsurvival.graphics.Camera;
import me.formercanuck.automationsurvival.graphics.Renderer;
import me.formercanuck.automationsurvival.world.TerrainNoise;
import me.formercanuck.automationsurvival.world.World;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;

public class Main {

    private long window;
    private int width = 1280, height = 720;

    private Renderer renderer;
    private World world;
    private Camera camera;

    public void run() {
        initWindow();
        GL.createCapabilities();

        glEnable(GL_DEPTH_TEST);
        glEnable(GL_CULL_FACE);
        glCullFace(GL_BACK);
        glFrontFace(GL_CCW); // matches our indices

        renderer = new Renderer();
        renderer.init();
        camera = new Camera();
        renderer.setCamera(camera);

        TerrainNoise terrainNoise = new TerrainNoise(3812);

        world = new World(terrainNoise, renderer);

        // basic input state
        glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_DISABLED);
        double[] lastMouse = {width / 2.0, height / 2.0};
        glfwSetCursorPos(window, lastMouse[0], lastMouse[1]);

        double lastTime = glfwGetTime();
        while (!glfwWindowShouldClose(window)) {
            double currentTime = glfwGetTime();
            float deltaTime = (float) (currentTime - lastTime);
            lastTime = currentTime;

            glfwPollEvents();
            updateCamera(camera, deltaTime);                        // WASD + QE for up/down
            updateMouseLook(camera, lastMouse);          // mouse yaw/pitch

            world.update(camera.position);

            glViewport(0, 0, width, height);
            renderer.render();
            glfwSwapBuffers(window);
        }

        glfwDestroyWindow(window);
        glfwTerminate();
    }

    private void initWindow() {
        GLFWErrorCallback.createPrint(System.err).set();
        if (!glfwInit()) throw new IllegalStateException("GLFW init failed");
        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);
        window = glfwCreateWindow(width, height, "Voxel Renderer", 0, 0);
        if (window == 0) throw new RuntimeException("Window creation failed");

        // center window
        long monitor = glfwGetPrimaryMonitor();
        GLFWVidMode mode = glfwGetVideoMode(monitor);
        if (mode != null)
            glfwSetWindowPos(window, (mode.width() - width) / 2, (mode.height() - height) / 2);

        glfwMakeContextCurrent(window);
        glfwSwapInterval(1);
        glfwShowWindow(window);

        // resize callback
        glfwSetFramebufferSizeCallback(window, (w, newW, newH) -> {
            width = newW;
            height = newH;
        });

        // close on ESC
        glfwSetKeyCallback(window, (w, key, sc, act, mods) -> {
            if (key == GLFW_KEY_ESCAPE && act == GLFW_RELEASE) glfwSetWindowShouldClose(w, true);
        });
    }

    // --- controls: WASD + Q/E, shift to speed up ---
    private void updateCamera(Camera cam, double dt) {
        float base = 0.1f;
        if (glfwGetKey(window, GLFW_KEY_LEFT_SHIFT) == GLFW_PRESS) base *= 4;

        base *= (float) (dt * 60); // frame-rate normalize

        if (glfwGetKey(window, GLFW_KEY_W) == GLFW_PRESS) cam.moveForward(base);
        if (glfwGetKey(window, GLFW_KEY_S) == GLFW_PRESS) cam.moveForward(-base);
        if (glfwGetKey(window, GLFW_KEY_D) == GLFW_PRESS) cam.moveRight(base);
        if (glfwGetKey(window, GLFW_KEY_A) == GLFW_PRESS) cam.moveRight(-base);
        if (glfwGetKey(window, GLFW_KEY_E) == GLFW_PRESS) cam.moveUp(base);
        if (glfwGetKey(window, GLFW_KEY_Q) == GLFW_PRESS) cam.moveUp(-base);
    }

    private void updateMouseLook(Camera cam, double[] last) {
        double[] x = new double[1], y = new double[1];
        glfwGetCursorPos(window, x, y);
        double dx = x[0] - last[0];
        double dy = y[0] - last[1];
        last[0] = x[0];
        last[1] = y[0];

        float sens = 0.1f;
        cam.yaw += dx * sens;
        cam.pitch -= dy * sens;
        cam.pitch = Math.max(-89f, Math.min(89f, cam.pitch));
    }

    private Main() {}

    private static Main instance;

    public static Main getInstance() {
        if (instance == null) instance = new Main();
        return instance;
    }

    public World getWorld() {
        return world;
    }

    public static void main(String[] args) {getInstance().run();}
}
