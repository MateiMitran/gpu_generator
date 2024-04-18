package com.main.worldgenerator.generator;

import com.aparapi.Kernel;
import com.google.common.collect.MapMaker;
import com.main.worldgenerator.Core;
import com.main.worldgenerator.MaterialIds;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.util.noise.OctaveGenerator;

import java.util.*;
import java.util.logging.Logger;

public abstract class NewChunkGeneratorGPU extends ChunkGenerator implements MaterialIds {


    private final Logger logger;

    private int[] p = new int[512];

    public NewChunkGeneratorGPU(Core core) {
        this.logger = core.getLogger();
        this.init();
    }
    private final Map<World, Map<String, OctaveGenerator>> octaveCache = new MapMaker().weakKeys().makeMap();


    protected abstract void createWorldOctaves(World world,
                                               Map<String, OctaveGenerator> octaves);


    protected final Map<String, OctaveGenerator> getWorldOctaves(World world) {
        if (octaveCache.get(world) == null) {
            Map<String, OctaveGenerator> octaves = new HashMap<String, OctaveGenerator>();
            createWorldOctaves(world, octaves);
            octaveCache.put(world, octaves);
            return octaves;
        }
        return octaveCache.get(world);
    }
    private void init() {
        int[] permutation = new int[] { 151, 160, 137, 91, 90, 15, 131, 13, 201,
                95, 96, 53, 194, 233, 7, 225, 140, 36, 103, 30, 69, 142, 8, 99,
                37, 240, 21, 10, 23, 190, 6, 148, 247, 120, 234, 75, 0, 26,
                197, 62, 94, 252, 219, 203, 117, 35, 11, 32, 57, 177, 33, 88,
                237, 149, 56, 87, 174, 20, 125, 136, 171, 168, 68, 175, 74,
                165, 71, 134, 139, 48, 27, 166, 77, 146, 158, 231, 83, 111,
                229, 122, 60, 211, 133, 230, 220, 105, 92, 41, 55, 46, 245, 40,
                244, 102, 143, 54, 65, 25, 63, 161, 1, 216, 80, 73, 209, 76,
                132, 187, 208, 89, 18, 169, 200, 196, 135, 130, 116, 188, 159,
                86, 164, 100, 109, 198, 173, 186, 3, 64, 52, 217, 226, 250,
                124, 123, 5, 202, 38, 147, 118, 126, 255, 82, 85, 212, 207,
                206, 59, 227, 47, 16, 58, 17, 182, 189, 28, 42, 223, 183, 170,
                213, 119, 248, 152, 2, 44, 154, 163, 70, 221, 153, 101, 155,
                167, 43, 172, 9, 129, 22, 39, 253, 19, 98, 108, 110, 79, 113,
                224, 232, 178, 185, 112, 104, 218, 246, 97, 228, 251, 34, 242,
                193, 238, 210, 144, 12, 191, 179, 162, 241, 81, 51, 145, 235,
                249, 14, 239, 107, 49, 192, 214, 31, 181, 199, 106, 157, 184,
                84, 204, 176, 115, 121, 50, 45, 127, 4, 150, 254, 138, 236,
                205, 93, 222, 114, 67, 29, 24, 72, 243, 141, 128, 195, 78, 66,
                215, 61, 156, 180 };
        for (int i = 0; i < 256; i++) {
            p[256 + i] = p[i] = permutation[i];
        }
    }

