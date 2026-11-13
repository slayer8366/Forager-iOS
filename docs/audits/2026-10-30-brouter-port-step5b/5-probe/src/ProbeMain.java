package probe;

import btools.codec.DataBuffers;
import btools.codec.MicroCache;
import btools.codec.MicroCache2;
import btools.codec.StatCoderContext;
import btools.expressions.BExpressionContextNode;
import btools.expressions.BExpressionContextWay;
import btools.expressions.BExpressionMetaData;
import btools.mapaccess.OsmFile;
import btools.mapaccess.OsmLink;
import btools.mapaccess.OsmNode;
import btools.mapaccess.OsmNodesMap;
import btools.mapaccess.PhysicalFile;
import btools.mapaccess.TurnRestriction;
import btools.router.OsmNodeNamed;
import btools.router.RoutingContext;
import btools.router.RoutingEngine;
import btools.router.RoutingParamCollector;

import java.io.File;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.security.MessageDigest;
import java.util.*;

/**
 * Step 5b's probe (scratch; dispatch 2026-10-28-01). Decodes every non-empty micro-cache of the given tiles on the oracle,
 * four ways per cache (known in mode 0; fresh in modes 0, 1, 2), and writes one SHA-256 per cache and scenario. Run
 * with the plain -ro jar, the counters stay at zero; run with the shadowed classes first on the classpath, they fill.
 *   ProbeMain decode <oracleDir> <out> <wholeTile:true|false> <label=path>...
 *   ProbeMain routes <oracleDir> <out> <segmentsDir> <f0Dir>
 */
public final class ProbeMain {
  static BExpressionContextWay way;

  public static void main(String[] a) throws Exception {
    File oracleDir = new File(a[1]);
    File profile = new File(oracleDir, "profiles2/hiking-mountain.brf");
    try (PrintWriter out = new PrintWriter(a[2], "UTF-8")) {
      out.println("# java " + System.getProperty("java.vendor") + " " + System.getProperty("java.runtime.version") + " " + System.getProperty("os.arch"));
      out.println("# classpath " + System.getProperty("java.class.path"));
      if (a[0].equals("decode")) {
        way = wayContext(oracleDir, profile);
        boolean wholeTile = Boolean.parseBoolean(a[3]);
        for (int i = 4; i < a.length; i++) {
          String label = a[i].substring(0, a[i].indexOf('='));
          File file = new File(a[i].substring(a[i].indexOf('=') + 1));
          long t0 = System.nanoTime();
          int caches = perCache(label, file, out, wholeTile);
          out.println("# " + label + " " + caches + " caches, " + (System.nanoTime() - t0) / 1000000 + " ms");
          out.flush();
        }
      } else {
        File segments = new File(a[3]);
        File f0 = new File(a[4]);
        double[][] routes = {
          {41.2742564, -73.9978932, 41.3036912, -74.0159457},
          {41.2772811, -74.0422743, 41.292043, -73.996678},
        };
        for (File dir : new File[] {f0, segments}) {
          for (double[] r : routes) {
            Probe.where = dir.getName() + " route " + Arrays.toString(r);
            long b2 = Probe.parseNodeBody2, b = Probe.parseNodeBody, u = Probe.readUnified134;
            String res = route(profile, dir, r);
            out.println("route " + Probe.where + ": " + res + "; parseNodeBody +" + (Probe.parseNodeBody - b)
                + ", parseNodeBody2 +" + (Probe.parseNodeBody2 - b2) + ", readUnified134 +" + (Probe.readUnified134 - u));
          }
        }
      }
      out.println("## counters (zero unless the shadowed classes are first on the classpath)");
      out.println(Probe.summary());
    }
  }

  static BExpressionContextWay wayContext(File oracleDir, File profile) {
    RoutingParamCollector collector = new RoutingParamCollector();
    List<OsmNodeNamed> wps = collector.getWayPointList("8.7,49.4|8.71,49.41");
    RoutingContext rc = new RoutingContext();
    Map<String, String> params = new HashMap<>();
    params.put("profile", profile.getAbsolutePath());
    params.put("alternativeidx", "0");
    params.put("profile:use_dynamic_range", "0");
    collector.setParams(rc, wps, params);
    BExpressionMetaData meta = new BExpressionMetaData();
    BExpressionContextWay w = new BExpressionContextWay(rc.memoryclass * 512, meta);
    BExpressionContextNode n = new BExpressionContextNode(0, meta);
    n.setForeignContext(w);
    meta.readMetaData(new File(oracleDir, "profiles2/lookups.dat"));
    w.parseFile(profile, "global", rc.keyValues);
    n.parseFile(profile, "global", rc.keyValues);
    return w;
  }

