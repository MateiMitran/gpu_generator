package com.main.worldgenerator.generator;

import com.main.worldgenerator.Core;
import com.main.worldgenerator.generator.gpu.SimplexNoiseGpu3D;
import org.apache.commons.lang.StringUtils;
import org.bukkit.*;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.util.BlockVector;
import org.bukkit.util.Vector;

import java.util.Arrays;
import java.util.Random;
import java.util.List;
import java.util.ArrayList;
import java.util.logging.Logger;

import static com.main.worldgenerator.generator.gpu.SimplexNoiseGpu3D.calculateFastOctaved;

public class CustomChunkGeneratorExtra extends ChunkGenerator {
    private OpenSimplexNoise noiseGenerator;

    private Core core;

    private final Logger logger;

    public CustomChunkGeneratorExtra(Core core) {
        noiseGenerator = new OpenSimplexNoise();
        this.core = core;
        this.logger = core.getLogger();
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
        return false;
    }

    @Override
    public ChunkData generateChunkData(World world, Random random, int chunkX, int chunkZ, BiomeGrid biome) {
        ChunkData chunk = createChunkData(world); // Method to create a new chunk data container
        int width = 16; // Standard width and depth of a Minecraft chunk
        int height = world.getMaxHeight(); // Maximum height of the world

        long seed = world.getSeed(); // Get the world's seed for consistent noise generation
        float scale = 0.025F; // Scale factor for noise, controls feature size
        int seaLevel = 62;
        // Loop through each block in the chunk
        for (int x = 0; x < width; x++) {
            for (int z = 0; z < width; z++) {
                // Convert chunk-relative coordinates to world coordinates
                float worldX = (chunkX * width) + x;
                float worldZ = (chunkZ * width) + z;

                // Generate noise value for each point, scaled by 'scale'
                // This method should use a 3D noise function, like noise3_ImproveXZ or noise3_ImproveXY depending on orientation

                float smallScaleNoise = SimplexNoiseGpu3D.calculateNew(seed, worldX * 0.1F, 0, worldZ * 0.1F);

                float noiseValue = smallScaleNoise * 0.2f;

                logger.info("X:" + worldX + " Z:" + worldZ + " Noise:" + noiseValue);
                // Scale and shift the noise value to generate a suitable elevation, ensuring it's within world bounds
                int sampleY = seaLevel;
                // Get biome at current x, z coordinates
                Biome currentBiome = biome.getBiome(x, sampleY, z);
                int baseHeight = height / 4;
                int surfaceHeight;

                if (currentBiome == Biome.PLAINS) {
                    surfaceHeight = Math.min(height, Math.max(0, baseHeight + (int) (noiseValue * height / 8)));
                } else if (currentBiome == Biome.MOUNTAINS) {
                    surfaceHeight = Math.min(height, Math.max(0, baseHeight + (int) (noiseValue * height / 2)));
                } else {
                    surfaceHeight = Math.min(height, Math.max(0, baseHeight + (int) (noiseValue * height / 6)));
                }

                // Set blocks based on calculated height
                for (int y = 0; y < height; y++) {
                    Material material;
                    if (y == surfaceHeight) {
                        material = Material.GRASS_BLOCK; // Surface layer/
                    } else if (y < surfaceHeight && y > surfaceHeight - 5) {
                        material = Material.DIRT; // Subsurface layer
                    } else if (y <= surfaceHeight - 5) {
                        material = Material.STONE; // Underground
                    } else {
                        material = Material.AIR; // Above ground
                    }
                    chunk.setBlock(x, y, z, material); // Set the block in the chunk data
                }

                if (currentBiome == Biome.OCEAN || currentBiome == Biome.COLD_OCEAN || currentBiome == Biome.DEEP_OCEAN) {
                    surfaceHeight = seaLevel - 20; // Define the seabed just below sea level
                    // Fill the chunk with water up to sea level
                    for (int y = 0; y < seaLevel; y++) {
                        if (y > surfaceHeight) {
                            chunk.setBlock(x, y, z, Material.WATER);
                        } else {
                            chunk.setBlock(x, y, z, Material.SAND); // Or use SAND, based on your preference
                        }
                    }

                }
            }
        }

        return chunk; // Return the populated chunk data
    }


    @Override
    public List<BlockPopulator> getDefaultPopulators(World world) {
        ArrayList<BlockPopulator> populators = new ArrayList<>();
        populators.add(new TreePopulator());
        populators.add(new OrePopulator());
        populators.add(new BiomePopulator());
        populators.add(new TerrainPopulator());
        return populators;
    }

