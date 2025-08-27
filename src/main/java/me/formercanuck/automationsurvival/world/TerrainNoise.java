package me.formercanuck.automationsurvival.world;

import me.formercanuck.automationsurvival.util.FastNoiseLite;

public class TerrainNoise {
    private final FastNoiseLite noise;

    public TerrainNoise(int seed) {
        noise = new FastNoiseLite(seed);
        noise.SetNoiseType(FastNoiseLite.NoiseType.OpenSimplex2);
        noise.SetFrequency(0.01f);
        noise.SetFractalOctaves(4);
        noise.SetFractalLacunarity(2.0f);
        noise.SetFractalGain(0.5f);
    }

    public float getHeight(float x, float z) {
        return noise.GetNoise(x, z);
    }
}
