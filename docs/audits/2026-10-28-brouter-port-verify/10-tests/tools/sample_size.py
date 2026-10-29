#!/usr/bin/env python3
"""Part 8 (item E): exact sample sizes for the differential tests. No rule of thumb.

Model, for one stratum: cases drawn independently from that stratum's generator; each case either
shows a discrepancy or not, with unknown rate p in that population. Zero discrepancies are required.
- Detection: the smallest n with P(no discrepancy seen | rate p) = (1-p)^n <= beta. So
  n = ceil(ln(beta) / ln(1-p)), checked here by the inequality itself at n and n-1.
- The bound after a clean run: with 0 discrepancies in n, the exact one-sided (1-beta) upper
  confidence bound on p (Clopper-Pearson, x = 0) is 1 - beta^(1/n).
- Measuring a rate (option (b) on iOS, hash order under (ii)/(iii)): the exact two-sided
  Clopper-Pearson interval for x of n, by bisection on the binomial tail, printed for a few x.
Rule-of-thumb figures (3/p, 9/p) are printed beside the exact ones only to show they are not used.

Usage: python3 -I sample_size.py
"""
import math


def detect_n(p, beta):
    n = math.ceil(math.log(beta) / math.log(1 - p))
    # check against the defining inequality, so a float slip in the logs cannot pass silently
    assert (1 - p) ** n <= beta, (p, beta, n)
    assert (1 - p) ** (n - 1) > beta, (p, beta, n)
    return n


def upper0(n, beta):
    return 1 - beta ** (1.0 / n)


def binom_cdf(x, n, p):
    # P(X <= x), exact sum in log space
    if p <= 0:
        return 1.0
    if p >= 1:
        return 1.0 if x >= n else 0.0
    s = 0.0
    for k in range(0, x + 1):
        s += math.exp(math.lgamma(n + 1) - math.lgamma(k + 1) - math.lgamma(n - k + 1)
                      + k * math.log(p) + (n - k) * math.log(1 - p))
    return s


def bisect(f, lo, hi):
    for _ in range(200):
        mid = (lo + hi) / 2
        if f(mid):
            hi = mid
        else:
            lo = mid
    return (lo + hi) / 2


def clopper_pearson(x, n, alpha=0.05):
    lo = 0.0 if x == 0 else bisect(lambda p: 1 - binom_cdf(x - 1, n, p) >= alpha / 2, 0.0, 1.0)
    hi = 1.0 if x == n else bisect(lambda p: binom_cdf(x, n, p) <= alpha / 2, 0.0, 1.0)
    return lo, hi


def main():
    beta = 0.05
    print("# beta = %.2f (detection probability 95%%)" % beta)
    print("p\texact_n\t(1-p)^n\t(1-p)^(n-1)\trule_of_three_3/p\t9/p")
    for p in (0.05, 0.01, 0.005, 0.001):
        n = detect_n(p, beta)
        print("%.3f\t%d\t%.6f\t%.6f\t%.1f\t%.1f" % (p, n, (1 - p) ** n, (1 - p) ** (n - 1), 3 / p, 9 / p))

    profiles = 26
    per_profile = detect_n(0.05, beta)
    pooled = profiles * per_profile
    print()
    print("# random stratum R: %d profiles x %d cases = %d" % (profiles, per_profile, pooled))
    print("per-profile clean-run bound (n=%d): p < %.6f" % (per_profile, upper0(per_profile, beta)))
    print("pooled clean-run bound (n=%d): p < %.6f" % (pooled, upper0(pooled, beta)))
    print("pooled: detection probability at p = 0.01: %.6f" % (1 - 0.99 ** pooled))
    print("pooled: detection probability at p = 0.001: %.6f" % (1 - 0.999 ** pooled))
    # a defect confined to one profile, hitting a fraction f of that profile's routes
    for f in (1.0, 0.10, 0.05):
        print("a defect in one profile hitting %.0f%% of its routes: detection probability %.6f"
              % (f * 100, 1 - (1 - f) ** per_profile))

    ios_per_profile = 12
    ios_r = profiles * ios_per_profile
    print()
    print("# iOS fallback subset, if K finds the full list too slow: %d profiles x %d = %d"
          % (profiles, ios_per_profile, ios_r))
    print("exact n for p = 0.01 is %d; the subset has %d >= it: %s" % (detect_n(0.01, beta), ios_r,
                                                                     ios_r >= detect_n(0.01, beta)))
    print("subset clean-run bound: p < %.6f" % upper0(ios_r, beta))

    targeted = {
        "MP64 collecting routes, memoryclass 64": per_profile,
        "ML5 'memory limit reached', memoryclass 5": per_profile,
        "PM5 memory panic mode, memoryclass 5": per_profile,
        "NT waypoint near-ties": per_profile,
        "LB latitude bands": per_profile,
        "CR crossing profile": per_profile,
        "DL-lat 5-degree line at 40N": per_profile,
        "DL-lon 5-degree line at 70W": per_profile,
    }
    print()
    print("# targeted strata, each n = %d: detects a rate >= 5%% with probability >= 95%%" % per_profile)
    for k, v in targeted.items():
        print("%s\t%d\tclean-run bound p < %.6f" % (k, v, upper0(v, beta)))
    fk_kinds = 11
    fk_each = 10
    print("FK failure kinds: %d kinds x %d constructed cases = %d (not a sampling claim)"
          % (fk_kinds, fk_each, fk_kinds * fk_each))
    f0 = 2
    total = pooled + sum(targeted.values()) + fk_kinds * fk_each + f0
    print("F0 Forager's own fixture routes: %d" % f0)
    print("TOTAL cases: %d" % total)

    print()
    print("# measuring a rate: exact 95% Clopper-Pearson intervals")
    for n in (pooled, per_profile):
        for x in (0, 1, 5, 15):
            if x > n:
                continue
            lo, hi = clopper_pearson(x, n)
            print("x=%d of n=%d: %.6f to %.6f" % (x, n, lo, hi))
    # self-check of the interval code against the closed form at x = 0 (two-sided alpha/2)
    lo, hi = clopper_pearson(0, pooled)
    closed = 1 - (0.025) ** (1.0 / pooled)
    assert abs(hi - closed) < 1e-9, (hi, closed)
    print("check: x=0 upper bound by bisection %.9f equals closed form %.9f" % (hi, closed))


if __name__ == "__main__":
    main()
