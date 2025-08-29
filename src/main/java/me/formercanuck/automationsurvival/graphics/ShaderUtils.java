package me.formercanuck.automationsurvival.graphics;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL43;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class ShaderUtils {

    public static int createComputeShader(String path) {
        String source = null;
        try {
            source = Files.readString(Path.of(path));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        int shader = GL20.glCreateShader(GL43.GL_COMPUTE_SHADER);
        GL20.glShaderSource(shader, source);
        GL20.glCompileShader(shader);
        if (GL20.glGetShaderi(shader, GL20.GL_COMPILE_STATUS) == GL11.GL_FALSE)
            throw new RuntimeException("Compute shader compile error: " + GL20.glGetShaderInfoLog(shader));

        int program = GL20.glCreateProgram();
        GL20.glAttachShader(program, shader);
        GL20.glLinkProgram(program);
        GL20.glDeleteShader(shader);
        return program;
    }
}
