package com.main.worldgenerator.generator;

import com.aparapi.Kernel;
import com.aparapi.Range;
import com.main.worldgenerator.Core;
import com.main.worldgenerator.generator.gpu.SimplexOctaveGeneratorGPU;
import org.bukkit.World;
import org.bukkit.generator.ChunkGenerator;

import java.util.Random;
import java.util.logging.Logger;

public class NewChunkGeneratorGPU extends ChunkGenerator {


    private final Logger logger;

    public NewChunkGeneratorGPU(Core core) {
        this.logger = core.getLogger();
    }

    @Override
    public ChunkData generateChunkData(World world, Random random, int chunkX, int chunkZ, BiomeGrid biome) {

        logger.info("Using GPU Generator for chunk " + chunkX + ", " + chunkZ);
        SimplexOctaveGeneratorGPU generator = new SimplexOctaveGeneratorGPU(new Random(world.getSeed()), 8); //GPU
        ChunkData chunk = createChunkData(world);
        generator.setScale(0.005D);
        int[][] currentHeight = new int[16][16];
        Kernel kernel = new Kernel() {
            @Override
            public void run() {
                for (int Xl = 0; Xl < 16; Xl++) {
                    for (int Zl = 0; Zl < 16; Zl++) {
                        currentHeight[Xl][Zl] = (int) generator.noise(chunkX * 16 + Xl, chunkZ * 16 + Zl, 0.5D, 0.5D);
                    }
                }
            }
        };
        kernel.execute(Range.create(6));
        return chunk;
    }
}