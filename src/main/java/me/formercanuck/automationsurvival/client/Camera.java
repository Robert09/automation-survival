package me.formercanuck.automationsurvival.client;

import org.joml.Matrix4f;
import org.joml.Vector3f;

public class Camera {

    private Vector3f position;
    private float pitch, yaw;

    public Camera(Vector3f position) {
        this.position = position;
    }

    public Matrix4f getViewMatrix() {
        return new Matrix4f()
                .rotate((float) -Math.toRadians(pitch), 1, 0, 0)
                .rotate((float) Math.toRadians(yaw), 0, 1, 0)
                .translate(-position.x, -position.y, -position.z);
    }

    public void move(Vector3f offset) {
        position.add(offset);
    }

    public void rotate(float pitchOffset, float yawOffset) {
        this.pitch += pitchOffset;
        this.yaw += yawOffset;

        this.pitch = Math.clamp(this.pitch, -89.0f, 89.0f);
    }

    public Vector3f getPosition() {
        return position;
    }
}
