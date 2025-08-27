package me.formercanuck.automationsurvival.graphics.shadow;

import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.nio.ByteBuffer;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL30.*;

public class ShadowMap {
    public int fbo, depthTexture;
    public final int SHADOW_WIDTH = 2048, SHADOW_HEIGHT = 2048;

    public Matrix4f lightSpaceMatrix;

    public void init(Vector3f lightDir) {
        fbo = glGenFramebuffers();
        depthTexture = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, depthTexture);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_DEPTH_COMPONENT, SHADOW_WIDTH, SHADOW_HEIGHT, 0,
                GL_DEPTH_COMPONENT, GL_FLOAT, (ByteBuffer) null);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_BORDER);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_BORDER);
        glTexParameterfv(GL_TEXTURE_2D, GL_TEXTURE_BORDER_COLOR, new float[]{1, 1, 1, 1});

        glBindFramebuffer(GL_FRAMEBUFFER, fbo);
        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_TEXTURE_2D, depthTexture, 0);
        glDrawBuffer(GL_NONE);
        glReadBuffer(GL_NONE);
        glBindFramebuffer(GL_FRAMEBUFFER, 0);

        // Light-space matrix
        Matrix4f lightProjection = new Matrix4f().ortho(-50, 50, -50, 50, 1, 100);
        Matrix4f lightView = new Matrix4f().lookAt(
                new Vector3f(lightDir).mul(-30), // light position
                new Vector3f(0, 0, 0),           // target
                new Vector3f(0, 1, 0)            // up
        );
        lightSpaceMatrix = lightProjection.mul(lightView);
    }
}