package com.main.worldgenerator.populators;

import com.main.worldgenerator.GPUBlockPopulator;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;

import java.util.Random;

/**
 * BlockPopulator that adds trees based on the biome.
 *
 * @author heldplayer
 */
public class TreePopulator extends GPUBlockPopulator {
	private static final int LOG = 17;
	private static final int LEAVES = 18;

	/**
	 * @see org.bukkit.generator.BlockPopulator#populate(World,
	 *      Random, Chunk)
	 */
	@Override
	public void populate(World world, Random random, Chunk source) {


		int centerX = (source.getX() << 4) + random.nextInt(16);
		int centerZ = (source.getZ() << 4) + random.nextInt(16);

		byte data = 0;
		int chance = 0;
		int height = 4 + random.nextInt(3);
		int multiplier = 1;

		if (random.nextBoolean()) {
			data = 2;
			height = 5 + random.nextInt(3);
		}

		switch (world.getBlockAt(centerX, 0, centerZ).getBiome()) {
		case FOREST:
			chance = 160;
			multiplier = 10;
			break;
		case PLAINS:
			chance = 40;
			break;

		case SAVANNA:
			chance = 20;
			break;

		case TAIGA:
			chance = 120;
			data = 1;
			height = 8 + random.nextInt(3);
			multiplier = 3;
			break;

		case DESERT:

		}

		for (int i = 0; i < multiplier; i++) {
			centerX = (source.getX() << 4) + random.nextInt(16);
			centerZ = (source.getZ() << 4) + random.nextInt(16);
				int centerY = world.getHighestBlockYAt(centerX, centerZ) - 1;
				Block sourceBlock = world.getBlockAt(centerX, centerY, centerZ);

				if (sourceBlock.getType() == Material.GRASS) {
					world.getBlockAt(centerX, centerY + height + 1, centerZ).setType(Material.OAK_LEAVES, true);

					for (int j = 0; j < 4; j++) {
						world.getBlockAt(centerX, centerY + height + 1 - j, centerZ - 1).setType(Material.OAK_LEAVES, true);
						world.getBlockAt(centerX, centerY + height + 1 - j, centerZ + 1).setType(Material.OAK_LEAVES, true);
						world.getBlockAt(centerX - 1, centerY + height + 1 - j, centerZ).setType(Material.OAK_LEAVES,true);
						world.getBlockAt(centerX + 1, centerY + height + 1 - j, centerZ).setType(Material.OAK_LEAVES, true);
					}

					if (random.nextBoolean()) {
						world.getBlockAt(centerX + 1, centerY + height, centerZ + 1).setType(Material.OAK_LEAVES, true);
					}
					if (random.nextBoolean()) {
						world.getBlockAt(centerX + 1, centerY + height, centerZ - 1).setType(Material.OAK_LEAVES, true);
					}
					if (random.nextBoolean()) {
						world.getBlockAt(centerX - 1, centerY + height, centerZ + 1).setType(Material.OAK_LEAVES, true);
					}
					if (random.nextBoolean()) {
						world.getBlockAt(centerX - 1, centerY + height, centerZ - 1).setType(Material.OAK_LEAVES, true);
					}

					world.getBlockAt(centerX + 1, centerY + height - 1, centerZ + 1).setType(Material.OAK_LEAVES, true);
					world.getBlockAt(centerX + 1, centerY + height - 1, centerZ - 1).setType(Material.OAK_LEAVES,  true);
					world.getBlockAt(centerX - 1, centerY + height - 1, centerZ + 1).setType(Material.OAK_LEAVES,  true);
					world.getBlockAt(centerX - 1, centerY + height - 1, centerZ - 1).setType(Material.OAK_LEAVES,  true);
					world.getBlockAt(centerX + 1, centerY + height - 2, centerZ + 1).setType(Material.OAK_LEAVES, true);
					world.getBlockAt(centerX + 1, centerY + height - 2, centerZ - 1).setType(Material.OAK_LEAVES,  true);
					world.getBlockAt(centerX - 1, centerY + height - 2, centerZ + 1).setType(Material.OAK_LEAVES, true);
					world.getBlockAt(centerX - 1, centerY + height - 2, centerZ - 1).setType(Material.OAK_LEAVES,  true);

					for (int j = 0; j < 2; j++) {
						for (int k = -2; k <= 2; k++) {
							for (int l = -2; l <= 2; l++) {
								world.getBlockAt(centerX + k, centerY + height
										- 1 - j, centerZ + l).setType(Material.OAK_LEAVES, true);
							}
						}
					}

					for (int j = 0; j < 2; j++) {
						if (random.nextBoolean()) {
							world.getBlockAt(centerX + 2, centerY + height - 1
									- j, centerZ + 2).setType(Material.AIR, true);
						}
						if (random.nextBoolean()) {
							world.getBlockAt(centerX + 2, centerY + height - 1
									- j, centerZ - 2).setType(Material.AIR, true);
						}
						if (random.nextBoolean()) {
							world.getBlockAt(centerX - 2, centerY + height - 1
									- j, centerZ + 2).setType(Material.AIR, true);
						}
						if (random.nextBoolean()) {
							world.getBlockAt(centerX - 2, centerY + height - 1
									- j, centerZ - 2).setType(Material.AIR, true);
						}
					}

					// Trunk
					for (int y = 1; y <= height; y++) {
						world.getBlockAt(centerX, centerY + y, centerZ).setType(Material.OAK_LOG, true);
					}
				}
			}
	}

}