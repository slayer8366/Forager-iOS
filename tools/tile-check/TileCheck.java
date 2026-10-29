// The tile check (dispatch 2026-10-28-01; step 1's harness, docs/audits/2026-10-29-brouter-port-phase2/step1/TileCheck.java.txt,
// moved here by Amendment 4 for .github/workflows/publish-tiles.yml). Loads one tile in the oracle, BRouter 1.7.10's -ro jar.
// Prints: the tile's lookup version against lookups.dat's; the engine's own open (PhysicalFile with that version, as
// NodesCache opens it); checkFileIntegrity; a count of the micro-caches it decodes (so a pass is not a pass on no data);
// and one route between two points in the tile, Forager's calls and profile.
// Unlike step 1's copy, which only printed, it ends with "PASS" or "FAIL: <reasons>" and exits 1 on any failed check.
//   java -cp <ro.jar>:<classes> TileCheck <profiles2 dir> <segments dir> <tile name> <lat1> <lon1> <lat2> <lon2>
import btools.codec.DataBuffers;
import btools.codec.MicroCache;
import btools.expressions.BExpressionMetaData;
import btools.mapaccess.OsmFile;
import btools.mapaccess.PhysicalFile;
import btools.router.*;
import java.io.*;
import java.util.*;

public class TileCheck {
  // PhysicalFile.ra is package-private (PhysicalFile.java:18 at v1.7.10), so the harness closes it by reflection.
  static void close(PhysicalFile pf) throws Exception {
    java.lang.reflect.Field fr = PhysicalFile.class.getDeclaredField("ra"); fr.setAccessible(true);
    ((RandomAccessFile) fr.get(pf)).close();
  }
  static final List<String> failures = new ArrayList<>();
  static void check(boolean ok, String what) { if (!ok) failures.add(what); }
  public static void main(String[] a) throws Exception {
    File profiles = new File(a[0]); File segs = new File(a[1]); String tile = a[2];
    File f = new File(segs, tile + ".rd5");
    DataInputStream in = new DataInputStream(new FileInputStream(f));
    short tileVersion = (short) (in.readLong() >> 48); in.close();
    BExpressionMetaData meta = new BExpressionMetaData();
    // lookups.dat's version line, read as the engine reads it
    int lookupsVersion = -1, minor = -1;
    for (String line : java.nio.file.Files.readAllLines(new File(profiles, "lookups.dat").toPath())) {
      if (line.startsWith("---lookupversion:")) lookupsVersion = Integer.parseInt(line.substring(17).trim());
      if (line.startsWith("---minorversion:")) minor = Integer.parseInt(line.substring(16).trim());
    }
    System.out.println(tile + " | bytes=" + f.length() + " | tile lookupversion=" + tileVersion + " | lookups.dat=" + lookupsVersion + "." + minor);
    check(tileVersion == lookupsVersion, "lookup version " + tileVersion + " is not lookups.dat's " + lookupsVersion);
    String open;
    try { close(new PhysicalFile(f, new DataBuffers(), lookupsVersion, minor)); open = "ok"; }
    catch (IOException e) { open = "FAILED " + e.getMessage(); }
    System.out.println(tile + " | engine open (version-checked) | " + open);
    check(open.equals("ok"), "engine open " + open);
    String integrity;
    try { String r = PhysicalFile.checkFileIntegrity(f); integrity = r == null ? "ok (null)" : "FAILED " + r; }
    catch (Exception e) { integrity = "FAILED " + e; }
    System.out.println(tile + " | checkFileIntegrity | " + integrity);
    check(integrity.equals("ok (null)"), "checkFileIntegrity " + integrity);
    // checkFileIntegrity's loop (PhysicalFile.java:72-80), but decoding: it passes MicroCache.debug (false unless
    // PhysicalFile.main sets it) as reallyDecode, so it only CRC-checks each cell's bytes and returns null for every
    // non-empty one (OsmFile.java:133-148). Here reallyDecode is true, as PhysicalFile.main runs it, and the nodes
    // each cell decodes to are counted, so a pass is not a pass on no data.
    DataBuffers db = new DataBuffers();
    PhysicalFile pf = new PhysicalFile(f, db, -1, -1);
    int div = pf.divisor; long caches = 0, nonEmpty = 0, entries = 0; int degreesWithData = 0;
    for (int lo = 0; lo < 5; lo++) for (int la = 0; la < 5; la++) {
      OsmFile osmf = new OsmFile(pf, lo, la, db);
      if (!osmf.hasData()) continue;
      degreesWithData++;
      for (int i = 0; i < div; i++) for (int j = 0; j < div; j++) {
        MicroCache mc = osmf.createMicroCache(lo * div + i, la * div + j, db, null, null, true, null);
        caches++;
        if (mc != null && mc.getSize() > 0) { nonEmpty++; entries += mc.getSize(); }
      }
    }
    close(pf);
    System.out.println(tile + " | decoded | 1-degree squares with data=" + degreesWithData + "/25 | micro-caches=" + caches
      + " | non-empty=" + nonEmpty + " | nodes=" + entries);
    check(degreesWithData > 0 && nonEmpty > 0 && entries > 0, "nothing decoded");
    double[] s = {Double.parseDouble(a[3]), Double.parseDouble(a[4])}, t = {Double.parseDouble(a[5]), Double.parseDouble(a[6])};
    RoutingParamCollector c = new RoutingParamCollector();
    List<OsmNodeNamed> wps = c.getWayPointList(s[1] + "," + s[0] + "|" + t[1] + "," + t[0]);
    RoutingContext rc = new RoutingContext();
    HashMap<String, String> p = new HashMap<>();
    p.put("profile", new File(profiles, "hiking-mountain.brf").getAbsolutePath());
    p.put("alternativeidx", "0"); p.put("profile:use_dynamic_range", "0");
    c.setParams(rc, wps, p);
    RoutingEngine e = new RoutingEngine(null, null, segs, wps, rc, RoutingEngine.BROUTER_ENGINEMODE_ROUTING);
    e.quite = true; e.doRun(0);
    OsmTrack tr = e.getFoundTrack();
    System.out.println(tile + " | route " + a[3] + "," + a[4] + " -> " + a[5] + "," + a[6] + " | error=" + e.getErrorMessage()
      + " | nodes=" + (tr == null ? "null" : tr.nodes.size()) + " | distance=" + (tr == null ? "null" : tr.distance));
    check(e.getErrorMessage() == null && tr != null && tr.nodes.size() > 0, "no route");
    System.out.println(tile + " | " + (failures.isEmpty() ? "PASS" : "FAIL: " + String.join("; ", failures)));
    if (!failures.isEmpty()) System.exit(1);
  }
}
