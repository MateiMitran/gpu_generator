/**
 *
 */
package com.main.worldgenerator.populators;

import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

import java.util.Random;

/**
 * @author Nightgunner5
 */
public class ShipwreckPopulator extends BuildingPopulator {
	@SuppressWarnings("javadoc")
	public ShipwreckPopulator() {
		super("shipwrecks");
	}

	/**
	 * @see org.bukkit.generator.BlockPopulator#populate(World,
	 *      Random, Chunk)
	 */
	@Override
	public void populate(World world, Random random, Chunk source) {

		int x = random.nextInt(16);
		int z = random.nextInt(16);

		Block block = source.getBlock(x, 127, z);
		while (block.isEmpty() || block.isLiquid()) {
			block = block.getRelative(BlockFace.DOWN);
		}
		block = block.getRelative(BlockFace.UP);

		Building building = getRandomBuilding(block, random);
		if (building != null) {
			building.maybePlaceDestroyed(block, random, 20, 60);
		}
	}
}
