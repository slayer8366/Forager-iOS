// Step 2a close: the two loops of BitCoderContextTest.rawBitReads* after the planner's ruling (RECORD -33), on the
// oracle alone, with the test's draws in the test's order. Case 1: reset kept, op cap 1,000,000, position bound kept.
// Case 2: no in-loop reset (so no nextInt(50) draw), position bound only. Logs ops per seed, what ended the loop, and
// the oracle's private idx (read by reflection after every op), to see whether fillBuffer's end-of-array branch
// (`idx++ < idxMax` false, which leaves idx > idxMax) is ever reached.
import btools.util.BitCoderContext;
import java.lang.reflect.Field;
public class LoopCheck2 {
  public static void main(String[] a) throws Exception {
    int kase = Integer.parseInt(a[0]); long cap = Long.parseLong(a[1]); // cap < 0: none
    Field fIdx = BitCoderContext.class.getDeclaredField("idx"); fIdx.setAccessible(true);
    Field fMax = BitCoderContext.class.getDeclaredField("idxMax"); fMax.setAccessible(true);
    long total = 0; int ended = 0;
    for (int seed = 1; seed <= 20; seed++) {
      kotlin.random.Random r = kotlin.random.RandomKt.Random(seed);
      byte[] bytes = r.nextBytes(new byte[4096]);
      BitCoderContext j = new BitCoderContext(bytes);
      int bound = (bytes.length - 16) * 8, idxMax = fMax.getInt(j);
      long i = 0, resets = 0; int maxPos = 0, maxIdx = -1;
      while (j.getReadingBitPosition() < bound && (cap < 0 || i < cap)) {
        int count = r.nextInt(0, 33);
        switch (r.nextInt(5)) {
          case 0: j.decodeBits(count); break;
          case 1: j.decodeBitsReverse(count); break;
          case 2: j.decodeVarBits(); break;
          case 3: j.decodeBit(); break;
          default:
            if (kase == 1 && r.nextInt(50) == 0) { j.reset(); resets++; }
            j.decodeBounded(count);
        }
        i++;
        maxPos = Math.max(maxPos, j.getReadingBitPosition()); maxIdx = Math.max(maxIdx, fIdx.getInt(j));
      }
      boolean byBound = j.getReadingBitPosition() >= bound;
      if (byBound || (cap >= 0 && i >= cap)) ended++;
      total += i;
      System.out.printf("case %d seed %d: ops %d, ended by %s, resets %d, final position %d, max position %d of bound %d, max idx %d of idxMax %d, end-of-array branch %s%n",
        kase, seed, i, byBound ? "position bound" : "op cap", resets, j.getReadingBitPosition(), maxPos, bound, maxIdx, idxMax, maxIdx > idxMax ? "REACHED" : "not reached");
    }
    System.out.printf("case %d: %d of 20 seeds ended; %d ops in all%n", kase, ended, total);
  }
}
