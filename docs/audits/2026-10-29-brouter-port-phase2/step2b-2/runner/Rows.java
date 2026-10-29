public class Rows { public static void main(String[] a) { int n = 0; StringBuilder sb = new StringBuilder();
  for (int r = 0; r < 1800; r++) { double lat = (Math.PI / 180.0) * ((r * 100000 + 50000) * 1e-6 - 90);
    if (Double.doubleToRawLongBits(Math.cos(lat)) != Double.doubleToRawLongBits(StrictMath.cos(lat))) { n++; if (n <= 5) sb.append(r).append(' '); } }
  System.out.println(System.getProperty("java.vm.version") + " " + System.getProperty("os.arch") + ": Math.cos != StrictMath.cos at " + n + " of 1800 rows; first: " + sb); } }
