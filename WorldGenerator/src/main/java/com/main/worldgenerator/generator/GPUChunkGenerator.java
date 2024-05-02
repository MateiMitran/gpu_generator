package com.main.worldgenerator.generator;

import com.main.worldgenerator.Core;
import org.bukkit.*;
import org.bukkit.block.Biome;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.generator.BlockPopulator;
import java.util.Random;
import java.util.List;
import java.util.ArrayList;
import java.util.logging.Logger;


public class GPUChunkGenerator extends ChunkGenerator {
    private Core core;

    private final Logger logger;

    public GPUChunkGenerator(Core core) {
        this.core = core;
        this.logger = core.getLogger();
    }

    @Override
    public ChunkData generateChunkData(World world, Random random, int chunkX, int chunkZ, BiomeGrid biome) {
        ChunkData chunk = createChunkData(world);
        int width = 16;
        int height = world.getMaxHeight();

        long seed = world.getSeed();
        float scale = 0.025F; // Scale factor for noise, controls feature size
        int seaLevel = 62;
        int riverDepth = 3;

        for (int x = 0; x < width; x++) {
            for (int z = 0; z < width; z++) {
                float worldX = (chunkX * width) + x;
                float worldZ = (chunkZ * width) + z;

                float smallScaleNoise = OpenSimplex2.calculateNew(seed, worldX * 0.1F, 0, worldZ * 0.1F);
                float noiseValue = smallScaleNoise * 0.2f;

                Biome currentBiome = biome.getBiome(x, 0, z);
                int baseHeight = height / 4;
                int surfaceHeight;

                if (currentBiome == Biome.RIVER) {
                    surfaceHeight = seaLevel - riverDepth;
                    for (int y = 0; y < height; y++) {
                        if (y < surfaceHeight) {
                            chunk.setBlock(x, y, z, Material.STONE);
                        } else if (y <= seaLevel) {
                            chunk.setBlock(x, y, z, Material.WATER);
                        } else {
                            chunk.setBlock(x, y, z, Material.AIR);
                        }
                    }
                } else if (currentBiome == Biome.BEACH) {
                    surfaceHeight = seaLevel + 1; // Beaches are at sea level or above sea level? GOD HELP US
                    for (int y = 0; y < height; y++) {
                        if (y < surfaceHeight) {
                            chunk.setBlock(x, y, z, Material.SAND);
                        } else {
                            chunk.setBlock(x, y, z, Material.AIR);
                        }
                    }
                } else if (currentBiome == Biome.DESERT || currentBiome == Biome.DESERT_HILLS || currentBiome == Biome.DESERT_LAKES) {
                    surfaceHeight = (currentBiome == Biome.DESERT_HILLS) ? baseHeight + (int) (noiseValue * height / 4) : baseHeight;
                    for (int y = 0; y < height; y++) {
                        if (y < surfaceHeight) {
                            chunk.setBlock(x, y, z, Material.SAND);
                        } else if (currentBiome == Biome.DESERT_LAKES && y == seaLevel) {
                            chunk.setBlock(x, y, z, Material.WATER);
                        } else {
                            chunk.setBlock(x, y, z, Material.AIR);
                        }
                    }
                } else if (currentBiome == Biome.OCEAN || currentBiome == Biome.COLD_OCEAN || currentBiome == Biome.DEEP_OCEAN || currentBiome == Biome.DEEP_COLD_OCEAN
                        || currentBiome == Biome.WARM_OCEAN || currentBiome == Biome.DEEP_WARM_OCEAN || currentBiome == Biome.LUKEWARM_OCEAN
                        || currentBiome == Biome.DEEP_LUKEWARM_OCEAN) {
                    // Define the seabed level based on noise, creating some depth variation
                    surfaceHeight = seaLevel - 20 + (int) (noiseValue * 15);

                    for (int y = 0; y <= seaLevel; y++) {
                        Material material;
                        if (y > surfaceHeight) {
                            material = Material.WATER;
                        } else {
                            // Select seabed material
                            material = (random.nextDouble() < 0.2) ? Material.GRAVEL : Material.SAND;
                        }
                        chunk.setBlock(x, y, z, material);
                    }

                    // Add shore when near land
//                    if (isNearLand && z == surfaceHeight + 1) {
//                        chunk.setBlock(x, seaLevel, z, Material.SAND);
//                    }
                } else if (currentBiome == Biome.FROZEN_OCEAN || currentBiome == Biome.DEEP_FROZEN_OCEAN) {
                    surfaceHeight = seaLevel - 20 + (int) (noiseValue * 10); // Shallower depth variation for frozen oceans

                    for (int y = 0; y <= seaLevel; y++) {
                        Material material;
                        if (y > surfaceHeight) {
                            material = Material.ICE; // Surface is icy
                        } else {
                            material = Material.PACKED_ICE; // Packed ice at the bottom
                        }
                        chunk.setBlock(x, y, z, material);
                    }
                } else if (currentBiome == Biome.PLAINS || currentBiome == Biome.FOREST || currentBiome == Biome.BIRCH_FOREST || currentBiome == Biome.TAIGA
                        || currentBiome == Biome.SWAMP || currentBiome == Biome.SUNFLOWER_PLAINS || currentBiome == Biome.FLOWER_FOREST
                        || currentBiome == Biome.TALL_BIRCH_FOREST || currentBiome == Biome.DARK_FOREST
                        || currentBiome == Biome.WOODED_HILLS || currentBiome == Biome.TAIGA_HILLS) {
                    // Plains biome: very flat terrain
                    surfaceHeight = seaLevel + (int) (noiseValue * 10); // Small variation around sea level
                    for (int y = 0; y < height; y++) {
                        Material material;
                            if (y == surfaceHeight) {
                                material = Material.GRASS_BLOCK; // Surface layer
                            } else if (y < surfaceHeight && y > surfaceHeight - 5) {
                                material = Material.DIRT; // Subsurface layer
                            } else if (y <= surfaceHeight - 5) {
                                material = Material.STONE; // Underground
                            } else {
                                material = Material.AIR; // Above ground
                            }
                            chunk.setBlock(x, y, z, material);
                        }
                } else {
                    // Mountains and other biomes

                    surfaceHeight = baseHeight + (int) (noiseValue * (height / 6));
                    if (random.nextDouble() < 0.3) { // Reduce mountain frequency
                        surfaceHeight = baseHeight + (int) (noiseValue * height / 10);
                    }
                    for (int y = 0; y < height; y++) {
                        Material material;
                        if (y == surfaceHeight) {
                            material = Material.GRASS_BLOCK; // Surface layer
                        } else if (y < surfaceHeight && y > surfaceHeight - 5) {
                            material = Material.DIRT; // Subsurface layer
                        } else if (y <= surfaceHeight - 5) {
                            material = Material.STONE; // Underground
                        } else {
                            material = Material.AIR; // Above ground
                        }
                        chunk.setBlock(x, y, z, material);
                    }

                }
            }
        }

        return chunk;
    }

    @Override
    public List<BlockPopulator> getDefaultPopulators(World world) {
        ArrayList<BlockPopulator> populators = new ArrayList<>();
//        populators.add(new TreePopulator());
//        populators.add(new OrePopulator());
//        populators.add(new BiomePopulator());
//        populators.add(new TerrainPopulator());
        return populators;
    }

    @Override
    public boolean shouldGenerateCaves() {
        return true;
    }

    @Override
    public boolean shouldGenerateDecorations() {
        return true;
    }

    @Override
    public boolean shouldGenerateStructures() {
        return true;
    }

    @Override
    public boolean shouldGenerateMobs() {
        return true;
    }

    @Override
    public boolean isParallelCapable() {
        return true;
    }

}