/* Runs this build's cos() and atan2() over the edge inputs in edges.txt and prints each input with its result, as
   bits (step 2b, dispatch 2026-10-28-01 Amendment 3). Input lines: "c X" (cos) or "a Y X" (atan2), each operand the
   16 hex digits of a double's bits; "#" lines are comments. Output lines: the input line with the result's bits
   appended. Written for this step beside research/android-math's tab.c and at2.c, in their form. */
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <stdint.h>
#include <math.h>
static uint64_t bits(double d) { uint64_t u; memcpy(&u, &d, 8); return u; }
static double frombits(uint64_t u) { double d; memcpy(&d, &u, 8); return d; }
int main(int argc, char **argv) {
  FILE *f = fopen(argv[1], "r"); char line[512]; int n = 0;
  if (!f) { perror("open"); return 2; }
  while (fgets(line, sizeof line, f)) {
    char op[4], a[40], b[40];
    if (line[0] == '#') continue;
    int k = sscanf(line, "%3s %39s %39s", op, a, b);
    if (k == 2 && strcmp(op, "c") == 0) {
      double x = frombits(strtoull(a, 0, 16));
      printf("c %s %016llx\n", a, (unsigned long long) bits(cos(x))); n++;
    } else if (k == 3 && strcmp(op, "a") == 0) {
      double y = frombits(strtoull(a, 0, 16)), x = frombits(strtoull(b, 0, 16));
      printf("a %s %s %016llx\n", a, b, (unsigned long long) bits(atan2(y, x))); n++;
    } else if (k > 0) {
      fprintf(stderr, "unreadable line: %s", line); return 3;
    }
  }
  fprintf(stderr, "edges %d\n", n);
  return 0;
}
