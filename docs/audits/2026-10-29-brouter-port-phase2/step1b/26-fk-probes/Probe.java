import btools.router.*;
import java.io.File;
import java.util.*;
public class Probe {
  // args: segments profilesDir profile; stdin lines: id sLat sLon eLat eLon
  public static void main(String[] a) throws Exception {
    Scanner in = new Scanner(System.in);
    while (in.hasNext()) {
      String id = in.next(); double sLat = in.nextDouble(), sLon = in.nextDouble(), eLat = in.nextDouble(), eLon = in.nextDouble();
      RoutingParamCollector c = new RoutingParamCollector();
      List<OsmNodeNamed> wps = c.getWayPointList(sLon + "," + sLat + "|" + eLon + "," + eLat);
      RoutingContext rc = new RoutingContext();
      Map<String,String> p = new HashMap<>();
      p.put("profile", new File(a[1], a[2] + ".brf").getAbsolutePath());
      p.put("alternativeidx", "0"); p.put("profile:use_dynamic_range", "0");
      c.setParams(rc, wps, p);
      RoutingEngine e = new RoutingEngine(null, null, new File(a[0]), wps, rc, RoutingEngine.BROUTER_ENGINEMODE_ROUTING);
      e.quite = true;
      e.doRun(0);
      System.out.println(id + "\t" + e.getErrorMessage());
    }
  }
}
