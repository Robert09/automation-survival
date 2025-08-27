package me.formercanuck.automationsurvival.graphics.shader;

import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.system.MemoryStack;
import java.nio.FloatBuffer;
import java.nio.file.Files;
import java.nio.file.Path;

public class ShaderProgram {
    private final int programId;

    public ShaderProgram(String vertexPath, String fragmentPath) {
        try {
            String vsrc = Files.readString(Path.of(vertexPath));
            String fsrc = Files.readString(Path.of(fragmentPath));
            int vs = compile(GL_VERTEX_SHADER, vsrc);
            int fs = compile(GL_FRAGMENT_SHADER, fsrc);
            programId = glCreateProgram();
            glAttachShader(programId, vs);
            glAttachShader(programId, fs);
            glLinkProgram(programId);
            if (glGetProgrami(programId, GL_LINK_STATUS) == 0)
                throw new RuntimeException("Program link error: " + glGetProgramInfoLog(programId));
            glDeleteShader(vs);
            glDeleteShader(fs);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private int compile(int type, String src) {
        int id = glCreateShader(type);
        glShaderSource(id, src);
        glCompileShader(id);
        if (glGetShaderi(id, GL_COMPILE_STATUS) == 0)
            throw new RuntimeException("Shader compile error: " + glGetShaderInfoLog(id));
        return id;
    }

    public void use() { glUseProgram(programId); }

    public void setMat4(String name, Matrix4f mat) {
        int loc = glGetUniformLocation(programId, name);
        try (MemoryStack stack = MemoryStack.stackPush()) {
            FloatBuffer fb = stack.mallocFloat(16);
            mat.get(fb);
            glUniformMatrix4fv(loc, false, fb);
        }
    }

    public void delete() { glDeleteProgram(programId); }

    public void setVec3(String uLightDir, Vector3f normalize) {
        int loc = glGetUniformLocation(programId, uLightDir);
        glUniform3f(loc, normalize.x, normalize.y, normalize.z);
    }
}
