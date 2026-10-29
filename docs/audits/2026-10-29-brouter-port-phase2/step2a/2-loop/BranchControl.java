// Positive control for LoopCheck2's end-of-array detector: case 2's draws with NO position bound, run until the oracle
// throws. If the detector works, idx passes idxMax (the branch fires) before the first unguarded ab[++idx] throws.
import btools.util.BitCoderContext;
import java.lang.reflect.Field;
public class BranchControl {
  public static void main(String[] a) throws Exception {
    Field fIdx = BitCoderContext.class.getDeclaredField("idx"); fIdx.setAccessible(true);
    for (int seed = 1; seed <= 20; seed++) {
      kotlin.random.Random r = kotlin.random.RandomKt.Random(seed);
      byte[] bytes = r.nextBytes(new byte[4096]);
      BitCoderContext j = new BitCoderContext(bytes);
      long i = 0, opsPastBranch = 0; int maxIdx = -1, kind = -1; String ex = "none";
      try {
        while (true) {
          int count = r.nextInt(0, 33); kind = r.nextInt(5);
          switch (kind) {
            case 0: j.decodeBits(count); break; case 1: j.decodeBitsReverse(count); break;
            case 2: j.decodeVarBits(); break; case 3: j.decodeBit(); break; default: j.decodeBounded(count);
          }
          i++; int idx = fIdx.getInt(j); maxIdx = Math.max(maxIdx, idx); if (idx > 4095) opsPastBranch++;
        }
      } catch (RuntimeException e) { ex = e.getClass().getSimpleName() + ": " + e.getMessage(); }
      System.out.printf("seed %d: threw at op %d (kind %d) %s; max idx before %d; ops completed with idx > idxMax %d%n", seed, i, kind, ex, maxIdx, opsPastBranch);
    }
  }
}