  static String route(File profile, File segments, double[] r) {
    RoutingParamCollector collector = new RoutingParamCollector();
    List<OsmNodeNamed> wps = collector.getWayPointList(r[1] + "," + r[0] + "|" + r[3] + "," + r[2]);
    RoutingContext rc = new RoutingContext();
    Map<String, String> params = new HashMap<>();
    params.put("profile", profile.getAbsolutePath());
    params.put("alternativeidx", "0");
    params.put("profile:use_dynamic_range", "0");
    collector.setParams(rc, wps, params);
    RoutingEngine e = new RoutingEngine(null, null, segments, wps, rc, RoutingEngine.BROUTER_ENGINEMODE_ROUTING);
    e.quite = true;
    e.doRun(300000);
    return e.getErrorMessage() != null ? "error " + e.getErrorMessage()
        : "nodes " + e.getFoundTrack().nodes.size() + " distance " + e.getFoundTrack().distance;
  }

  static int[] corner(String name) {
    int lon = Integer.parseInt(name.substring(1, name.indexOf('_'))) * (name.charAt(0) == 'W' ? -1 : 1);
    String latPart = name.substring(name.indexOf('_') + 1, name.indexOf('.'));
    int lat = Integer.parseInt(latPart.substring(1)) * (latPart.charAt(0) == 'S' ? -1 : 1);
    return new int[] {lon + 180, lat + 90};
  }

  static int perCache(String label, File file, PrintWriter out, boolean wholeTile) throws Exception {
    int[] c0 = corner(file.getName());
    PhysicalFile pf = new PhysicalFile(file, new DataBuffers(), -1, -1);
    DataBuffers buffers = new DataBuffers();
    OsmNodesMap whole = new OsmNodesMap();
    whole.cleanupMode = 1;
    int caches = 0;
    try {
      for (int tileIndex = 0; tileIndex < 25; tileIndex++) {
        int lonDegree = c0[0] + tileIndex / 5, latDegree = c0[1] + tileIndex % 5;
        OsmFile of = new OsmFile(pf, lonDegree, latDegree, new DataBuffers());
        if (!of.hasData()) continue;
        int divisor = pf.divisor;
        byte[] buf = new byte[1 << 16];
        for (int sub = 0; sub < divisor * divisor; sub++) {
          int size = of.getDataInputForSubIdx(sub, buf);
          if (size == 0) continue;
          if (size > buf.length) { buf = new byte[size]; size = of.getDataInputForSubIdx(sub, buf); }
          int lonIdx = divisor * lonDegree + sub % divisor, latIdx = divisor * latDegree + sub / divisor;
          caches++;
          String at = label + " (" + lonDegree + "," + latDegree + ") sub " + sub + " (" + size + " bytes)";
          Probe.where = at + " MicroCache2 (seed ids)";
          Probe.scope = "seed";
          long eoa = Probe.endOfArray, eoaS = Probe.endOfArrayStream, eoaO = Probe.endOfArrayOther;
          MicroCache2 mc = new MicroCache2(new StatCoderContext(buf), new DataBuffers(), lonIdx, latIdx, divisor, null, null);
          long[] ids = new long[mc.getSize()];
          for (int i = 0; i < ids.length; i++) ids[i] = mc.getIdForIndex(i);
          // MicroCache2's own decode is not the engine's: count only createMicroCache's
          Probe.endOfArray = eoa; Probe.endOfArrayStream = eoaS; Probe.endOfArrayOther = eoaO;

          StringBuilder line = new StringBuilder(label + " " + lonDegree + " " + latDegree + " " + sub);
          // known, mode 0
          Probe.where = at + " known";
          Probe.scope = "known";
          OsmNodesMap m = new OsmNodesMap();
          List<OsmNode> seeds = new ArrayList<>();
          for (long id : ids) { OsmNode n = new OsmNode((int) (id >> 32), (int) id); n.setHollow(); m.put(n); seeds.add(n); }
          long before = Probe.endOfArrayStream;
          check(of.createMicroCache(lonIdx, latIdx, buffers, way, null, true, m));
          long eoaHere = Probe.endOfArrayStream - before;
          StringBuilder t = new StringBuilder();
          for (OsmNode n : seeds) node(t, n);
          state(t, m, false);
          line.append(' ').append(sha(t));
          for (int mode = 0; mode <= 2; mode++) {
            Probe.where = at + " fresh mode " + mode;
            Probe.scope = "fresh";
            OsmNodesMap f = new OsmNodesMap();
            f.cleanupMode = mode;
            check(of.createMicroCache(lonIdx, latIdx, buffers, way, null, true, f));
            StringBuilder u = new StringBuilder();
            state(u, f, true);
            line.append(' ').append(sha(u));
          }
          if (eoaHere > 0) out.println("# endOfArray in the cache stream " + eoaHere + " times in " + at);
          out.println(line);
          if (wholeTile) {
            Probe.where = at + " whole tile";
            Probe.scope = "whole";
            check(of.createMicroCache(lonIdx, latIdx, buffers, way, null, true, whole));
          }
        }
      }
    } finally {
      pf.close();
    }
    return caches;
  }

