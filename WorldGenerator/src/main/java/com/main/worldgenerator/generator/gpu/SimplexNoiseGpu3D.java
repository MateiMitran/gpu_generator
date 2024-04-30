package com.main.worldgenerator.generator.gpu;

import com.aparapi.Range;
import com.main.worldgenerator.generator.Noise;

import java.util.Arrays;

public class SimplexNoiseGpu3D {

	private static SimplexNoiseGpu3DKernel simplexKernel = new SimplexNoiseGpu3DKernel();
	private static SimplexNoiseGpu3DKernelIntNoised fastSimplexKernel = new SimplexNoiseGpu3DKernelIntNoised();

	private static SimplexNoiseGpu3DKernelOctaved simplexKernelOctaved = new SimplexNoiseGpu3DKernelOctaved();
	private static SimplexNoiseGpu3DKernelIntNoisedOctaved fastSimplexKernelOctaved = new SimplexNoiseGpu3DKernelIntNoisedOctaved();

	private static Noise noise = new Noise();

	public static synchronized float[] calculate(float x, float y, float z, int width,
			int height, int depth, float frequency) {
		simplexKernel.setParameters(x, y, z, width, height, depth, frequency);
		simplexKernel.execute(width * height * depth);
		return simplexKernel.getResult();
	}

	public static synchronized float[] calculateFast(float x, float y, float z,
			int width, int height, int depth, float frequency) {
		fastSimplexKernel.setParameters(x, y, z, width, height, depth,
				frequency);
		fastSimplexKernel.execute(width * height * depth);
		return fastSimplexKernel.getResult();
	}

	public static synchronized float[] calculateOctaved(float x, float y, float z,
			int width, int height, int depth, float frequency, float[] weight) {
		simplexKernelOctaved.setParameters(x, y, z, width, height, depth,
				frequency, weight,4);
		Range range = Range.create3D(width, height, depth);
		simplexKernelOctaved.execute(range);
		simplexKernelOctaved.dispose();
		return simplexKernelOctaved.getResult();
	}
	public static synchronized float[] calculateFastOctaved(float x, float y, float z,
			int width, int height, int depth, float frequency, float[] weight) {
		fastSimplexKernelOctaved.setExplicit(true);
		fastSimplexKernelOctaved.setParameters(x, y, z, width, height, depth,
				frequency, weight);

		Range range = Range.create(width * height);
		fastSimplexKernelOctaved.execute(range);
		return fastSimplexKernelOctaved.getResult();
	}

	public static synchronized float calculateNew(long seed, float x, float y, float z) {
		noise.setParameters(x, y, z, seed);
		Range range = Range.create(256);
		noise.execute(range);
		return noise.getResult()[0];
	}
}