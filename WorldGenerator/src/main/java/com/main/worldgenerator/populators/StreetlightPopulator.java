package com.main.worldgenerator.populators;

import com.main.worldgenerator.GPUBlockPopulator;
import net.llamaslayers.minecraft.banana.gen.BananaBlockPopulator;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;

import java.util.Random;

/**
 * @author Nightgunner5
 */
public class StreetlightPopulator extends GPUBlockPopulator {
	@Override
	public void populate(World world, Random random, Chunk source) {

		for (int i = 0; i < 10; i++) {
			int x = random.nextInt(16);
			int z = random.nextInt(16);
			if (x != 1 && x != 14 && z != 1 && z != 14)
				continue;
			int x2 = x;
			int z2 = z;
			if (x == 1)
				x2 = 0;
			else if (x == 14)
				x2 = 15;
			else if (z == 1)
				z2 = 0;
			else if (z == 14)
				z2 = 15;
			if (source.getBlock(x, 128 / 2, z).getType() == Material.STONE && source.getBlock(x, 128 / 2 + 1, z).getType() == Material.AIR) {
				for (int y = 128 / 2 + 1; y < 128 / 2 + 6; y++) {
					source.getBlock(x, y, z).setType(Material.GLOWSTONE_DUST, false);
				}
				source.getBlock(x2, 128 / 2 + 5, z2).setType(Material.GLOWSTONE_DUST, false);
				source.getBlock(x2, 128 / 2 + 4, z2).setType(Material.GLOWSTONE);
			}
		}
	}
}
