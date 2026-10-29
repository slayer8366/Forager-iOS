/* What bionic's own headers supply and glibc's do not, for building msun's cos/atan2 on this Linux x86_64 host:
   bionic <sys/types.h> includes <stdint.h> (libc/include/sys/types.h:35 at 06356e41); bionic <math.h> defines
   __double_t/__float_t from __FLT_EVAL_METHOD__ (libc/include/math.h, read 2026-10-29). Here FLT_EVAL_METHOD is 0, as
   on arm64 Android, so both are the plain types. */
#include <stdint.h>
typedef double __double_t;
typedef float __float_t;