    private class TreePopulator extends BlockPopulator {
        @Override
        public void populate(World world, Random random, Chunk chunk) {
            int chance = 20; // Set the chance of a tree spawning at any x, z
            for (int i = 0; i < chance; i++) {
                int x = random.nextInt(16);
                int z = random.nextInt(16);
                int y = world.getHighestBlockYAt(chunk.getX() * 16 + x, chunk.getZ() * 16 + z);
                Material type = world.getBlockAt(x, y - 1, z).getType();
                if (type == Material.GRASS) {
                    world.generateTree(new Location(world, chunk.getX() * 16 + x, y, chunk.getZ() * 16 + z), TreeType.TREE);
                }
            }
        }
    }

    private class TerrainPopulator extends BlockPopulator {
        public void populate(World world, Random rand, Chunk chunk) {
            for(int x = 0; x < 16; ++x) {
                for(int z = 0; z < 16; ++z) {
                    for(int y = 0; y < world.getMaxHeight() / 2; ++y) {
                        Block block = chunk.getBlock(x, y, z);
                        Material coverBottom = Material.DIRT;
                        Material coverTop = Material.GRASS_BLOCK;
                        if (block.getType() == Material.SANDSTONE) {
                            coverBottom = Material.SAND;
                            coverTop = Material.SAND;
                        } else if (block.getType() == Material.ICE) {
                            coverBottom = Material.SNOW_BLOCK;
                            coverTop = Material.SNOW_BLOCK;
                        } else if (block.getType() == Material.STONE) {
                            if (StringUtils.containsIgnoreCase(block.getBiome().toString(), "mushroom")) {
                                coverBottom = Material.DIRT;
                            }
                        } else if (StringUtils.containsIgnoreCase(block.getBiome().toString(), "taiga")) {
                            coverBottom = Material.DIRT;
                            coverTop = Material.GRASS_BLOCK;
                        }

                        if (block.getType() == Material.STONE || block.getType() == Material.SANDSTONE || block.getType() == Material.ICE) {
                            if (chunk.getBlock(x, y + 3, z).getType() == Material.AIR && chunk.getBlock(x, y - 1, z).getType() != Material.AIR) {
                                block.setType(coverBottom);
                            } else if (chunk.getBlock(x, y + 2, z).getType() == Material.AIR && chunk.getBlock(x, y - 1, z).getType() != Material.AIR) {
                                block.setType(coverBottom);
                            }

                            if (chunk.getBlock(x, y + 1, z).getType() == Material.AIR && chunk.getBlock(x, y - 1, z).getType() != Material.AIR) {
                                block.setType(coverTop);
                            }
                        }
                    }
                }
            }

        }
    }
    private class BiomePopulator extends BlockPopulator {
        public void populate(World world, Random random, Chunk chunk) {
            int ChunkX = chunk.getX() * 16;
            int ChunkZ = chunk.getZ() * 16;

            for(int x = 0; x < 16; ++x) {
                for(int z = 0; z < 16; ++z) {
                    int height = world.getHighestBlockAt(x + ChunkX, z + ChunkZ).getY();

                    for(int y = 0; y < height; ++y) {
                        Biome currentBiome = world.getBiome(x + ChunkX, z + ChunkZ);
                        Block block = world.getBlockAt(x + ChunkX, y, z + ChunkZ);
                        if (block.getType() == Material.STONE) {
                            if (StringUtils.containsIgnoreCase(currentBiome.toString(), "desert")) {
                                block.setType(Material.SANDSTONE);
                            } else if (StringUtils.containsIgnoreCase(currentBiome.toString(), "ice")) {
                                block.setType(Material.ICE);
                            } else if (StringUtils.containsIgnoreCase(currentBiome.toString(), "mushroom")) {
                                block.setType(Material.STONE);
                            }
                        }
                    }
                }
            }

        }
    }


    private class DesertPopulator extends BlockPopulator {

