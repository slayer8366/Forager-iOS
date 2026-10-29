/* Runs this build's atan2() over integer pairs "dy dx ..." (one per line) and prints dy dx atan2-bits,
   as CheapAngleMeter.getAngle does: Math.atan2((double) ydiff, (double) xdiff). */
#include <stdio.h>
#include <string.h>
#include <stdint.h>
#include <math.h>
static uint64_t bits(double d) { uint64_t u; memcpy(&u, &d, 8); return u; }
int main(int argc, char **argv) {
  FILE *f = fopen(argv[1], "r"); char line[512]; long n = 0;
  if (!f) { perror("open"); return 2; }
  while (fgets(line, sizeof line, f)) {
    int dy, dx; if (sscanf(line, "%d %d", &dy, &dx) != 2) continue;
    double a = atan2((double) dy, (double) dx);
    printf("%d %d %016llx\n", dy, dx, (unsigned long long) bits(a)); n++;
  }
  fprintf(stderr, "pairs %ld\n", n);
  return 0;
}
