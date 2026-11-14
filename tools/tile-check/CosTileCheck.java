// The capture check for the cosine-row tiles (session 73; docs/audits/2026-11-13-brouter-port-closing/s73-cos-tiles-capture.md).
// The owner's ruling it serves: "Run-time ends + row counts". A new class beside TileCheck.java, which it does not change.
// Loads one tile in the oracle, BRouter 1.7.10's -ro jar, and checks:
//  1. as TileCheck does: lookup version, the version-checked open, checkFileIntegrity, every cell decoded (nodes counted);
//  2. row counts: for each row listed, the nodes whose ilat lies in [row * 100000, (row + 1) * 100000); fails below FLOOR;
//  3. run-time ends, one route per listed row: the row's node ids sorted ascending (B, n = |B|); attempt k (k = 0 to
//     ATTEMPTS - 1) starts at B[n * (2k + 1) / (2 * ATTEMPTS)] and ends at the first later B[j] with ilon at least
//     STEP greater and ilat within STEP; the first attempt the oracle routes is the row's route; fails if none does.
// Prints counts, attempt numbers, metres and a digest of the chosen ids only. It never prints an id, an ilon, an ilat or a
// coordinate, and a failed attempt's error message is printed with every digit replaced by '#'.
//   java -cp <ro.jar>:<classes> CosTileCheck <profiles2 dir> <segments dir> <tile name> <rows, comma-separated> [notes file]
// With a notes file, appends one "#"-prefixed line per row (counts, attempts, metres) and one for the digest.
import btools.codec.DataBuffers;
import btools.codec.MicroCache;
import btools.mapaccess.OsmFile;
import btools.mapaccess.PhysicalFile;
import btools.router.*;
import java.io.*;
import java.security.MessageDigest;
import java.util.*;

public class CosTileCheck {
  // Session 72 measured 1,124 nodes in the lowest row (502); 500 is under half that. Reasons in the s73 report, §3.3.
  static final int FLOOR = 500;
  static final int ATTEMPTS = 16;
  // 0.01 degree in BRouter's micro-degrees: the least eastward step to the end, and the most north or south.
  static final int STEP = 10_000;
  static final int ROW = 100_000;

  static final List<String> failures = new ArrayList<>();
  static void check(boolean ok, String what) { if (!ok) failures.add(what); }
  static final List<String> notes = new ArrayList<>();

  // PhysicalFile.ra is package-private (PhysicalFile.java:18 at v1.7.10), so it is closed by reflection, as in TileCheck.
  static void close(PhysicalFile pf) throws Exception {
    java.lang.reflect.Field fr = PhysicalFile.class.getDeclaredField("ra"); fr.setAccessible(true);
    ((RandomAccessFile) fr.get(pf)).close();
  }
  static int ilon(long id) { return (int) (id >> 32); }
  static int ilat(long id) { return (int) id; }
  static String noDigits(String s) { return s == null ? "null" : s.replaceAll("[0-9]", "#"); }

  public static void main(String[] a) throws Exception {
    File profiles = new File(a[0]); File segs = new File(a[1]); String tile = a[2];
    int[] rows = Arrays.stream(a[3].split(",")).mapToInt(s -> Integer.parseInt(s.trim())).toArray();
    File notesFile = a.length > 4 ? new File(a[4]) : null;
    File f = new File(segs, tile + ".rd5");
    try {
      run(profiles, segs, tile, rows, f);
    } catch (Exception e) {
      // Kept to the exception's class and its message without digits, so nothing positional can leak.
      failures.add("stopped by " + e.getClass().getSimpleName() + ": " + noDigits(e.getMessage()));
    }
    if (notesFile != null && failures.isEmpty()) {
      try (Writer w = new FileWriter(notesFile, true)) { for (String n : notes) w.write(n + "\n"); }
    }
    System.out.println(tile + " | " + (failures.isEmpty() ? "PASS" : "FAIL: " + String.join("; ", failures)));
    if (!failures.isEmpty()) System.exit(1);
  }

