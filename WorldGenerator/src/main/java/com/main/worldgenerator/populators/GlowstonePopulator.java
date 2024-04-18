// Package Declaration
package com.main.worldgenerator.populators;

// Java Imports

import com.main.worldgenerator.GPUBlockPopulator;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

import java.util.Random;

/**
 * Edited for BananaGen.
 *
 * @author Markus 'Notch' Persson
 * @author iffa
 * @author Nightgunner5
 */
public class GlowstonePopulator extends GPUBlockPopulator {
	private static final BlockFace[] faces = {BlockFace.DOWN, BlockFace.EAST,
		BlockFace.NORTH, BlockFace.SOUTH, BlockFace.UP, BlockFace.WEST};

	/**
	 * Populates a world with glowstone. Easily configurable (but results in
	 * more rare glowstone) by modifying the suitable()-method.
	 */
	@Override
	public void populate(World world, Random random, Chunk source) {
		for (int i = 0; i < 2; i++) {
			int x = random.nextInt(16);
			int y = random.nextInt(128);
			int z = random.nextInt(16);
			while (!suitable(y)) {
				y = random.nextInt(128);
			}
			Block block = source.getBlock(x, y, z);

			block.setType(Material.GLOWSTONE);

			for (int j = 0; j < 1500; j++) {
				Block current = block.getRelative(random.nextInt(8) - random.nextInt(8),
												  random.nextInt(12),
												  random.nextInt(8) - random.nextInt(8));
				if (current.getType() != Material.AIR) {
					continue;
				}
				int count = 0;
				for (BlockFace face : faces) {
					if (current.getRelative(face).getType() == Material.GLOWSTONE) {
						count++;
					}
				}

				if (count == 1) {
					current.setType(Material.GLOWSTONE);
				}
			}
		}
	}


	private static boolean suitable(int y) {
		if (y > 113 && y < 128) {
			return true;
		}
		if (y > 51 && y < 73) {
			return true;
		}
		return false;
	}
}