        @Override
        public void populate(World world, Random random, Chunk chunk) {
            Material matSand = world.getEnvironment() == World.Environment.NETHER ? Material.SOUL_SAND : Material.SAND;

            Material matDirt = world.getEnvironment() == World.Environment.NETHER ? Material.NETHERRACK : Material.DIRT;

            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
                    int tx = (chunk.getX() << 4) + x;
                    int tz = (chunk.getZ() << 4) + z;
                    int y = world.getHighestBlockYAt(tx, tz);

                    Block block = chunk.getBlock(x, y, z).getRelative(BlockFace.DOWN);
                    if (block.getBiome() != Biome.DESERT) {
                        continue;
                    }

                    for (int i = 0; i < 5; ++i) {
                        Block b2 = block.getRelative(0, -i, 0);
                        if (b2.getType() == Material.GRASS_BLOCK
                                || b2.getType() == matDirt) {
                            b2.setType(matSand);
                        }
                    }

                    // Generate cactus
                    if (block.getType() == matSand) {
                        if (random.nextInt(20) == 0) {
                            // Make sure it's surrounded by air
                            Block base = block.getRelative(BlockFace.UP);
                            if (base.getType() == Material.AIR
                                    && base.getRelative(BlockFace.NORTH).getType() == Material.AIR
                                    && base.getRelative(BlockFace.EAST).getType() == Material.AIR
                                    && base.getRelative(BlockFace.SOUTH).getType() == Material.AIR
                                    && base.getRelative(BlockFace.WEST).getType() == Material.AIR) {
                                generateCactus(base, random.nextInt(4), world);
                            }
                        }
                    }
                }
            }
        }

        private void generateCactus(Block block, int height, World world) {
            if (world.getEnvironment() == World.Environment.NETHER) {
                block.setType(Material.FIRE);
            } else {
                for (int i = 0; i < height; ++i) {
                    block.getRelative(0, i, 0).setType(Material.CACTUS);
                }
            }
        }
    }

    private class OrePopulator extends BlockPopulator {

        private final int[] iterations = new int[] {10, 20, 20, 2, 8, 1, 1, 1};
        private final int[] amount = new int[] {32, 16, 8, 8, 7, 7, 6};
        private final Material[] type = new Material[] {Material.GRAVEL, Material.COAL_ORE,
                Material.IRON_ORE, Material.GOLD_ORE, Material.REDSTONE_ORE,
                Material.DIAMOND_ORE, Material.LAPIS_ORE};
        private final int[] maxHeight = new int[] {128, 128, 128, 128, 128, 64,
                32, 16, 16, 32};

        @Override
        public void populate(World world, Random random, Chunk source) {

            for (int i = 0; i < type.length; i++) {
                for (int j = 0; j < iterations[i]; j++) {
                    internal(source, random, random.nextInt(16),
                            random.nextInt(maxHeight[i]), random.nextInt(16),
                            amount[i], type[i]);
                }
            }
        }

        private void internal(Chunk source, Random random, int originX,
                              int originY, int originZ, int amount, Material type) {
            for (int i = 0; i < amount; i++) {
                int x = originX + random.nextInt(amount / 2) - amount / 4;
                int y = originY + random.nextInt(amount / 4) - amount / 8;
                int z = originZ + random.nextInt(amount / 2) - amount / 4;
                x &= 0xf;
                z &= 0xf;
                if (y > 127 || y < 0) {
                    continue;
                }
                Block block = source.getBlock(x, y, z);
                if (block.getType() == Material.STONE) {
                    block.setType(type, false);
                }
            }
        }
    }
    public class LakePopulator extends BlockPopulator {

        @Override
        public void populate(World world, Random random, Chunk source) {

            if (random.nextInt(10) > 1)
                return;

            ChunkSnapshot snapshot = source.getChunkSnapshot();

            int rx16 = random.nextInt(16);
            int rx = (source.getX() << 4) + rx16;
            int rz16 = random.nextInt(16);
            int rz = (source.getZ() << 4) + rz16;
            if (snapshot.getHighestBlockYAt(rx16, rz16) < 4)
                return;
            int ry = 6 + random.nextInt(snapshot.getHighestBlockYAt(rx16, rz16) - 3);
            int radius = 2 + random.nextInt(3);

            Material liquidMaterial = Material.LAVA;
            Material solidMaterial = Material.OBSIDIAN;

            if (random.nextInt(10) < 3) {
                ry = snapshot.getHighestBlockYAt(rx16, rz16) - 1;
            }
            if (random.nextInt(96) < ry) {
                liquidMaterial = Material.WATER;
                solidMaterial = Material.WATER;
            } else if (world.getBlockAt(rx, ry, rz).getBiome() == Biome.FOREST)
                return;

            ArrayList<Block> lakeBlocks = new ArrayList<Block>();
            for (int i = -1; i < 4; i++) {
                Vector center = new BlockVector(rx, ry - i, rz);
                for (int x = -radius; x <= radius; x++) {
                    for (int z = -radius; z <= radius; z++) {
                        Vector position = center.clone().add(new Vector(x, 0, z));
                        if (center.distance(position) <= radius + 0.5 - i) {
                            lakeBlocks.add(world.getBlockAt(position.toLocation(world)));
                        }
                    }
                }
            }

            for (Block block : lakeBlocks) {
                // Ensure it's not air or liquid already
                if (!block.isEmpty() && !block.isLiquid()) {
                    if (block.getY() == ry + 1) {
                        if (random.nextBoolean()) {
                            block.setType(Material.AIR);
                        }
                    } else if (block.getY() == ry) {
                        block.setType(Material.AIR);
                    } else if (random.nextInt(10) > 1) {
                        block.setType(liquidMaterial);
                    } else {
                        block.setType(solidMaterial);
                    }
                }
            }
        }
    }
}