  static void run(File profiles, File segs, String tile, int[] rows, File f) throws Exception {
    // 1. As TileCheck.java:37-75.
    short tileVersion;
    try (DataInputStream in = new DataInputStream(new FileInputStream(f))) { tileVersion = (short) (in.readLong() >> 48); }
    int lookupsVersion = -1, minor = -1;
    for (String line : java.nio.file.Files.readAllLines(new File(profiles, "lookups.dat").toPath())) {
      if (line.startsWith("---lookupversion:")) lookupsVersion = Integer.parseInt(line.substring(17).trim());
      if (line.startsWith("---minorversion:")) minor = Integer.parseInt(line.substring(16).trim());
    }
    System.out.println(tile + " | bytes=" + f.length() + " | tile lookupversion=" + tileVersion + " | lookups.dat=" + lookupsVersion + "." + minor);
    check(tileVersion == lookupsVersion, "lookup version " + tileVersion + " is not lookups.dat's " + lookupsVersion);
    String open;
    try { close(new PhysicalFile(f, new DataBuffers(), lookupsVersion, minor)); open = "ok"; }
    catch (IOException e) { open = "FAILED " + noDigits(e.getMessage()); }
    System.out.println(tile + " | engine open (version-checked) | " + open);
    check(open.equals("ok"), "engine open " + open);
    String integrity;
    try { String r = PhysicalFile.checkFileIntegrity(f); integrity = r == null ? "ok (null)" : "FAILED " + noDigits(r); }
    catch (Exception e) { integrity = "FAILED " + e.getClass().getSimpleName() + " " + noDigits(e.getMessage()); }
    System.out.println(tile + " | checkFileIntegrity | " + integrity);
    check(integrity.equals("ok (null)"), "checkFileIntegrity " + integrity);
    if (!failures.isEmpty()) return; // nothing below can be trusted on a file that does not open whole

    Map<Integer, List<Long>> band = new LinkedHashMap<>();
    for (int r : rows) band.put(r, new ArrayList<>());
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
        if (mc == null || mc.getSize() == 0) continue;
        nonEmpty++; entries += mc.getSize();
        for (int k = 0; k < mc.getSize(); k++) {
          long id = mc.getIdForIndex(k);
          List<Long> b = band.get(Math.floorDiv(ilat(id), ROW));
          if (b != null) b.add(id);
        }
      }
    }
    close(pf);
    System.out.println(tile + " | decoded | 1-degree squares with data=" + degreesWithData + "/25 | micro-caches=" + caches
      + " | non-empty=" + nonEmpty + " | nodes=" + entries);
    check(degreesWithData > 0 && nonEmpty > 0 && entries > 0, "nothing decoded");

    // 2. Row counts.
    for (int r : rows) {
      int n = band.get(r).size();
      System.out.println(tile + " | row " + r + " | nodes in band=" + n + " | floor=" + FLOOR + " | " + (n >= FLOOR ? "ok" : "BELOW FLOOR"));
      check(n >= FLOOR, "row " + r + " holds " + n + " nodes, below the floor of " + FLOOR);
    }
    if (!failures.isEmpty()) return;

    // 3. Run-time ends, one route per row.
    MessageDigest md = MessageDigest.getInstance("SHA-256");
    for (int r : rows) {
      List<Long> b = band.get(r);
      Collections.sort(b);
      int n = b.size();
      boolean routed = false;
      for (int k = 0; k < ATTEMPTS && !routed; k++) {
        int s = (int) ((long) n * (2 * k + 1) / (2L * ATTEMPTS));
        long start = b.get(s), end = 0; boolean found = false;
        for (int j = s + 1; j < n; j++) {
          long c = b.get(j);
          if (ilon(c) - ilon(start) >= STEP && Math.abs(ilat(c) - ilat(start)) <= STEP) { end = c; found = true; break; }
        }
        String head = tile + " | row " + r + " | attempt " + (k + 1) + " of " + ATTEMPTS;
        if (!found) { System.out.println(head + " | no end"); continue; }
        OsmTrack tr = null; String err;
        try {
          RoutingEngine e = route(profiles, segs, start, end);
          tr = e.getFoundTrack(); err = e.getErrorMessage();
        } catch (Exception ex) { err = ex.getClass().getSimpleName() + " " + ex.getMessage(); }
        if (err != null || tr == null || tr.nodes.isEmpty()) {
          System.out.println(head + " | no route | error=" + noDigits(err));
          continue;
        }
        int inBand = 0;
        for (OsmPathElement pe : tr.nodes) if (Math.floorDiv(pe.getILat(), ROW) == r) inBand++;
        System.out.println(head + " | routed | track nodes=" + tr.nodes.size() + " | in band=" + inBand + " | distance=" + tr.distance + " m");
        notes.add("# row\t" + tile + "\t" + r + "\tnodes=" + n + "\tattempt=" + (k + 1) + "/" + ATTEMPTS
          + "\ttrack nodes=" + tr.nodes.size() + "\tin band=" + inBand + "\tdistance=" + tr.distance + " m");
        md.update(java.nio.ByteBuffer.allocate(16).putLong(start).putLong(end).array());
        routed = true;
      }
      check(routed, "row " + r + ": no route in " + ATTEMPTS + " attempts");
    }
    if (!failures.isEmpty()) return;
    StringBuilder hex = new StringBuilder();
    for (byte x : md.digest()) hex.append(String.format("%02x", x));
    String digest = hex.substring(0, 16);
    System.out.println(tile + " | ends digest=" + digest);
    notes.add("# ends\t" + tile + "\tdigest=" + digest);
  }

  // Forager's calls and profile, as TileCheck.java:76-80, with the waypoints set from the nodes themselves.
  static RoutingEngine route(File profiles, File segs, long start, long end) {
    List<OsmNodeNamed> wps = new ArrayList<>();
    String[] names = {"from", "to"}; long[] ids = {start, end};
    for (int i = 0; i < 2; i++) {
      OsmNodeNamed w = new OsmNodeNamed(); w.name = names[i]; w.ilon = ilon(ids[i]); w.ilat = ilat(ids[i]); wps.add(w);
    }
    RoutingParamCollector c = new RoutingParamCollector();
    RoutingContext rc = new RoutingContext();
    HashMap<String, String> p = new HashMap<>();
    p.put("profile", new File(profiles, "hiking-mountain.brf").getAbsolutePath());
    p.put("alternativeidx", "0"); p.put("profile:use_dynamic_range", "0");
    c.setParams(rc, wps, p);
    RoutingEngine e = new RoutingEngine(null, null, segs, wps, rc, RoutingEngine.BROUTER_ENGINEMODE_ROUTING);
    e.quite = true; e.doRun(0);
    return e;
  }
}
