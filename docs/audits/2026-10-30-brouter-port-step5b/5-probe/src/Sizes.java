package probe;

import btools.codec.DataBuffers;
import btools.mapaccess.OsmFile;
import btools.mapaccess.PhysicalFile;
import java.io.File;

/** Counts non-empty caches per tile, and those larger than DataBuffers' 65,636-byte iobuffer (decoded from an exact-size array). */
public final class Sizes {
  public static void main(String[] a) throws Exception {
    for (String arg : a) {
      File file = new File(arg);
      int[] c0 = ProbeMain.corner(file.getName());
      PhysicalFile pf = new PhysicalFile(file, new DataBuffers(), -1, -1);
      int n = 0, big = 0, max = 0;
      byte[] buf = new byte[1 << 20];
      for (int t = 0; t < 25; t++) {
        OsmFile of = new OsmFile(pf, c0[0] + t / 5, c0[1] + t % 5, new DataBuffers());
        if (!of.hasData()) continue;
        for (int sub = 0; sub < pf.divisor * pf.divisor; sub++) {
          int size = of.getDataInputForSubIdx(sub, buf);
          if (size == 0) continue;
          n++;
          if (size > 65636) big++;
          max = Math.max(max, size);
        }
      }
      pf.close();
      System.out.println(arg.replaceAll(".*/tiles/", "") + ": " + n + " caches, " + big + " larger than 65636 bytes, largest " + max);
    }
  }
}
