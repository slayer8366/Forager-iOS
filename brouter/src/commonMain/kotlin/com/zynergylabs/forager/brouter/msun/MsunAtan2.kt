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
 * e_atan2.c: Developed at SunSoft, a Sun Microsystems, Inc. business.
 */
package com.zynergylabs.forager.brouter.msun

import kotlin.math.abs

// Step 2b (dispatch 2026-10-28-01, Amendment 3, Part B; RECORD -24): Android's double atan2, ported from bionic's FreeBSD
// msun at android-17.0.0_r1 (bionic 06356e41): e_atan2.c with s_atan.c (tools/msun-ref/sources.tsv). msun's evaluation
// order is kept operation for operation, as in MsunCos.kt. C's `volatile` on tiny, pi_lo and atanlo[3] only stops the
// compiler folding them, to raise inexact; the values are the same, and Kotlin has no floating-point flags to raise.

// e_atan2.c
private const val tiny = 1.0e-300
private const val zero = 0.0
private const val pi_o_4 = 7.8539816339744827900E-01 /* 0x3FE921FB, 0x54442D18 */
private const val pi_o_2 = 1.5707963267948965580E+00 /* 0x3FF921FB, 0x54442D18 */
private const val pi = 3.1415926535897931160E+00 /* 0x400921FB, 0x54442D18 */
private const val pi_lo = 1.2246467991473531772E-16 /* 0x3CA1A626, 0x33145C07 */

// s_atan.c
private val atanhi = doubleArrayOf(
    4.63647609000806093515e-01, /* atan(0.5)hi 0x3FDDAC67, 0x0561BB4F */
    7.85398163397448278999e-01, /* atan(1.0)hi 0x3FE921FB, 0x54442D18 */
    9.82793723247329054082e-01, /* atan(1.5)hi 0x3FEF730B, 0xD281F69B */
    1.57079632679489655800e+00, /* atan(inf)hi 0x3FF921FB, 0x54442D18 */
)
private val atanlo = doubleArrayOf(
    2.26987774529616870924e-17, /* atan(0.5)lo 0x3C7A2B7F, 0x222F65E2 */
    3.06161699786838301793e-17, /* atan(1.0)lo 0x3C81A626, 0x33145C07 */
    1.39033110312309984516e-17, /* atan(1.5)lo 0x3C700788, 0x7AF0CBBD */
    6.12323399573676603587e-17, /* atan(inf)lo 0x3C91A626, 0x33145C07 */
)
private val aT = doubleArrayOf(
    3.33333333333329318027e-01, /* 0x3FD55555, 0x5555550D */
    -1.99999999998764832476e-01, /* 0xBFC99999, 0x9998EBC4 */
    1.42857142725034663711e-01, /* 0x3FC24924, 0x920083FF */
    -1.11111104054623557880e-01, /* 0xBFBC71C6, 0xFE231671 */
    9.09088713343650656196e-02, /* 0x3FB745CD, 0xC54C206E */
    -7.69187620504482999495e-02, /* 0xBFB3B0F2, 0xAF749A6D */
    6.66107313738753120669e-02, /* 0x3FB10D66, 0xA0D03D51 */
    -5.83357013379057348645e-02, /* 0xBFADDE2D, 0x52DEFD9A */
    4.97687799461593236017e-02, /* 0x3FA97B4B, 0x24760DEB */
    -3.65315727442169155270e-02, /* 0xBFA2B444, 0x2C6A6C2F */
    1.62858201153657823623e-02, /* 0x3F90AD3A, 0xE322DA11 */
)
private const val one = 1.0
private const val huge = 1.0e300

/**
 * e_atan2.c's atan2(y, x), as bionic builds it for an Android phone. A NaN result is a NaN; its payload is not part of
 * the port's claim (C's nan_mix adds in long double, which is 80-bit on the x86_64 reference and 128-bit on arm64).
 */
