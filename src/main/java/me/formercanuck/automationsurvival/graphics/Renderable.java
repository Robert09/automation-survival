package me.formercanuck.automationsurvival.graphics;

import me.formercanuck.automationsurvival.graphics.mesh.Mesh;
import org.joml.Matrix4f;

public interface Renderable {

    Mesh getMesh();

    Matrix4f getModelMatrix();

    boolean isVisible();
}
