// Replicates BitCoderContextTest.rawBitReadsOnRandomBytesMatchTheOracle's loop on the oracle alone, with a cap.
import btools.util.BitCoderContext;
public class LoopCheck {
  public static void main(String[] a) {
    long cap = Long.parseLong(a[0]);
    for (int seed = 1; seed <= Integer.parseInt(a[1]); seed++) {
      kotlin.random.Random r = kotlin.random.RandomKt.Random(seed);
      byte[] bytes = r.nextBytes(new byte[4096]);
      BitCoderContext j = new BitCoderContext(bytes);
      long i = 0, resets = 0, sinceReset = 0, longestRun = 0; int maxPos = 0;
      long bitsAdvanced = 0;
      while (j.getReadingBitPosition() < (bytes.length - 16) * 8 && i < cap) {
        int before = j.getReadingBitPosition();
        int count = r.nextInt(0, 33);
        int op = r.nextInt(5);
        switch (op) {
          case 0: j.decodeBits(count); break;
          case 1: j.decodeBitsReverse(count); break;
          case 2: j.decodeVarBits(); break;
          case 3: j.decodeBit(); break;
          default:
            if (r.nextInt(50) == 0) { j.reset(); resets++; longestRun = Math.max(longestRun, sinceReset); sinceReset = 0; before = j.getReadingBitPosition(); }
            j.decodeBounded(count);
        }
        bitsAdvanced += j.getReadingBitPosition() - before;
        sinceReset++; i++;
        maxPos = Math.max(maxPos, j.getReadingBitPosition());
      }
      System.out.printf("seed %d: ops %d (cap %d), ended %b, resets %d, longest run without reset %d ops, max position %d of %d bits, mean advance %.2f bits/op%n",
        seed, i, cap, i < cap, resets, Math.max(longestRun, sinceReset), maxPos, (bytes.length - 16) * 8, bitsAdvanced / (double) i);
    }
  }
}
