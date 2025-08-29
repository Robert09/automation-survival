package me.formercanuck.automationsurvival.graphics;

import org.joml.Matrix4f;
import org.joml.Vector3f;

public class Camera {

    public final Vector3f position = new Vector3f(0, 70, 0);
    public float yaw = -90f;   // looking down -Z
    public float pitch = 0f;
    public float fov = 70f;

    private final Vector3f front = new Vector3f(0, 0, -1);
    private final Vector3f up = new Vector3f(0, 1, 0);
    private final Vector3f right = new Vector3f(1, 0, 0);
    private final Vector3f worldUp = new Vector3f(0, 1, 0);

    public Matrix4f getView() {
        updateVectors();
        Vector3f center = new Vector3f(position).add(front);
        return new Matrix4f().lookAt(position, center, up);
    }

    public Matrix4f getProjection(int width, int height) {
        return new Matrix4f().perspective((float) Math.toRadians(fov), (float) width / height, 0.1f, 1000f);
    }

    private void updateVectors() {
        float cy = (float) Math.cos(Math.toRadians(yaw));
        float sy = (float) Math.sin(Math.toRadians(yaw));
        float cp = (float) Math.cos(Math.toRadians(pitch));
        float sp = (float) Math.sin(Math.toRadians(pitch));

        front.set(cy * cp, sp, sy * cp).normalize();
        right.set(front).cross(worldUp).normalize();
        up.set(right).cross(front).normalize();
    }

    // Simple WASD movement helpers
    public void moveForward(float amt) {position.fma(amt, front);}

    public void moveRight(float amt) {position.fma(amt, right);}

    public void moveUp(float amt) {position.y += amt;}
}
