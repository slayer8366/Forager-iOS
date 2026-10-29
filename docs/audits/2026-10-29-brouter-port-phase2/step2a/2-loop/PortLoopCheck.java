// BitCoderContextTest.rawBitReadsOnRandomBytesMatchTheOracle's loop, oracle against the port's jvm jar, with an op cap
// per seed (the test's own loop does not end: LoopCheck.java). Same draws in the same order; same two equalities.
public class PortLoopCheck {
  public static void main(String[] a) {
    long cap = Long.parseLong(a[0]); long total = 0, mism = 0; long[] perOp = new long[5]; long resets = 0; int maxPos = 0;
    for (int seed = 1; seed <= 20; seed++) {
      kotlin.random.Random r = kotlin.random.RandomKt.Random(seed);
      byte[] bytes = r.nextBytes(new byte[4096]);
      btools.util.BitCoderContext j = new btools.util.BitCoderContext(bytes);
      com.zynergylabs.forager.brouter.util.BitCoderContext k = new com.zynergylabs.forager.brouter.util.BitCoderContext(bytes);
      long i = 0;
      while (j.getReadingBitPosition() < (bytes.length - 16) * 8 && i < cap) {
        int count = r.nextInt(0, 33); int op = r.nextInt(5); int vj, vk;
        switch (op) {
          case 0: vj = j.decodeBits(count); vk = k.decodeBits(count); break;
          case 1: vj = j.decodeBitsReverse(count); vk = k.decodeBitsReverse(count); break;
          case 2: vj = j.decodeVarBits(); vk = k.decodeVarBits(); break;
          case 3: vj = j.decodeBit() ? 1 : 0; vk = k.decodeBit() ? 1 : 0; break;
          default:
            if (r.nextInt(50) == 0) { j.reset(); k.reset(); resets++; }
            vj = j.decodeBounded(count); vk = k.decodeBounded(count);
        }
        perOp[op]++;
        if (vj != vk || j.getReadingBitPosition() != k.getReadingBitPosition()) {
          if (mism++ < 5) System.out.printf("MISMATCH seed %d op %d (kind %d, count %d): value %d vs %d, position %d vs %d%n", seed, i, op, count, vj, vk, j.getReadingBitPosition(), k.getReadingBitPosition());
        }
        maxPos = Math.max(maxPos, j.getReadingBitPosition()); i++; total++;
      }
    }
    System.out.printf("20 seeds x cap %d: %d ops compared (decodeBits %d, decodeBitsReverse %d, decodeVarBits %d, decodeBit %d, decodeBounded %d), %d resets, max reading position %d bits, %d mismatches%n",
      cap, total, perOp[0], perOp[1], perOp[2], perOp[3], perOp[4], resets, maxPos, mism);
  }
}
