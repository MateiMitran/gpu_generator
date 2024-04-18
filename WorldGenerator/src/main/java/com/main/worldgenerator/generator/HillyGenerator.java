package com.main.worldgenerator.generator;


import com.main.worldgenerator.populators.*;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.util.noise.OctaveGenerator;
import org.bukkit.util.noise.SimplexOctaveGenerator;

import java.util.Arrays;
import java.util.Map;
import java.util.Random;



public class HillyGenerator extends NewChunkGeneratorGPU {
	{
		populators = Arrays.asList(
				// In-ground
				new LakePopulator().setDefault(this),
				new TreePopulator().setDefault(this),
				new MushroomPopulator().setDefault(this),
				new SnowPopulator().setDefault(this),
				new OrePopulator().setDefault(this));
	}

	@Override
	public byte[] generate(World world, Random random, int chunkX, int chunkZ) {
		Map<String, OctaveGenerator> octaves = getWorldOctaves(world);
		OctaveGenerator noiseHeight = octaves.get("height");
		OctaveGenerator noiseJitter = octaves.get("jitter");
		OctaveGenerator noiseType = octaves.get("type");

		chunkX <<= 4;
		chunkZ <<= 4;

		byte matMain = DIRT;
		
		byte matShore = SAND;

		byte bedrock = BEDROCK;

		byte[] b = new byte[272 * 128];

		int baseHeight = getArgInt(world, "baseheight", 70, 0, 127);
		double terrainHeight = getArgDouble(world, "terrainheight", 16.0);
		boolean noDirt = getArg(world, "nodirt");
		int waterLevel = getArgInt(world, "waterlevel", 64, 0, 127);

		for (int x = 0; x < 16; x++) {
			for (int z = 0; z < 16; z++) {
				int deep = 0;
				for (int y = (int) Math.min(baseHeight
						+ noiseHeight.noise(x + chunkX, z + chunkZ, 0.7, 0.6, true)
						* terrainHeight
						+ noiseJitter.noise(x + chunkX, z + chunkZ, 0.5, 0.5)
						* 1.5, 127); y > 0; y--) {
					double terrainType = noiseType.noise(x + chunkX, y, z
							+ chunkZ, 0.5, 0.5);
					byte ground = matTop;
					if (Math.abs(terrainType) < random.nextDouble() / 3
							&& !noDirt) {
						ground = matMain;
					} else if (deep != 0
							|| y < waterLevel) {
						ground = matMain;
					}

					if (Math.abs(y - waterLevel) < 5 - random.nextInt(2)
							&& deep < 7) {
						if (terrainType < random.nextDouble() / 2) {
							if (terrainType < random.nextDouble() / 4) {
								ground = matShore;
							} else {
								ground = matShore2;
							}
						}
					}

					if (deep > random.nextInt(3) + 6) {
						ground = matUnder;
					}

					b[x * 2048 + z * 128 + y] = ground;
					deep++;
				}
				b[x * 2048 + z * 128] = bedrock;
			}
		}

		for (int x = 0; x < 16; x++) {
			for (int z = 0; z < 16; z++) {
				for (int y = 0; y < waterLevel; y++) {
					if (b[x * 2048 + z * 128 + y] == 0) {
						b[x * 2048 + z * 128 + y] = matLiquid;
					}
				}
			}
		}

		return b;
	}

	/**
	 * @see net.llamaslayers.minecraft.banana.gen.BananaChunkGenerator#createWorldOctaves(World,
	 *      Map)
	 */
	@Override
	protected void createWorldOctaves(World world,
									  Map<String, OctaveGenerator> octaves) {
		Random seed = new Random(world.getSeed());

		/* With default settings, this is 5 octaves. With tscale=256,terrainheight=50,
		 * this comes out to 14 octaves, which makes more complex terrain at the cost
		 * of more complex generation. Without this, the terrain looks bad, especially
		 * on higher tscale/terrainheight pairs. */
		OctaveGenerator gen = new SimplexOctaveGenerator(seed, Math.max((int) Math.round(Math.sqrt(50
				* getArgDouble(world, "tscale", 64.0)
				/ (128 - getArgDouble(world, "terrainheight", 16.0))) * 1.1 - 0.2), 5));
		gen.setScale(1 / getArgDouble(world, "tscale", 64.0));
		octaves.put("height", gen);

		gen = new SimplexOctaveGenerator(seed, gen.getOctaves().length / 2);
		gen.setScale(Math.min(getArgDouble(world, "tscale", 64.0) / 1024, 1 / 32.0));
		octaves.put("jitter", gen);

		gen = new SimplexOctaveGenerator(seed, 2);
		gen.setScale(1 / 128.0);
		octaves.put("type", gen);
	}
}