    private double computeNoise(double x, double y, double z, double size) {
        int X = ((int) floor(x / size)) & 255;
        int Y = ((int) floor(y / size)) & 255;
        int Z = ((int) floor(z / size)) & 255;
        double u = fade((x / size) - floor(x / size));
        double v = fade((y / size) - floor(y / size));
        double w = fade((z / size) - floor(z / size));
        int A = p[X] + Y, AA = p[A] + Z, AB = p[A + 1] + Z;
        int B = p[X + 1] + Y, BA = p[B] + Z, BB = p[B + 1] + Z;
        return lerp(w, lerp(v, lerp(u, grad(p[AA], x, y, z), grad(p[BA], x-1, y, z)),
                        lerp(u, grad(p[AB], x, y-1, z), grad(p[BB], x-1, y-1, z))),
                lerp(v, lerp(u, grad(p[AA+1], x, y, z-1), grad(p[BA+1], x-1, y, z-1)),
                        lerp(u, grad(p[AB+1], x, y-1, z-1), grad(p[BB+1], x-1, y-1, z-1))));
    }
    @Override
    public ChunkData generateChunkData(World world, Random random, int chunkX, int chunkZ, BiomeGrid biome) {

        logger.info("Using GPU Generator for chunk " + chunkX + ", " + chunkZ);
        ChunkData chunk = createChunkData(world);
        int width = 16;
        int height = 256;  // Max height for simplicity

        double[] heightMap = new double[width * width];

        Kernel kernel = new Kernel() {
            @Override
            public void run() {
                int index = getGlobalId();
                int x = index % width;
                int z = index / width;
                double noiseValue = 0;
                double size = 100.0;  // Example scale size for noise
                noiseValue += computeNoise(chunkX * 16 + x, 0, chunkZ * 16 + z, size);
                heightMap[index] = noiseValue * 100;  // Scale factor for height
            }


        };
        kernel.execute(width * width);

        // Apply the height map to the chunk
        for (int i = 0; i < width * width; i++) {
            int x = i % width;
            int z = i / width;
            int finalHeight = Math.min(height, (int) heightMap[i]);
            for (int y = 0; y <= finalHeight; y++) {
                Material mat = (y == finalHeight) ? Material.GRASS_BLOCK : Material.DIRT;
                chunk.setBlock(x, y, z, mat);
            }
        }

        return chunk;
    }

    private double fade(double t) {
        // Fade function unchanged
        return t * t * t * (t * (t * 6 - 15) + 10);
    }

    private double lerp(double t, double a, double b) {
        // Linear interpolation unchanged
        return a + t * (b - a);
    }

    private double floor(double value) {
        if (value >=0) {
            return (int) value;
        } else {
            return (int) value - 1;
        }
    }

    protected List<BlockPopulator> populators;

    @Override
    public final List<BlockPopulator> getDefaultPopulators(World world) {
        if (populators == null || world != null)
            return Collections.emptyList();
        return populators;
    }

    private final Map<World, Map<String, OctaveGenerator>> octaveCache = new MapMaker().weakKeys().makeMap();



    private double grad(int hash, double x, double y, double z) {
        // Calculate gradient from hash
        int h = hash & 15;
        double u;
        if (h < 8) {
            u = x;
        } else {
            u = y;
        }
        double v;
        if (h<4) {
            v = y;
        } else if (h == 12 || h == 14) {
            v = x;
        } else {
            v = z;
        }

        double grad_u;
        double grad_v;

        if ((h & 1) == 0) {
            grad_u = u;
        } else {
            grad_u = -u;
        }
        if ((h&2) == 0) {
            grad_v = v;
        } else {
            grad_v = -v;
        }
        return grad_u + grad_v;
    }

    @Override
    public Location getFixedSpawnLocation(World world, Random random) {
        int x = -16;
        do {
            x += 16;
            world.loadChunk(x / 16, 0);
        } while (!canSpawn(world, x, 0));

        return new Location(world, x, world.getHighestBlockYAt(x, 0), 0);
    }

    private static final Set<Material> FORBIDDEN_SPAWN_FLOORS = new HashSet<Material>();
    static {
        // Air and liquids are already accounted for.
        FORBIDDEN_SPAWN_FLOORS.add(Material.FIRE); // That would hurt.
        FORBIDDEN_SPAWN_FLOORS.add(Material.CACTUS); // Ouch!
    }


    @Override
    public boolean canSpawn(World world, int x, int z) {
        Block block = world.getHighestBlockAt(x, z).getRelative(BlockFace.DOWN);
        return !block.isLiquid() && !block.isEmpty()
                && !FORBIDDEN_SPAWN_FLOORS.contains(block.getType());
    }

    public abstract byte[] generate(World world, Random random, int chunkX, int chunkZ);

    protected abstract void createWorldOctaves(World world, Map<String, OctaveGenerator> octaves);
}