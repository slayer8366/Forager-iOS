/*
 * ====================================================
 * Copyright (C) 1993 by Sun Microsystems, Inc. All rights reserved.
 *
 * Developed at SunPro, a Sun Microsystems, Inc. business.
 * Permission to use, copy, modify, and distribute this
 * software is freely granted, provided that this notice
 * is preserved.
 * ====================================================
 *
 * e_rem_pio2.c: Developed at SunSoft, a Sun Microsystems, Inc. business. Optimized by Bruce D. Evans.
 * k_cos.c, k_sin.c: Developed at SunSoft, a Sun Microsystems, Inc. business.
 */
package com.zynergylabs.forager.brouter.msun

// Step 2b (dispatch 2026-10-28-01, Amendment 3, Part B; RECORD -24, "Copy Android's maths"): Android's double cos, ported
// from bionic's FreeBSD msun at android-17.0.0_r1 (bionic 06356e41): s_cos.c, with e_rem_pio2.c inlined as s_cos.c
// includes it, k_cos.c and k_sin.c (sizes and SHA-256 in tools/msun-ref/sources.tsv). msun's evaluation order is kept
// operation for operation; each Kotlin expression is the C expression with the same parenthesisation, and Kotlin, like
// C under Android's -ffp-contract=off, rounds each operation separately. Constants are msun's decimal literals; both
// compilers round them to the bits in the comments. k_rem_pio2.c is not ported: its arguments throw
// MsunUnsupportedException.

private const val one = 1.0

// k_cos.c
private const val C1 = 4.16666666666666019037e-02 /* 0x3FA55555, 0x5555554C */
private const val C2 = -1.38888888888741095749e-03 /* 0xBF56C16C, 0x16C15177 */
private const val C3 = 2.48015872894767294178e-05 /* 0x3EFA01A0, 0x19CB1590 */
private const val C4 = -2.75573143513906633035e-07 /* 0xBE927E4F, 0x809C52AD */
private const val C5 = 2.08757232129817482790e-09 /* 0x3E21EE9E, 0xBDB4B1C4 */
private const val C6 = -1.13596475577881948265e-11 /* 0xBDA8FAE9, 0xBE8838D4 */

// k_sin.c
private const val half = 5.00000000000000000000e-01 /* 0x3FE00000, 0x00000000 */
private const val S1 = -1.66666666666666324348e-01 /* 0xBFC55555, 0x55555549 */
private const val S2 = 8.33333333332248946124e-03 /* 0x3F811111, 0x1110F8A6 */
private const val S3 = -1.98412698298579493134e-04 /* 0xBF2A01A0, 0x19C161D5 */
private const val S4 = 2.75573137070700676789e-06 /* 0x3EC71DE3, 0x57B1FE7D */
private const val S5 = -2.50507602534068634195e-08 /* 0xBE5AE5E6, 0x8A2B9CEB */
private const val S6 = 1.58969099521155010221e-10 /* 0x3DE5D93A, 0x5ACFD57C */

// e_rem_pio2.c
private const val invpio2 = 6.36619772367581382433e-01 /* 0x3FE45F30, 0x6DC9C883 */
private const val pio2_1 = 1.57079632673412561417e+00 /* 0x3FF921FB, 0x54400000 */
private const val pio2_1t = 6.07710050650619224932e-11 /* 0x3DD0B461, 0x1A626331 */
private const val pio2_2 = 6.07710050630396597660e-11 /* 0x3DD0B461, 0x1A600000 */
private const val pio2_2t = 2.02226624879595063154e-21 /* 0x3BA3198A, 0x2E037073 */
private const val pio2_3 = 2.02226624871116645580e-21 /* 0x3BA3198A, 0x2E000000 */
private const val pio2_3t = 8.47842766036889956997e-32 /* 0x397B839A, 0x252049C1 */

/** math_private.h's rnint(): 0x1.8p52 is 6755399441055744.0, exactly. */
private fun rnint(x: Double): Double = (x + 6755399441055744.0) - 6755399441055744.0

/** s_cos.c's cos(), as bionic builds it for an Android phone. Throws [MsunUnsupportedException] in k_rem_pio2.c's range. */
fun msunCos(x: Double): Double {
    val y = DoubleArray(2)
    val z = 0.0

    /* High word of x. */
    var ix = highWord(x)

    /* |x| ~< pi/4 */
    ix = ix and 0x7fffffff
    if (ix <= 0x3fe921fb) {
        if (ix < 0x3e46a09e) { /* if x < 2**-27 * sqrt(2) */
            if (x.toInt() == 0) return 1.0 /* generate inexact */
        }
        return kernelCos(x, z)
    } else if (ix >= 0x7ff00000) {
        /* cos(Inf or NaN) is NaN */
        return x - x
    } else {
        /* argument reduction needed */
        val n = remPio2(x, y)
        return when (n and 3) {
            0 -> kernelCos(y[0], y[1])
            1 -> -kernelSin(y[0], y[1], 1)
            2 -> -kernelCos(y[0], y[1])
            else -> kernelSin(y[0], y[1], 1)
        }
    }
}

