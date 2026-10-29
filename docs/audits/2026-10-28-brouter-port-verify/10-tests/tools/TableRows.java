// Part 8 (item E): CheapRuler's 1,800-row scale table, built twice in one JVM, with Math.cos and with
// StrictMath.cos, written as bits. calcKxKyFromILat is copied from the vendored
// third_party/brouter/src/main/java/btools/util/CheapRuler.java:29-49 (the static block at :29-33, calcKxKyFromILat at :35-49; Forager e2969abe4), with the cos call
// as the only change. Output lines: row lat-bits cos-bits kx-bits ky-bits, the same form as part 6's
// 09-float/probe scale-table.txt, so the Math table can be checked against part 6's JVM file byte for byte.
//
// Usage: java TableRows <out-math.txt> <out-strict.txt>
import java.io.PrintWriter;
import java.util.function.DoubleUnaryOperator;

public class TableRows {
  static final double ILATLNG_TO_LATLNG = 1e-6;
  static final int KILOMETERS_TO_METERS = 1000;
  static final double DEG_TO_RAD = Math.PI / 180.;
  static final int SCALE_CACHE_LENGTH = 1800;
  static final int SCALE_CACHE_INCREMENT = 100000;

  static String row(int i, DoubleUnaryOperator cosFn) {
    int ilat = i * SCALE_CACHE_INCREMENT + SCALE_CACHE_INCREMENT / 2;
    double lat = DEG_TO_RAD * (ilat * ILATLNG_TO_LATLNG - 90);
    double cos = cosFn.applyAsDouble(lat);
    double cos2 = 2 * cos * cos - 1;
    double cos3 = 2 * cos * cos2 - cos;
    double cos4 = 2 * cos * cos3 - cos2;
    double cos5 = 2 * cos * cos4 - cos3;
    double kx = (111.41513 * cos - 0.09455 * cos3 + 0.00012 * cos5) * ILATLNG_TO_LATLNG * KILOMETERS_TO_METERS;
    double ky = (111.13209 - 0.56605 * cos2 + 0.0012 * cos4) * ILATLNG_TO_LATLNG * KILOMETERS_TO_METERS;
    return i + " " + hex(lat) + " " + hex(cos) + " " + hex(kx) + " " + hex(ky);
  }

  static String hex(double d) {
    return String.format("%016x", Double.doubleToRawLongBits(d));
  }

  public static void main(String[] args) throws Exception {
    int n = 0;
    try (PrintWriter m = new PrintWriter(args[0], "UTF-8"); PrintWriter s = new PrintWriter(args[1], "UTF-8")) {
      for (int i = 0; i < SCALE_CACHE_LENGTH; i++) {
        m.println(row(i, Math::cos));
        s.println(row(i, StrictMath::cos));
        n++;
      }
    }
    System.out.println("rows written " + n + " java.version " + System.getProperty("java.version")
        + " vm " + System.getProperty("java.vm.name") + " " + System.getProperty("java.vm.version")
        + " arch " + System.getProperty("os.arch"));
  }
}
