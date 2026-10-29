package com.zynergylabs.forager.brouter.msun

/**
 * The port's named refusal (dispatch 2026-10-28-01, Amendment 3, Part B, step 2b): an argument msun would hand to
 * `__kernel_rem_pio2` (`k_rem_pio2.c`), which is not ported. For `cos` that is a finite |x| whose high word is not below
 * `0x413921fb` (`e_rem_pio2.c` at bionic `06356e41`): |x| >= 1,647,099.0 exactly (0x1.921fbp+20, high word 0x413921fb,
 * low word 0). No latitude reaches it: |lat| <= pi/2.
 */
class MsunUnsupportedException(val function: String, val argument: Double) :
    UnsupportedOperationException(
        "$function(${argument}) is unsupported: it needs k_rem_pio2.c's reduction, which is not ported " +
            "(bits ${argument.toRawBits().toULong().toString(16).padStart(16, '0')})",
    )
