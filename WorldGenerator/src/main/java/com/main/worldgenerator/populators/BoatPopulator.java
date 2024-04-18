package com.main.worldgenerator.populators;

import com.main.worldgenerator.Core;
import com.main.worldgenerator.GPUBlockPopulator;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Boat;

import java.util.Random;

public class BoatPopulator extends GPUBlockPopulator {

	/**
	 * @see org.bukkit.generator.BlockPopulator#populate(World,
	 *      Random, Chunk)
	 */
	@Override
	public void populate(World world, Random random, Chunk source) {
		if (random.nextInt(100) < 5) {
			int x = source.getX() * 16 + random.nextInt(16);
			int z = source.getZ() * 16 + random.nextInt(16);
			int y = world.getHighestBlockYAt(x, z);
			createBoat(world, x, y, z);
		}
	}

	private static void createBoat(World w, int x, int y, int z) {
		if (w.getBlockAt(x, y - 1, z).isLiquid()) {
			w.spawn(new Location(w, x, y, z), Boat.class);
		}
	}
}