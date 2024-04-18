package com.main.worldgenerator.generator;

import com.main.worldgenerator.Core;
import com.main.worldgenerator.populators.BoatPopulator;
import com.main.worldgenerator.populators.OrePopulator;
import org.bukkit.World;
import org.bukkit.util.noise.OctaveGenerator;
import org.bukkit.util.noise.SimplexOctaveGenerator;

import java.util.Arrays;
import java.util.Map;
import java.util.Random;

/**
 * A generator that makes sandy, desert island-filled worlds, as suggested on
 * <a href="http://forums.bukkit.org/threads/23014/">the Bukkit forums</a>
 *
 * @author Nightgunner5
 */
public class BeachGenerator extends NewChunkGeneratorGPU {
	{
		populators = Arrays.asList(
				new OrePopulator().setDefault(this),
				new BoatPopulator().setDefault(this));
	}

	public BeachGenerator(Core core) {
		super(core);
	}


	@Override
	public byte[] generate(World world, Random random, int chunkX, int chunkZ) {
		Map<String, OctaveGenerator> octaves = getWorldOctaves(world);
		OctaveGenerator noiseTerrainHeight = octaves.get("terrainHeight");
		OctaveGenerator noiseTerrainType = octaves.get("terrainType");
		OctaveGenerator noiseTerrainType2 = octaves.get("terrainType2");

		chunkX <<= 4;
		chunkZ <<= 4;

		byte[] b = new byte[272 * 128];

		byte liquid = STATIONARY_WATER;
		byte sand = SAND;
		byte sandstone = SANDSTONE;
		byte stone = STONE;
		byte bedrock = BEDROCK;

		for (int x = 0; x < 16; x++) {
			for (int z = 0; z < 16; z++) {
				for (int y = 1; y < 64; y++) {
					b[x * 2048 + z * 128 + y] = liquid;
				}
				int deep = 0;

				double _y = noiseTerrainHeight.noise(x + chunkX, z + chunkZ, 0.5, 0.5, true) + 1;
				_y = _y * _y * 4;
				if (_y < 8) {
					_y = _y * _y / 4 - 8;
				}

				for (int y = 56 + (int) _y; y > 0; y--) {
					if (deep < noiseTerrainType.noise(x + chunkX, z + chunkZ, 0.5, 0.5, true) * 3 + 5) {
						b[x * 2048 + z * 128 + y] = sand;
					} else if (deep < noiseTerrainType2.noise(x + chunkX, z
							+ chunkZ, 0.5, 0.5, true) * 4 + 7) {
						b[x * 2048 + z * 128 + y] = sandstone;
					} else {
						b[x * 2048 + z * 128 + y] = stone;
					}
					deep++;
				}
				b[x * 2048 + z * 128] = bedrock;
			}
		}

		return b;
	}


	@Override
	protected void createWorldOctaves(World world, Map<String, OctaveGenerator> octaves) {
		Random seed = new Random(world.getSeed());

		OctaveGenerator gen = new SimplexOctaveGenerator(seed, 16);
		gen.setScale(1 / 128.0);
		octaves.put("terrainHeight", gen);

		gen = new SimplexOctaveGenerator(seed, 4);
		gen.setScale(1 / 64.0);
		octaves.put("terrainType", gen);

		gen = new SimplexOctaveGenerator(seed, 4);
		gen.setScale(1 / 32.0);
		octaves.put("terrainType2", gen);
	}
}
