package com.main.worldgenerator.generator;

import com.aparapi.Kernel;

public class OpenSimplex2NoiseGenerator extends Kernel {

    private static final long PRIME_X = 0x5205402B9270C86FL;
    private static final long PRIME_Y = 0x598CD327003817B5L;
    private static final long PRIME_Z = 0x5BCC226E9FA0BACBL;
    private static final long HASH_MULTIPLIER = 0x53A3F72DEEC546F5L;
    private static final long SEED_FLIP_3D = -0x52D547B2E96ED629L;
    private static final double UNSKEW_2D = -0.21132486540518713;

    private static final double ROOT3OVER3 = 0.577350269189626;
    private static final double ROTATE_3D_ORTHOGONALIZER = UNSKEW_2D;

    private static final int N_GRADS_3D_EXPONENT = 8;
    private static final int N_GRADS_3D = 1 << N_GRADS_3D_EXPONENT;

    private static final double NORMALIZER_3D = 0.07969837668935331;

    private static final float RSQUARED_3D = 0.6f;

    private float[] r = { 0 };

    private float[] argsFloat = { 0, 0, 0 };

    public float[] getResult() {
        return r;
    }

    public OpenSimplex2NoiseGenerator() {
        for (int i = 0; i < grad3.length; i++) {
            grad3[i] = (float)(grad3[i] / NORMALIZER_3D);
        }
        for (int i = 0, j = 0; i < GRADIENTS_3D.length; i++, j++) {
            if (j == grad3.length) j = 0;
            GRADIENTS_3D[i] = grad3[j];
        }
    }

    private long[] SEED;

    public void setParameters(float x, float y, float z, long seed) {
        argsFloat = new float[] {x, y, z};
        r = new float[1];
        SEED = new long[] {seed};
    }

    @Override
    public void run() {
        int i = getGlobalId();
        r[i] = noise3_ImproveXZ(SEED[0], argsFloat[0], argsFloat[1], argsFloat[2]);
    }

    public float noise3_ImproveXZ(long seed, double x, double y, double z) {

        // Re-orient the cubic lattices without skewing, so Y points up the main lattice diagonal,
        // and the planes formed by XZ are moved far out of alignment with the cube faces.
        // Orthonormal rotation. Not a skew transform.
        double xz = x + z;
        double s2 = xz * ROTATE_3D_ORTHOGONALIZER;
        double yy = y * ROOT3OVER3;
        double xr = x + s2 + yy;
        double zr = z + s2 + yy;
        double yr = xz * -ROOT3OVER3 + yy;

        // Evaluate both lattices to form a BCC lattice.
        return noise3_UnrotatedBase(seed, xr, yr, zr);
    }

    /**
     * Generate overlapping cubic lattices for 3D OpenSimplex2 noise.
     */
    private float noise3_UnrotatedBase(long seed, double xr, double yr, double zr) {

        // Get base points and offsets.
        int xrb = fastRound(xr), yrb = fastRound(yr), zrb = fastRound(zr);
        float xri = (float)(xr - xrb), yri = (float)(yr - yrb), zri = (float)(zr - zrb);

        // -1 if positive, 1 if negative.
        int xNSign = (int)(-1.0f - xri) | 1, yNSign = (int)(-1.0f - yri) | 1, zNSign = (int)(-1.0f - zri) | 1;

        // Compute absolute values, using the above as a shortcut. This was faster in my tests for some reason.
        float ax0 = xNSign * -xri, ay0 = yNSign * -yri, az0 = zNSign * -zri;

        // Prime pre-multiplication for hash.
        long xrbp = xrb * PRIME_X, yrbp = yrb * PRIME_Y, zrbp = zrb * PRIME_Z;

        // Loop: Pick an edge on each lattice copy.
        float value = 0;
        float a = (RSQUARED_3D - xri * xri) - (yri * yri + zri * zri);
        for (int l = 0; l < 2; l++) {
            // Closest point on cube.
            if (a > 0) {
                value += (a * a) * (a * a) * grad(seed, xrbp, yrbp, zrbp, xri, yri, zri);
            }

            // Second-closest point.
            if (ax0 >= ay0) {
                float b = a + ax0 + ax0;
                if (ax0 >= az0) {
                    if (b > 1) {
                        b -= 1;
                        value += (b * b) * (b * b) * grad(seed, xrbp - xNSign * PRIME_X, yrbp, zrbp, xri + xNSign, yri, zri);
                    }
                    else
                    {
                        b+=1;
                    }
                }
                else
                {
                    b+=1;
                }
            }
            else if (ay0 >= az0) {
                float b = a + ay0 + ay0;
                if (b > 1) {
                    b -= 1;
                    value += (b * b) * (b * b) * grad(seed, xrbp, yrbp - yNSign * PRIME_Y, zrbp, xri, yri + yNSign, zri);
                }
                else
                {
                    b+=1;
                }
            }
            else
            {
                float b = a + az0 + az0;
                if (b > 1) {
                    b -= 1;
                    value += (b * b) * (b * b) * grad(seed, xrbp, yrbp, zrbp - zNSign * PRIME_Z, xri, yri, zri + zNSign);
                }
                else
                {
                    b+=1;
                }
            }

            // Break from loop if we're done, skipping updates below.

            // Update absolute value.
            ax0 = 0.5f - ax0;
            ay0 = 0.5f - ay0;
            az0 = 0.5f - az0;

            // Update relative coordinate.
            xri = xNSign * ax0;
            yri = yNSign * ay0;
            zri = zNSign * az0;

            // Update falloff.
            a += (0.75f - ax0) - (ay0 + az0);

            // Update prime for hash.
            xrbp += (xNSign >> 1) & PRIME_X;
            yrbp += (yNSign >> 1) & PRIME_Y;
            zrbp += (zNSign >> 1) & PRIME_Z;

            // Update the reverse sign indicators.
            xNSign = -xNSign;
            yNSign = -yNSign;
            zNSign = -zNSign;

            // And finally update the seed for the other lattice copy.
            seed ^= SEED_FLIP_3D;
        }

        return value;
    }


