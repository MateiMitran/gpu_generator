package com.main.worldgenerator.generator;

import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.*;

public class OpenSimplexNoiseTest
{
    private static final int WIDTH = 512;
    private static final int HEIGHT = 512;
    private static final double FEATURE_SIZE = 24;

    public static void main(String[] args)
            throws IOException {

        OpenSimplex2NoiseGenerator openSimplex2NoiseGenerator = new OpenSimplex2NoiseGenerator();
        BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        for (int y = 0; y < HEIGHT; y++)
        {
            for (int x = 0; x < WIDTH; x++)
            {
                double value = openSimplex2NoiseGenerator.noise3_ImproveXZ(12345L,x / FEATURE_SIZE, y / FEATURE_SIZE, 0.0);
                int rgb = 0x010101 * (int)((value + 1) * 127.5);
                image.setRGB(x, y, rgb);
            }
        }
        ImageIO.write(image, "png", new File("noise.png"));
    }
}