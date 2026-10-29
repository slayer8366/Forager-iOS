// Regenerates part 6's atan2 sample (ProbeTest.kt:5 Lcg, :42-51 atan2Sample, seed 42), since the probe's atan2.txt
// result file is not in the brouter-port evidence. Output: dy dx Math.atan2-bits StrictMath.atan2-bits
import java.io.PrintWriter;
public class Gen {
  static long s = 42L;
  static int next() { s = s * 6364136223846793005L + 1442695040888963407L; return (int) (s >>> 33); }
  static String hex(double d) { return String.format("%016x", Double.doubleToRawLongBits(d)); }
  public static void main(String[] a) throws Exception {
    int n = 0, diff = 0;
    try (PrintWriter w = new PrintWriter(a[0], "UTF-8")) {
      for (int i = 0; i < 300000; i++) {
        int range = (i % 4 == 0) ? 50 : (i % 4 == 1) ? 2000 : (i % 4 == 2) ? 100000 : 2000000;
        int dx = next() % range; int dy = next() % range;
        if (dx == 0 && dy == 0) continue;
        double m = Math.atan2((double) dy, (double) dx), st = StrictMath.atan2((double) dy, (double) dx);
        if (Double.doubleToRawLongBits(m) != Double.doubleToRawLongBits(st)) diff++;
        w.println(dy + " " + dx + " " + hex(m) + " " + hex(st)); n++;
      }
    }
    System.out.println("pairs " + n + " Math!=StrictMath " + diff + " java.version " + System.getProperty("java.version") + " arch " + System.getProperty("os.arch"));
  }
}
