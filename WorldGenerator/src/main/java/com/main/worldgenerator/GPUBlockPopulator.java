package com.main.worldgenerator;

import com.main.worldgenerator.generator.NewChunkGeneratorGPU;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.generator.BlockPopulator;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public abstract class GPUBlockPopulator extends BlockPopulator implements MaterialIds {
	private final Map<World, NewChunkGeneratorGPU> generators = new HashMap<>();

	private Core core;

	private NewChunkGeneratorGPU defgen;

	public GPUBlockPopulator(Core core) {
		this.core = core;
		this.defgen = new NewChunkGeneratorGPU(core);
	}

	public GPUBlockPopulator() {

	}

	public BlockPopulator setDefault(NewChunkGeneratorGPU generator) {
		defgen = generator;
		return this;
	}


	public void populate(NewChunkGeneratorGPU generator, World world,
		Random random, Chunk source) {
		generators.put(world, generator);
		populate(world, random, source);
	}

	private NewChunkGeneratorGPU getGen(World world) {
		if (generators.containsKey(world))
			return generators.get(world);
		return defgen;
	}

	protected static boolean setBlock(World world, int x, int y, int z,
		Material type) {
		Block block = world.getBlockAt(x, y, z);
		if (block.getType() != Material.AIR)
			return false;
		block.setType(type);
		return true;
	}


	protected static boolean setBlock(World world, int x, int y, int z,
		Material type, byte data) {
		Block block = world.getBlockAt(x, y, z);
		if (block.getType() != Material.AIR)
			return false;
		block.setType(type, true);
		return true;
	}
}
