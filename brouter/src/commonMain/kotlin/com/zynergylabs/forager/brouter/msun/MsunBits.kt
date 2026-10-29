package com.zynergylabs.forager.brouter.msun

// math_private.h's word access, for the step 2b port (bionic 06356e41, libm/upstream-freebsd/lib/msun/src/
// math_private.h: GET_HIGH_WORD, GET_LOW_WORD, EXTRACT_WORDS). IEEE_WORD_ORDER is BYTE_ORDER on arm64, so the high
// word is the sign, exponent and top 20 fraction bits; reading the raw bits gives the same words on every target.

/** GET_HIGH_WORD: bits 63 to 32 of [x], as int32_t. */
internal fun highWord(x: Double): Int = (x.toRawBits() ushr 32).toInt()

/** GET_LOW_WORD: bits 31 to 0 of [x], as the u_int32_t's bits. */
internal fun lowWord(x: Double): Int = x.toRawBits().toInt()