  static void check(MicroCache seg) {
    if (seg != MicroCache.emptyNonVirgin || seg.getSize() != 0) throw new IllegalStateException("segment is not emptyNonVirgin at " + Probe.where);
  }

  static String hex(byte[] b) {
    if (b == null) return "-";
    StringBuilder s = new StringBuilder();
    for (byte x : b) s.append(String.format("%02x", x));
    return s.toString();
  }

  static void node(StringBuilder t, OsmNode n) {
    t.append("N ").append(n.ilon).append(' ').append(n.ilat).append(' ').append(n.selev).append(' ').append(n.visitID).append(' ').append(hex(n.nodeDescription)).append('\n');
    for (TurnRestriction r = n.firstRestriction; r != null; r = r.next)
      t.append("R ").append(r.exceptions).append(' ').append(r.isPositive).append(' ').append(r.fromLon).append(' ').append(r.fromLat).append(' ').append(r.toLon).append(' ').append(r.toLat).append('\n');
    for (OsmLink l = n.firstlink; l != null; l = l.getNext(n)) {
      OsmNode tn = l.getTarget(n);
      t.append("L ").append(tn.ilon).append(' ').append(tn.ilat).append(' ').append(tn.isHollow()).append(' ').append(l.isReverse(n)).append(' ')
          .append(l == n ? "self" : l == tn ? "target" : "own").append(' ').append(hex(l.descriptionBitmap)).append(' ').append(hex(l.geometry)).append('\n');
    }
  }

  @SuppressWarnings("unchecked")
  static void state(StringBuilder t, OsmNodesMap m, boolean reach) throws Exception {
    Field f = OsmNodesMap.class.getDeclaredField("hmap");
    f.setAccessible(true);
    List<OsmNode> hollow = new ArrayList<>(((Map<OsmNode, OsmNode>) f.get(m)).values());
    hollow.sort(Comparator.<OsmNode>comparingInt(n -> n.ilon).thenComparingInt(n -> n.ilat));
    t.append("C ").append(m.nodesCreated).append(' ').append(m.lastVisitID).append(' ').append(m.baseID).append(' ').append(hollow.size()).append('\n');
    IdentityHashMap<OsmNode, Boolean> seen = new IdentityHashMap<>();
    ArrayDeque<OsmNode> q = new ArrayDeque<>();
    for (OsmNode h : hollow) if (seen.put(h, true) == null) q.add(h);
    while (!q.isEmpty()) {
      OsmNode n = q.removeFirst();
      node(t, n);
      if (!reach) continue;
      for (OsmLink l = n.firstlink; l != null; l = l.getNext(n)) {
        OsmNode tn = l.getTarget(n);
        if (seen.put(tn, true) == null) q.add(tn);
      }
    }
  }

  static String sha(StringBuilder t) throws Exception {
    byte[] d = MessageDigest.getInstance("SHA-256").digest(t.toString().getBytes("UTF-8"));
    return hex(d).substring(0, 16);
  }
}
