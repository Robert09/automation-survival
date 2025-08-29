package me.formercanuck.automationsurvival.world;

import me.formercanuck.automationsurvival.util.FastNoiseLite;

public class TerrainNoise {
    private final FastNoiseLite noise;

    public TerrainNoise(int seed) {
        noise = new FastNoiseLite(seed);
        noise.SetNoiseType(FastNoiseLite.NoiseType.OpenSimplex2);
        noise.SetFrequency(0.015f);           // Lower frequency = broader hills
        noise.SetFractalOctaves(1);           // Fewer octaves = less detail
        noise.SetFractalLacunarity(1.0f);     // Keep features wide
        noise.SetFractalGain(0.1f);           // Lower gain = smoother transitions
    }

    public float getHeight(float x, float z) {
        return noise.GetNoise(x, z);
    }
}
