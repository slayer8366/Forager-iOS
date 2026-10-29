// Where is the oracle before case 1's op 206 on seed 1, and what does that op read? (bite A's failure, explained or not)
import btools.util.BitCoderContext;
public class Op206 {
  public static void main(String[] a) {
    kotlin.random.Random r = kotlin.random.RandomKt.Random(1);
    byte[] bytes = r.nextBytes(new byte[4096]);
    BitCoderContext j = new BitCoderContext(bytes);
    for (int i = 0; i <= 206; i++) {
      int before = j.getReadingBitPosition(); int count = r.nextInt(0, 33); int kind = r.nextInt(5); int v; boolean reset = false;
      switch (kind) { case 0: v = j.decodeBits(count); break; case 1: v = j.decodeBitsReverse(count); break; case 2: v = j.decodeVarBits(); break;
        case 3: v = j.decodeBit() ? 1 : 0; break; default: if (r.nextInt(50) == 0) { j.reset(); reset = true; before = 0; } v = j.decodeBounded(count); }
      if (i >= 204) System.out.printf("op %d: kind %d count %d reset %b, position %d -> %d, value %d%n", i, kind, count, reset, before, j.getReadingBitPosition(), v);
    }
  }
}
