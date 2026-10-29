import btools.mapaccess.OsmNode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

// H step 3, part 7: is java.util.HashMap's iteration order over BRouter's own OsmNode keys a function of the
// inserted coordinates alone? OsmNode.hashCode is ilon + ilat (OsmNode.java:259-261) and OsmNode is not Comparable,
// so keys with equal ilon + ilat that land in a tree bin are ordered by System.identityHashCode
// (HashMap.tieBreakOrder). Built the way OsmNodesMap builds hmap: new HashMap<>(4096) (OsmNodesMap.java:16).
// Each map is built from fresh OsmNode objects with the same coordinates, in the same insertion order.
public class TreeBinOrder {
  static final int GROUP = 12;     // nodes sharing one ilon + ilat: more than TREEIFY_THRESHOLD (8), so the bin treeifies
  static final int SPREAD = 2000;  // nodes with distinct sums, in list bins: the control

  static List<OsmNode> keys() {
    List<OsmNode> k = new ArrayList<>();
    for (int i = 0; i < GROUP; i++) k.add(new OsmNode(105_000_000 + 7 * i, 131_000_000 - 7 * i));
    long s = 12345;
    for (int i = 0; i < SPREAD; i++) {
      s = s * 6364136223846793005L + 1442695040888963407L;
      int dx = (int) ((s >>> 33) % 400_000), dy = (int) ((s >>> 13) % 400_000);
      k.add(new OsmNode(105_100_000 + dx, 130_500_000 + dy));
    }
    return k;
  }

  static String order(List<OsmNode> keys, boolean groupOnly) {
    HashMap<OsmNode, OsmNode> m = new HashMap<>(4096);
    for (OsmNode n : keys) m.put(n, n);
    StringBuilder b = new StringBuilder();
    for (OsmNode n : m.values()) {
      int idx = keys.indexOf(n); // equals is on (ilon, ilat), so this finds the key's position in the insertion list
      if (groupOnly == (idx < GROUP)) b.append(idx).append(',');
    }
    return b.toString();
  }

  public static void main(String[] args) {
    int reps = Integer.parseInt(args[0]);
    String control0 = null;
    int controlSame = 0;
    for (int r = 0; r < reps; r++) {
      List<OsmNode> k = keys();
      String g = order(k, true);
      String c = order(k, false);
      if (control0 == null) control0 = c;
      if (c.equals(control0)) controlSame++;
      System.out.println("rep " + r + " group-order " + g);
    }
    System.out.println("control (" + SPREAD + " distinct-sum keys) order identical in " + controlSame + " of " + reps + " reps");
  }
}