/** k_cos.c's __kernel_cos(x, y): FreeBSD's kernel, not fdlibm 5.3's (research/android-math, Q2, run 2). */
private fun kernelCos(x: Double, y: Double): Double {
    val z = x * x
    var w = z * z
    val r = z * (C1 + z * (C2 + z * C3)) + w * w * (C4 + z * (C5 + z * C6))
    val hz = 0.5 * z
    w = one - hz
    return w + (((one - w) - hz) + (z * r - x * y))
}

/** k_sin.c's __kernel_sin(x, y, iy). */
private fun kernelSin(x: Double, y: Double, iy: Int): Double {
    val z = x * x
    val w = z * z
    val r = S2 + z * (S3 + z * S4) + z * w * (S5 + z * S6)
    val v = z * x
    return if (iy == 0) {
        x + v * (S1 + z * r)
    } else {
        x - ((z * (half * y - v * r) - y) - v * S1)
    }
}

/**
 * e_rem_pio2.c's __ieee754_rem_pio2(x, y), as s_cos.c inlines it. C's `goto medium` becomes the flag [medium]; the
 * branches run in C's order. The "large arguments" tail, which calls __kernel_rem_pio2, throws instead.
 */
private fun remPio2(x: Double, y: DoubleArray): Int {
    val hx = highWord(x) /* high word of x */
    val ix = hx and 0x7fffffff
    var medium = false
    if (ix <= 0x400f6a7a) { /* |x| ~<= 5pi/4 */
        if ((ix and 0xfffff) == 0x921fb) { /* |x| ~= pi/2 or 2pi/2 */
            medium = true /* cancellation -- use medium case */
        } else if (ix <= 0x4002d97c) { /* |x| ~<= 3pi/4 */
            if (hx > 0) {
                val z = x - pio2_1 /* one round good to 85 bits */
                y[0] = z - pio2_1t
                y[1] = (z - y[0]) - pio2_1t
                return 1
            } else {
                val z = x + pio2_1
                y[0] = z + pio2_1t
                y[1] = (z - y[0]) + pio2_1t
                return -1
            }
        } else {
            if (hx > 0) {
                val z = x - 2 * pio2_1
                y[0] = z - 2 * pio2_1t
                y[1] = (z - y[0]) - 2 * pio2_1t
                return 2
            } else {
                val z = x + 2 * pio2_1
                y[0] = z + 2 * pio2_1t
                y[1] = (z - y[0]) + 2 * pio2_1t
                return -2
            }
        }
    }
    if (!medium && ix <= 0x401c463b) { /* |x| ~<= 9pi/4 */
        if (ix <= 0x4015fdbc) { /* |x| ~<= 7pi/4 */
            if (ix == 0x4012d97c) { /* |x| ~= 3pi/2 */
                medium = true
            } else if (hx > 0) {
                val z = x - 3 * pio2_1
                y[0] = z - 3 * pio2_1t
                y[1] = (z - y[0]) - 3 * pio2_1t
                return 3
            } else {
                val z = x + 3 * pio2_1
                y[0] = z + 3 * pio2_1t
                y[1] = (z - y[0]) + 3 * pio2_1t
                return -3
            }
        } else {
            if (ix == 0x401921fb) { /* |x| ~= 4pi/2 */
                medium = true
            } else if (hx > 0) {
                val z = x - 4 * pio2_1
                y[0] = z - 4 * pio2_1t
                y[1] = (z - y[0]) - 4 * pio2_1t
                return 4
            } else {
                val z = x + 4 * pio2_1
                y[0] = z + 4 * pio2_1t
                y[1] = (z - y[0]) + 4 * pio2_1t
                return -4
            }
        }
    }
    if (medium || ix < 0x413921fb) { /* |x| ~< 2^20*(pi/2), medium size */
        val fn = rnint(x * invpio2)
        val n = fn.toInt() /* irint(): (int) on arm64 */
        var r = x - fn * pio2_1
        var w = fn * pio2_1t /* 1st round good to 85 bit */
        val j = ix shr 20
        y[0] = r - w
        var high = highWord(y[0])
        var i = j - ((high ushr 20) and 0x7ff)
        if (i > 16) { /* 2nd iteration needed, good to 118 */
            var t = r
            w = fn * pio2_2
            r = t - w
            w = fn * pio2_2t - ((t - r) - w)
            y[0] = r - w
            high = highWord(y[0])
            i = j - ((high ushr 20) and 0x7ff)
            if (i > 49) { /* 3rd iteration need, 151 bits acc */
                t = r /* will cover all possible cases */
                w = fn * pio2_3
                r = t - w
                w = fn * pio2_3t - ((t - r) - w)
                y[0] = r - w
            }
        }
        y[1] = (r - y[0]) - w
        return n
    }
    /* all other (large) arguments */
    if (ix >= 0x7ff00000) { /* x is inf or NaN; unreachable from msunCos, which returns x-x first */
        y[0] = x - x
        y[1] = y[0]
        return 0
    }
    throw MsunUnsupportedException("cos", x)
}