    private  float grad(long seed, long xrvp, long yrvp, long zrvp, float dx, float dy, float dz) {
        long hash = (seed ^ xrvp) ^ (yrvp ^ zrvp);
        hash *= HASH_MULTIPLIER;
        hash ^= hash >> (64 - N_GRADS_3D_EXPONENT + 2);
        int gi = (int)hash & ((N_GRADS_3D - 1) << 2);
        return GRADIENTS_3D[gi | 0] * dx + GRADIENTS_3D[gi | 1] * dy + GRADIENTS_3D[gi | 2] * dz;
    }

    private static int fastRound(double x) {
        return x < 0 ? (int)(x - 0.5) : (int)(x + 0.5);
    }

    /*
     * gradients
     */

    private final float[] GRADIENTS_3D = new float[N_GRADS_3D * 4];

    float[] grad3 = {
            2.22474487139f,       2.22474487139f,      -1.0f,                 0.0f,
            2.22474487139f,       2.22474487139f,       1.0f,                 0.0f,
            3.0862664687972017f,  1.1721513422464978f,  0.0f,                 0.0f,
            1.1721513422464978f,  3.0862664687972017f,  0.0f,                 0.0f,
            -2.22474487139f,       2.22474487139f,      -1.0f,                 0.0f,
            -2.22474487139f,       2.22474487139f,       1.0f,                 0.0f,
            -1.1721513422464978f,  3.0862664687972017f,  0.0f,                 0.0f,
            -3.0862664687972017f,  1.1721513422464978f,  0.0f,                 0.0f,
            -1.0f,                -2.22474487139f,      -2.22474487139f,       0.0f,
            1.0f,                -2.22474487139f,      -2.22474487139f,       0.0f,
            0.0f,                -3.0862664687972017f, -1.1721513422464978f,  0.0f,
            0.0f,                -1.1721513422464978f, -3.0862664687972017f,  0.0f,
            -1.0f,                -2.22474487139f,       2.22474487139f,       0.0f,
            1.0f,                -2.22474487139f,       2.22474487139f,       0.0f,
            0.0f,                -1.1721513422464978f,  3.0862664687972017f,  0.0f,
            0.0f,                -3.0862664687972017f,  1.1721513422464978f,  0.0f,
            //--------------------------------------------------------------------//
            -2.22474487139f,      -2.22474487139f,      -1.0f,                 0.0f,
            -2.22474487139f,      -2.22474487139f,       1.0f,                 0.0f,
            -3.0862664687972017f, -1.1721513422464978f,  0.0f,                 0.0f,
            -1.1721513422464978f, -3.0862664687972017f,  0.0f,                 0.0f,
            -2.22474487139f,      -1.0f,                -2.22474487139f,       0.0f,
            -2.22474487139f,       1.0f,                -2.22474487139f,       0.0f,
            -1.1721513422464978f,  0.0f,                -3.0862664687972017f,  0.0f,
            -3.0862664687972017f,  0.0f,                -1.1721513422464978f,  0.0f,
            -2.22474487139f,      -1.0f,                 2.22474487139f,       0.0f,
            -2.22474487139f,       1.0f,                 2.22474487139f,       0.0f,
            -3.0862664687972017f,  0.0f,                 1.1721513422464978f,  0.0f,
            -1.1721513422464978f,  0.0f,                 3.0862664687972017f,  0.0f,
            -1.0f,                 2.22474487139f,      -2.22474487139f,       0.0f,
            1.0f,                 2.22474487139f,      -2.22474487139f,       0.0f,
            0.0f,                 1.1721513422464978f, -3.0862664687972017f,  0.0f,
            0.0f,                 3.0862664687972017f, -1.1721513422464978f,  0.0f,
            -1.0f,                 2.22474487139f,       2.22474487139f,       0.0f,
            1.0f,                 2.22474487139f,       2.22474487139f,       0.0f,
            0.0f,                 3.0862664687972017f,  1.1721513422464978f,  0.0f,
            0.0f,                 1.1721513422464978f,  3.0862664687972017f,  0.0f,
            2.22474487139f,      -2.22474487139f,      -1.0f,                 0.0f,
            2.22474487139f,      -2.22474487139f,       1.0f,                 0.0f,
            1.1721513422464978f, -3.0862664687972017f,  0.0f,                 0.0f,
            3.0862664687972017f, -1.1721513422464978f,  0.0f,                 0.0f,
            2.22474487139f,      -1.0f,                -2.22474487139f,       0.0f,
            2.22474487139f,       1.0f,                -2.22474487139f,       0.0f,
            3.0862664687972017f,  0.0f,                -1.1721513422464978f,  0.0f,
            1.1721513422464978f,  0.0f,                -3.0862664687972017f,  0.0f,
            2.22474487139f,      -1.0f,                 2.22474487139f,       0.0f,
            2.22474487139f,       1.0f,                 2.22474487139f,       0.0f,
            1.1721513422464978f,  0.0f,                 3.0862664687972017f,  0.0f,
            3.0862664687972017f,  0.0f,                 1.1721513422464978f,  0.0f,
    };


}
