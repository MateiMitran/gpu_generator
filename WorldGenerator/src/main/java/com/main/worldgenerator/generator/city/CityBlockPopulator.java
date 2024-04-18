package com.main.worldgenerator.generator.city;


import com.main.worldgenerator.MaterialIds;

import java.util.Random;

/**
 * @author Nightgunner5
 */
public interface CityBlockPopulator extends MaterialIds {
	public void populate(int originX, int originZ, int locX, int locZ, int height, byte[] b, Random random);
}
