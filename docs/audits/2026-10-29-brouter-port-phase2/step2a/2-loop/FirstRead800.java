// Case 2's draws (no reset) on the oracle, seed 1: the first op whose read covers bit 800 (bite B's predicted failure).
import btools.util.BitCoderContext;
public class FirstRead800 {
  public static void main(String[] a) {
    kotlin.random.Random r = kotlin.random.RandomKt.Random(1);
    byte[] bytes = r.nextBytes(new byte[4096]);
    BitCoderContext j = new BitCoderContext(bytes);
    for (int i = 0; ; i++) {
      int before = j.getReadingBitPosition(); int count = r.nextInt(0, 33); int kind = r.nextInt(5); int v;
      switch (kind) { case 0: v = j.decodeBits(count); break; case 1: v = j.decodeBitsReverse(count); break; case 2: v = j.decodeVarBits(); break;
        case 3: v = j.decodeBit() ? 1 : 0; break; default: v = j.decodeBounded(count); }
      int after = j.getReadingBitPosition();
      if (before <= 800 && after > 800) { System.out.printf("op %d: kind %d count %d, position %d -> %d, value %d%n", i, kind, count, before, after, v); return; }
    }
  }
}
