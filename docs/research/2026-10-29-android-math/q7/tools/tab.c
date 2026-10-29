/* Runs this build's cos() over the table's 1,800 lat inputs (hex bits read from a file, column 2) and prints
   row lat-bits cos-bits. Also recomputes lat as CheapRuler.calcKxKyFromILat / TableRows.row does and counts
   mismatches against the file, as a check that the inputs are the formula's. */
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <stdint.h>
#include <float.h>
#include <math.h>
static uint64_t bits(double d) { uint64_t u; memcpy(&u, &d, 8); return u; }
static double frombits(uint64_t u) { double d; memcpy(&d, &u, 8); return d; }
int main(int argc, char **argv) {
  FILE *f = fopen(argv[1], "r"); char line[512]; int n = 0, latmm = 0;
  if (!f) { perror("open"); return 2; }
#ifdef amd64
  int amd64_defined = 1;
#else
  int amd64_defined = 0;
#endif
  printf("# FLT_EVAL_METHOD=%d sizeof(double_t)=%zu amd64_defined=%d\n", (int) FLT_EVAL_METHOD, sizeof(double_t), amd64_defined);
  while (fgets(line, sizeof line, f)) {
    int row; char lathex[40];
    if (sscanf(line, "%d %39s", &row, lathex) != 2) continue;
    uint64_t lu = strtoull(lathex, 0, 16); double lat = frombits(lu);
    int ilat = row * 100000 + 100000 / 2;
    double lat2 = (3.14159265358979323846 / 180.) * (ilat * 1e-6 - 90);
    if (bits(lat2) != lu) latmm++;
    double c = cos(lat);
    printf("%d %016llx %016llx\n", row, (unsigned long long) lu, (unsigned long long) bits(c)); n++;
  }
  fprintf(stderr, "rows %d lat-recompute-mismatch %d\n", n, latmm);
  return 0;
}
