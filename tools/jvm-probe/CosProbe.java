// RECORD -37: whether this JVM's Math.cos and Math.atan2 are bit-equal to StrictMath's (fdlibm), on fixed inputs.
//     java [flags] -cp <dir> CosProbe <passes>
// The same 200,000 inputs (seeded) are compared in every pass. The first pass starts interpreted; by the last, the loop
// has been JIT-compiled unless the JVM runs with -Xint. Prints the mismatch counts of the first and last pass.
import java.util.Random;

public class CosProbe {
    static final int N = 200_000;

    static int cosMismatches(double[] xs) {
        int n = 0;
        for (int i = 0; i < xs.length; i++) {
            if (Double.doubleToRawLongBits(Math.cos(xs[i])) != Double.doubleToRawLongBits(StrictMath.cos(xs[i]))) n++;
        }
        return n;
    }

    static int atan2Mismatches(double[] ys, double[] xs) {
        int n = 0;
        for (int i = 0; i < xs.length; i++) {
            double a = Math.atan2(ys[i], xs[i]);
            if (Double.doubleToRawLongBits(a) != Double.doubleToRawLongBits(StrictMath.atan2(ys[i], xs[i]))) n++;
        }
        return n;
    }

    public static void main(String[] args) {
        int passes = Integer.parseInt(args[0]);
        Random r = new Random(20261030L);
        double[] xs = new double[N];
        double[] ys = new double[N];
        for (int i = 0; i < N; i++) {
            xs[i] = (r.nextDouble() * 2 - 1) * 4;
            ys[i] = (r.nextDouble() * 2 - 1) * 4;
        }
        for (int p = 1; p <= passes; p++) {
            int c = cosMismatches(xs);
            int a = atan2Mismatches(ys, xs);
            if (p == 1 || p == passes) {
                System.out.println("pass " + p + " of " + passes + ": Math.cos != StrictMath.cos at " + c + " of " + N
                        + " inputs; Math.atan2 != StrictMath.atan2 at " + a + " of " + N);
            }
        }
    }
}