fun msunAtan2(y: Double, x: Double): Double {
    val hx = highWord(x)
    val lx = lowWord(x)
    val ix = hx and 0x7fffffff
    val hy = highWord(y)
    val ly = lowWord(y)
    val iy = hy and 0x7fffffff
    if ((ix or ((lx or -lx) ushr 31)) > 0x7ff00000 ||
        (iy or ((ly or -ly) ushr 31)) > 0x7ff00000
    ) { /* x or y is NaN */
        return x + y /* nan_mix(x, y) */
    }
    if (hx == 0x3ff00000 && lx == 0) return atan(y) /* x=1.0 */
    var m = ((hy shr 31) and 1) or ((hx shr 30) and 2) /* 2*sign(x)+sign(y) */

    /* when y = 0 */
    if ((iy or ly) == 0) {
        when (m) {
            0, 1 -> return y /* atan(+-0,+anything)=+-0 */
            2 -> return pi + tiny /* atan(+0,-anything) = pi */
            else -> return -pi - tiny /* atan(-0,-anything) =-pi */
        }
    }
    /* when x = 0 */
    if ((ix or lx) == 0) return if (hy < 0) -pi_o_2 - tiny else pi_o_2 + tiny

    /* when x is INF */
    if (ix == 0x7ff00000) {
        if (iy == 0x7ff00000) {
            when (m) {
                0 -> return pi_o_4 + tiny /* atan(+INF,+INF) */
                1 -> return -pi_o_4 - tiny /* atan(-INF,+INF) */
                2 -> return 3.0 * pi_o_4 + tiny /*atan(+INF,-INF)*/
                else -> return -3.0 * pi_o_4 - tiny /*atan(-INF,-INF)*/
            }
        } else {
            when (m) {
                0 -> return zero /* atan(+...,+INF) */
                1 -> return -zero /* atan(-...,+INF) */
                2 -> return pi + tiny /* atan(+...,-INF) */
                else -> return -pi - tiny /* atan(-...,-INF) */
            }
        }
    }
    /* when y is INF */
    if (iy == 0x7ff00000) return if (hy < 0) -pi_o_2 - tiny else pi_o_2 + tiny

    /* compute y/x */
    val k = (iy - ix) shr 20
    val z: Double
    if (k > 60) { /* |y/x| >  2**60 */
        z = pi_o_2 + 0.5 * pi_lo
        m = m and 1
    } else if (hx < 0 && k < -60) {
        z = 0.0 /* 0 > |y|/x > -2**-60 */
    } else {
        z = atan(abs(y / x)) /* safe to do y/x */
    }
    return when (m) {
        0 -> z /* atan(+,+) */
        1 -> -z /* atan(-,+) */
        2 -> pi - (z - pi_lo) /* atan(+,-) */
        else -> (z - pi_lo) - pi /* atan(-,-) */
    }
}

/** s_atan.c's atan(x). */
private fun atan(x0: Double): Double {
    var x = x0
    val hx = highWord(x)
    val ix = hx and 0x7fffffff
    val id: Int
    if (ix >= 0x44100000) { /* if |x| >= 2^66 */
        val low = lowWord(x)
        if (ix > 0x7ff00000 || (ix == 0x7ff00000 && low != 0)) {
            return x + x /* NaN */
        }
        return if (hx > 0) atanhi[3] + atanlo[3] else -atanhi[3] - atanlo[3]
    }
    if (ix < 0x3fdc0000) { /* |x| < 0.4375 */
        if (ix < 0x3e400000) { /* |x| < 2^-27 */
            if (huge + x > one) return x /* raise inexact */
        }
        id = -1
    } else {
        x = abs(x)
        if (ix < 0x3ff30000) { /* |x| < 1.1875 */
            if (ix < 0x3fe60000) { /* 7/16 <=|x|<11/16 */
                id = 0
                x = (2.0 * x - one) / (2.0 + x)
            } else { /* 11/16<=|x|< 19/16 */
                id = 1
                x = (x - one) / (x + one)
            }
        } else {
            if (ix < 0x40038000) { /* |x| < 2.4375 */
                id = 2
                x = (x - 1.5) / (one + 1.5 * x)
            } else { /* 2.4375 <= |x| < 2^66 */
                id = 3
                x = -1.0 / x
            }
        }
    }
    /* end of argument reduction */
    var z = x * x
    val w = z * z
    /* break sum from i=0 to 10 aT[i]z**(i+1) into odd and even poly */
    val s1 = z * (aT[0] + w * (aT[2] + w * (aT[4] + w * (aT[6] + w * (aT[8] + w * aT[10])))))
    val s2 = w * (aT[1] + w * (aT[3] + w * (aT[5] + w * (aT[7] + w * aT[9]))))
    if (id < 0) return x - x * (s1 + s2)
    z = atanhi[id] - ((x * (s1 + s2) - atanlo[id]) - x)
    return if (hx < 0) -z else z
}
