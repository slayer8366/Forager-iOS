// Calls BRouter v1.7.10 exactly as Forager does at pre-main e2969abe4 (routing/BRouterTrailRouter.kt:146-178,
// routing/RoutingDataFolder.kt:48-53), over the cases Forager's own routing tests use, so a coverage agent can
// record which BRouter methods run (dispatch 2026-10-28-01, item A). Prints one line per case:
// name, error message (or "-"), node count, distance, and a hash of the node list.
//
//   java -cp <brouter classes>:<this dir> ForagerCalls <profile dir> <segments dir> <work dir>
import btools.codec.DataBuffers;
import btools.mapaccess.PhysicalFile;
import btools.router.OsmNodeNamed;
import btools.router.OsmPathElement;
import btools.router.OsmTrack;
import btools.router.RoutingContext;
import btools.router.RoutingEngine;
import btools.router.RoutingParamCollector;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;

public class ForagerCalls {
  static File profileDir;

  // RoutingTestData.kt and BRouterTrailRouterTest.kt at e2969abe4: [lat, lng]
  static final double[] ACROSS_FROM = {41.2742564, -73.9978932};
  static final double[] ACROSS_TO = {41.3036912, -74.0159457};
  static final double[] LONG_FROM = {41.2772811, -74.0422743};
  static final double[] LONG_TO = {41.292043, -73.996678};
  static final double[] NO_TRAIL_NEAR = {41.40, -74.05};
  static final double[] ISLAND = {41.23, -74.03};
  static final double[] OFF_TRAIL = {41.2742564 + 0.0004, -73.9978932 + 0.0004};

  public static void main(String[] args) throws Exception {
    profileDir = new File(args[0]);
    File segments = new File(args[1]);
    File work = new File(args[2]);
    work.mkdirs();

    run("across-the-degree-line", ACROSS_FROM, ACROSS_TO, segments, 30_000L, false);
    run("inside-long", LONG_FROM, LONG_TO, segments, 30_000L, false);
    run("across-again (profile cache warm)", ACROSS_FROM, ACROSS_TO, segments, 30_000L, false);
    run("snap-on-trail (point to itself)", ACROSS_FROM, ACROSS_FROM, segments, 30_000L, false);
    run("snap-off-trail (point to itself)", OFF_TRAIL, OFF_TRAIL, segments, 30_000L, false);
    run("full-route-snap (off trail toward across-to)", OFF_TRAIL, ACROSS_TO, segments, 30_000L, false);
    run("end-no-trail-near", ACROSS_FROM, NO_TRAIL_NEAR, segments, 30_000L, false);
    run("start-no-trail-near", NO_TRAIL_NEAR, ACROSS_FROM, segments, 30_000L, false);
    run("island", ACROSS_FROM, ISLAND, segments, 30_000L, false);
    run("time-limit 1 ms", LONG_FROM, LONG_TO, segments, 1L, false);
    run("terminated before the search", LONG_FROM, LONG_TO, segments, 30_000L, true);
    run("no segment directory", ACROSS_FROM, ACROSS_TO, new File(work, "never-downloaded"), 30_000L, false);

    byte[] good = Files.readAllBytes(new File(segments, "W75_N40.rd5").toPath());
    damaged("cut-100000", java.util.Arrays.copyOf(good, 100_000), work);
    damaged("cut-150", java.util.Arrays.copyOf(good, 150), work);
    byte[] hdr = good.clone();
    hdr[10] = (byte) (hdr[10] ^ 0xff);
    damaged("header-byte", hdr, work);
    byte[] tile = good.clone();
    for (int i = 2_000; i < tile.length - 200; i++) tile[i] = (byte) (tile[i] ^ 0x5a);
    damaged("tile-data", tile, work);
  }

  static void damaged(String name, byte[] bytes, File work) throws IOException {
    File dir = new File(work, name);
    dir.mkdirs();
    File f = new File(dir, "W75_N40.rd5");
    Files.write(f.toPath(), bytes);
    // RoutingDataFolder.headerProblem
    String header;
    try {
      new PhysicalFile(f, new DataBuffers(), -1, -1).close();
      header = "ok";
    } catch (IOException e) {
      header = e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage();
    }
    System.out.println("header-check " + name + ": " + header);
    // and the engine on it, as Forager would not (it stops at the header check), to see the engine's own read
    run("engine on " + name, ACROSS_FROM, ACROSS_TO, dir, 30_000L, false);
  }

  static void run(String name, double[] start, double[] end, File data, long maxMillis, boolean terminateFirst) {
    try {
      RoutingParamCollector collector = new RoutingParamCollector();
      // Kotlin's "${start.lng},${start.lat}|..." on the JVM is Double.toString, as Java's concatenation is
      List<OsmNodeNamed> waypoints = collector.getWayPointList(start[1] + "," + start[0] + "|" + end[1] + "," + end[0]);
      RoutingContext context = new RoutingContext();
      HashMap<String, String> params = new HashMap<>();
      params.put("profile", new File(profileDir, "hiking-mountain.brf").getAbsolutePath());
      params.put("alternativeidx", "0");
      params.put("profile:use_dynamic_range", "0");
      collector.setParams(context, waypoints, params);
      RoutingEngine engine = new RoutingEngine(null, null, data, waypoints, context, RoutingEngine.BROUTER_ENGINEMODE_ROUTING);
      engine.quite = true;
      if (terminateFirst) engine.terminate();
      engine.doRun(maxMillis);
      String message = engine.getErrorMessage();
      OsmTrack track = engine.getFoundTrack();
      long h = 1125899906842597L;
      for (OsmPathElement n : track.nodes) h = 31 * (31 * h + n.getILat()) + n.getILon();
      System.out.println(name + " | " + (message == null ? "-" : message) + " | nodes=" + track.nodes.size()
        + " | distance=" + track.distance + " | hash=" + Long.toHexString(h) + " | terminated=" + engine.isTerminated());
    } catch (Exception e) {
      System.out.println(name + " | THREW " + e);
    }
  }
}
