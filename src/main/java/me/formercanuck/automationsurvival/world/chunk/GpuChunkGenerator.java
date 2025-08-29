package me.formercanuck.automationsurvival.world.chunk;

import me.formercanuck.automationsurvival.graphics.ShaderUtils;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL42;
import org.lwjgl.opengl.GL43;

import java.nio.ByteBuffer;

import static org.lwjgl.opengl.GL30.glBindBufferBase;

public class GpuChunkGenerator {
    private final int computeProgram;
    private final int ssbo;
    private final int chunkSize = 16;
    private final int chunkHeight = 128;

    public GpuChunkGenerator() {
        computeProgram = ShaderUtils.createComputeShader("E:\\Dev\\AutomationSurvival - Test\\src\\main\\resources\\shaders\\compute\\VoxelCompute.glsl");
        ssbo = GL15.glGenBuffers();
        GL15.glBindBuffer(GL43.GL_SHADER_STORAGE_BUFFER, ssbo);
        GL15.glBufferData(GL43.GL_SHADER_STORAGE_BUFFER, chunkSize * chunkHeight * chunkSize * Integer.BYTES, GL15.GL_DYNAMIC_DRAW);
        glBindBufferBase(GL43.GL_SHADER_STORAGE_BUFFER, 0, ssbo);
    }

    public int[] generateChunk(int chunkX, int chunkZ) {
        GL20.glUseProgram(computeProgram);
        GL20.glUniform1i(GL20.glGetUniformLocation(computeProgram, "chunkX"), chunkX);
        GL20.glUniform1i(GL20.glGetUniformLocation(computeProgram, "chunkZ"), chunkZ);
        GL20.glUniform1i(GL20.glGetUniformLocation(computeProgram, "chunkSize"), chunkSize);
        GL20.glUniform1i(GL20.glGetUniformLocation(computeProgram, "chunkHeight"), chunkHeight);

        GL43.glDispatchCompute(chunkSize / 8, chunkHeight / 8, chunkSize / 8);
        GL42.glMemoryBarrier(GL43.GL_SHADER_STORAGE_BARRIER_BIT);

        GL15.glBindBuffer(GL43.GL_SHADER_STORAGE_BUFFER, ssbo);
        ByteBuffer buffer = GL15.glMapBuffer(GL43.GL_SHADER_STORAGE_BUFFER, GL15.GL_READ_ONLY);
        int[] voxelData = new int[chunkSize * chunkHeight * chunkSize];
        buffer.asIntBuffer().get(voxelData);
        GL15.glUnmapBuffer(GL43.GL_SHADER_STORAGE_BUFFER);

        return voxelData;
    }
}
