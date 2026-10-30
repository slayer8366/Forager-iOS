package probe;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/** Counters the shadowed oracle classes write. Single-threaded: the harness decodes on one thread. */
public final class Probe {
  public static String where = "-";

  public static long endOfArray;
  /** The cache stream's own context (a StatCoderContext over the cache's bytes) against any other BitCoderContext. */
  public static long endOfArrayStream, endOfArrayOther;
  public static int endOfArrayOtherMaxLen;
  public static String endOfArrayStreamFirstAt = "-", endOfArrayOtherClass = "-", endOfArrayOtherFirst = "-";

  public static void endOfArray(Object ctx, int length) {
    endOfArray++;
    if (ctx instanceof btools.codec.StatCoderContext) {
      if (endOfArrayStream++ == 0) endOfArrayStreamFirstAt = where + " (array length " + length + ")";
    } else {
      if (endOfArrayOther++ == 0) {
        StringBuilder t = new StringBuilder();
        StackTraceElement[] st = new Throwable().getStackTrace();
        for (int i = 1; i < Math.min(st.length, 9); i++) t.append("\n    at ").append(st[i]);
        endOfArrayOtherFirst = where + " (array length " + length + ")" + t;
      }
      endOfArrayOtherClass = ctx.getClass().getName();
      if (length > endOfArrayOtherMaxLen) endOfArrayOtherMaxLen = length;
    }
  }
  public static boolean inPredicted;
  public static int maxNumber = -1, maxDiff = -1, maxPredicted = -1;
  public static String maxNumberAt = "-", maxDiffAt = "-", maxPredictedAt = "-";
  public static long predictedOver24;

  public static long stackOverflows;
  public static int depth, maxDepth;
  public static String maxDepthAt = "-";

  public static long parseNodeBody, parseNodeBody2, readUnified134;

  /** Bin statistics per scope ("known", "fresh", "whole"): puts, max size, longest bin, tree bins, equal-hash tree bins. */
  public static String scope = "-";
  private static final java.util.TreeMap<String, long[]> bins = new java.util.TreeMap<>();
  private static final java.util.HashMap<String, String> binAt = new java.util.HashMap<>();
  private static final HashSet<String> treeSeen = new HashSet<>();

  public static void noisyNumber(int bits) {
    if (scope.equals("seed")) return;
    if (bits > maxNumber) { maxNumber = bits; maxNumberAt = where; }
  }

  public static void noisyDiff(int bits) {
    if (scope.equals("seed")) return;
    if (inPredicted) {
      if (bits > 24) predictedOver24++;
      if (bits > maxPredicted) { maxPredicted = bits; maxPredictedAt = where; }
    } else if (bits > maxDiff) { maxDiff = bits; maxDiffAt = where; }
  }

  public static void enter() {
    if (++depth > maxDepth) { maxDepth = depth; maxDepthAt = where; }
  }

  public static void leave() {
    depth--;
  }

  private static Field tableF, nextF, hashF;

  /** After OsmNodesMap.put: the bin the node went into, read from java.util.HashMap's table. */
  public static void afterPut(Map<?, ?> hmap, Object node) {
    try {
      if (tableF == null) {
        tableF = HashMap.class.getDeclaredField("table");
        tableF.setAccessible(true);
        Class<?> nodeC = Class.forName("java.util.HashMap$Node");
        nextF = nodeC.getDeclaredField("next");
        nextF.setAccessible(true);
        hashF = nodeC.getDeclaredField("hash");
        hashF.setAccessible(true);
      }
      long[] st = bins.computeIfAbsent(scope, k -> new long[5]);
      st[0]++;
      if (hmap.size() > st[1]) st[1] = hmap.size();
      Object[] tab = (Object[]) tableF.get(hmap);
      int h = node.hashCode();
      int i = (tab.length - 1) & (h ^ (h >>> 16));
      int len = 0;
      HashSet<Integer> hashes = new HashSet<>();
      boolean equal = false;
      for (Object e = tab[i]; e != null; e = nextF.get(e)) {
        len++;
        if (!hashes.add(hashF.getInt(e))) equal = true;
      }
      if (len > st[2]) { st[2] = len; binAt.put(scope, where + " bin " + i + " of " + tab.length); }
      boolean tree = tab[i] != null && tab[i].getClass().getName().equals("java.util.HashMap$TreeNode");
      if (tree) {
        if (treeSeen.add(scope + "/" + System.identityHashCode(hmap) + "/" + tab.length + "/" + i)) st[3]++;
        if (equal) st[4]++;
      }
    } catch (ReflectiveOperationException e) {
      throw new IllegalStateException("probe: java.util.HashMap's table is not readable (--add-opens java.base/java.util)", e);
    }
  }

  public static String summary() {
    StringBuilder b = new StringBuilder();
    for (java.util.Map.Entry<String, long[]> e : bins.entrySet()) {
      long[] st = e.getValue();
      b.append("\nhmap ").append(e.getKey()).append(": puts ").append(st[0]).append(", max size ").append(st[1]).append(", longest bin ").append(st[2])
          .append(" at ").append(binAt.get(e.getKey())).append(", tree bins seen ").append(st[3]).append(", puts into a tree bin holding equal hashes ").append(st[4]);
    }
    return "endOfArray " + endOfArray + ": in the cache stream's StatCoderContext " + endOfArrayStream + " (first at " + endOfArrayStreamFirstAt
        + "); in other contexts " + endOfArrayOther + " (" + endOfArrayOtherClass + ", arrays up to " + endOfArrayOtherMaxLen + " bytes); the first of those at " + endOfArrayOtherFirst
        + "\nnoisy: decodeNoisyNumber max " + maxNumber + " at " + maxNumberAt
        + "\nnoisy: decodeNoisyDiff (not from decodePredictedValue) max " + maxDiff + " at " + maxDiffAt
        + "\nnoisy: decodeNoisyDiff from decodePredictedValue max " + maxPredicted + " at " + maxPredictedAt + "; calls over 24: " + predictedOver24
        + "\ncleanupPeninsulas: StackOverflowError caught " + stackOverflows + "; max recursion depth " + maxDepth + " at " + maxDepthAt
        + "\nOsmNode.parseNodeBody " + parseNodeBody + ", parseNodeBody2 " + parseNodeBody2 + ", readUnified at :134 " + readUnified134
        + b;
  }
}
