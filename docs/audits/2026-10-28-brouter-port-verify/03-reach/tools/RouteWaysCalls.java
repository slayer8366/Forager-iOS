// Calls BRouter v1.7.10 exactly as BRouterRouteWays.read does at Forager a0535ece
// (app/src/main/java/com/zynergylabs/forager/app/routing/BRouterRouteWays.kt:59-123), so a coverage agent can record
// which BRouter methods run on that path (step 5d, RECORD -70 item 2). Prints, per point, the ways kept and a hash of
// their tags and geometry; no coordinate.
//
//   java -cp <brouter classes>:<this dir> RouteWaysCalls <lookups.dat> <segments dir> <lat,lng,radius>...
import btools.codec.DataBuffers;
import btools.codec.MicroCache;
import btools.expressions.BExpressionContextWay;
import btools.expressions.BExpressionMetaData;
import btools.mapaccess.GeometryDecoder;
import btools.mapaccess.OsmFile;
import btools.mapaccess.OsmLink;
import btools.mapaccess.OsmNode;
import btools.mapaccess.OsmNodesMap;
import btools.mapaccess.OsmTransferNode;
import btools.mapaccess.PhysicalFile;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Locale;

public class RouteWaysCalls {
  static final int DIVISOR = 32;
  static final long CELL = 1_000_000L / DIVISOR;

  public static void main(String[] args) throws Exception {
    File lookups = new File(args[0]);
    File segments = new File(args[1]);
    for (int i = 2; i < args.length; i++) {
      String[] p = args[i].split(",");
      int[] r = read(lookups, segments, Double.parseDouble(p[0]), Double.parseDouble(p[1]), Double.parseDouble(p[2]));
      System.out.println("point " + (i - 1) + ": ways " + r[0] + ", hash " + Integer.toHexString(r[1]));
    }
  }

  static long micro(double degrees) {
    return (long) (degrees * 1_000_000.0 + 0.5);
  }

  /** GeoDistance.metersPerDegree at a0535ece is not BRouter; any positive scale reaches the same BRouter methods. */
  static int[] read(File lookups, File segments, double lat, double lng, double radius) throws IOException {
    BExpressionMetaData meta = new BExpressionMetaData();                       // kt:60
    BExpressionContextWay wayContext = new BExpressionContextWay(meta);          // kt:61
    meta.readMetaData(lookups);                                                  // kt:62
    double perLat = 111_320.0, perLng = 111_320.0 * Math.cos(Math.toRadians(lat));
    long dLat = micro(radius / perLat), dLng = micro(radius / perLng);
    long lon0 = micro(lng + 180.0), lat0 = micro(lat + 90.0);
    GeometryDecoder geometry = new GeometryDecoder();                            // kt:72
    HashSet<String> seen = new HashSet<>();
    int ways = 0, hash = 17;
    for (long lc = (lon0 - dLng) / CELL; lc <= (lon0 + dLng) / CELL; lc++) {
      for (long ac = (lat0 - dLat) / CELL; ac <= (lat0 + dLat) / CELL; ac++) {
        long ilon = lc * CELL, ilat = ac * CELL;
        int lonDeg = (int) (ilon / 1_000_000) / 5 * 5 - 180, latDeg = (int) (ilat / 1_000_000) / 5 * 5 - 90;
        String name = String.format(Locale.ROOT, "%s%d_%s%d.rd5", lonDeg < 0 ? "W" : "E", Math.abs(lonDeg), latDeg < 0 ? "S" : "N", Math.abs(latDeg));
        File file = new File(segments, name);
        if (!file.isFile()) throw new IOException("no route square " + name);
        DataBuffers buffers = new DataBuffers();                                 // kt:77
        PhysicalFile square = new PhysicalFile(file, buffers, -1, -1);           // kt:78
        try {
          if (square.divisor != DIVISOR) throw new IOException("divisor " + square.divisor);   // kt:80
          OsmFile degree = new OsmFile(square, (int) (lc * CELL / 1_000_000), (int) (ac * CELL / 1_000_000), buffers);  // kt:82
          if (!degree.hasData()) continue;                                       // kt:83
          MicroCache cache = degree.createMicroCache((int) lc, (int) ac, buffers, null, null, true, null);  // kt:84
          if (cache == null) continue;
          OsmNodesMap nodes = new OsmNodesMap();                                 // kt:85
          for (int i = 0; i < cache.getSize(); i++) {                            // kt:86
            long id = cache.getIdForIndex(i);                                    // kt:87
            if (!cache.getAndClear(id)) continue;                                // kt:88
            OsmNode node = new OsmNode(id);                                      // kt:89
            node.parseNodeBody(cache, nodes, wayContext);                        // kt:90
            for (OsmLink link = node.firstlink; link != null; link = link.getNext(node)) {   // kt:91, :110
              byte[] bitmap = link.descriptionBitmap;                            // kt:93
              if (bitmap == null) continue;
              OsmNode target = link.getTarget(node);                             // kt:95
              boolean reverse = link.isReverse(node);                            // kt:96
              int h = node.ilat * 31 + node.ilon, n = 2;
              OsmTransferNode t = link.geometry == null ? null : geometry.decodeGeometry(link.geometry, node, target, reverse);  // kt:99
              for (; t != null; t = t.next) { h = h * 31 + t.ilat * 7 + t.ilon; n++; }                                     // kt:100-103
              h = h * 31 + target.ilat * 7 + target.ilon;
              long e0 = (long) node.ilon << 32 | node.ilat, e1 = (long) target.ilon << 32 | target.ilat;
              String key = Math.min(e0, e1) + ":" + Math.max(e0, e1) + ":" + n;
              if (seen.add(key)) {
                ways++;
                hash = hash * 31 + h * 7 + wayContext.getKeyValueDescription(reverse, bitmap).hashCode();  // kt:108
              }
            }
          }
        } finally {
          square.close();                                                        // kt:115
        }
      }
    }
    return new int[] {ways, hash};
  }
}
