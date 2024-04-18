package com.main.worldgenerator.populators;

import com.main.worldgenerator.Core;
import com.main.worldgenerator.GPUBlockPopulator;
import org.bukkit.Chunk;
import org.bukkit.ChunkSnapshot;
import org.bukkit.Material;
import org.bukkit.World;

import java.util.Random;


public class BoulderPopulator extends GPUBlockPopulator {
	private static final Material BOULDER_MATERIAL = Material.STONE;

	public BoulderPopulator(Core core) {
		super(core);
	}


	@Override
	public void populate(World world, Random random, Chunk source) {
		ChunkSnapshot snapshot = source.getChunkSnapshot();
			int x = random.nextInt(16);
			int z = random.nextInt(16);
			int y = snapshot.getHighestBlockYAt(x, z);


			int size = random.nextInt(100 - 2 + 1);
			int size2 = size * size;

			for (int i = -size; i < size; i++) {
				for (int j = -size; j < size; j++) {
					for (int k = -size; k < size; k++) {
						if (i * i + j * j + k * k < size2
								+ random.nextInt(1 + 2)) {
							setBlock(world, x + i + source.getX() * 16, y + j, z
									+ k + source.getZ() * 16, BOULDER_MATERIAL);
						}
					}
				}
			}
	}
}
