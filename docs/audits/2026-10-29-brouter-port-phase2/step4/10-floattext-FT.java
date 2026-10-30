public class FT {
  public static void main(String[] a) {
    StringBuilder sb = new StringBuilder();
    for (int x = 0; x < 30000; x++) { if (x > 0) sb.append('\n'); sb.append(Float.toString(x / 100f)); }
    System.out.println("range hash " + sb.toString().hashCode() + " length " + sb.length());
    float[] lit = {0f, 1f, -1f, 0.5f, 3.5f, 0.01f, 0.07f, 0.001f, 1.0E-4f, 9999f, 10000f, 1.0E7f, 1.2345678E7f, 100000f, Float.POSITIVE_INFINITY, Float.NaN, -0f, 299.99f, 1f/3f};
    for (float f : lit) System.out.println(Float.toString(f));
  }
}
