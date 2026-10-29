/* Included after freebsd-compat.h (so after glibc's <sys/cdefs.h>). glibc defines __CONCAT(x,y) as x ## y in one
   level, which does not expand LDBL_MANT_DIG in math_private.h's rnintl() (long double; not on the cos/atan2 path).
   bionic's <sys/cdefs.h> uses the two-level form, which does. This restores that form. */
#undef __CONCAT
#define __CONCAT1(x,y) x ## y
#define __CONCAT(x,y) __CONCAT1(x,y)
