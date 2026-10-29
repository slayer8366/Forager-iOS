# Part 6, item H step 1: classifies every method in out/T2/sites.tsv, by hand from the source (reasons below cite
# J = third_party/brouter/src/main/java/btools/ at Forager e2969abe4), and checks that the table covers sites.tsv
# exactly. usage: python3 -I classify.py <sites.tsv> <out classification.tsv>
#
# Classes:
#   R    can choose between routes, or change the cost, the node list or the failure kind, on Forager's calls
#        with some profile ("Any profile", RECORD -8)
#   Rp   same, but only with a request parameter Forager never passes (heading, nogos, via points, roundtrip)
#   P    parse or setup: turns profile or lookup text into the numbers R sites use
#   D    output Forager reads: track.distance (BRouterTrailRouter.kt:115)
#   O    output Forager does not read: time, energy, elevations, ascend, turn angles, voice hints
#   M    messages, logs, debug text
#   H    hash iteration (step 3)
#   X    in a branch the port fails as "unsupported" or never runs for Forager (B rows 4, 14)
# Kinds: basic = IEEE + - * / sqrt, compare, int/float conversions (exact or correctly rounded by IEEE 754);
#        libm = a library function (cos, sin, exp, atan2) whose last bit IEEE 754 does not fix;
#        table = reads CheapRuler's scale table, which is built with cos; parse/print = text conversion.
import sys
C = {
 'BExpression.createAssignExpressionFromKeyValue': ('P','parse','profile: values become constants (BExpression.java:401); Forager passes one, use_dynamic_range'),
 'BExpression.divide': ('R','basic','profile evaluator, feeds every cost (BExpression.java:369-370)'),
 'BExpression.evaluate': ('R','basic','profile evaluator: add, multiply, compare (BExpression.java:272-310)'),
 'BExpression.max': ('R','basic','profile evaluator (BExpression.java:361)'),
 'BExpression.min': ('R','basic','profile evaluator (BExpression.java:365)'),
 'BExpression.parseRaw': ('P','parse','profile number literals; a token that does not parse is "unknown expression", a failure (BExpression.java:233-238)'),
 'BExpression.toString': ('M','print','debug text (BExpression.java:376)'),
 'BExpression.tryCollapse': ('P','basic','constant folding at parse, comparisons with constants (BExpression.java:322-340)'),
 'BExpressionContext._parseFile': ('H','-','profile: keys in map order (BExpressionContext.java:860-865); step 3'),
 'BExpressionContext.addLookupValue': ('X','parse','every float site is in the unknown-value branch :572-696, which B row 14 makes "unsupported"'),
 'BExpressionContext.getKeyValueDescription': ('R','print','Float.toString((val-1000)/100f) (:225); its text reaches a cost only through StdPath.java:98-130, bikeMode with consider_crossing (both profile settings), which reads one character; otherwise messages and postElevationCheck (elevations)'),
 'BExpressionContext.getLookupValue': ('R','parse','tag values become profile inputs: Float.parseFloat of a lookup value name, or (val-1000)/100f (BExpressionContext.java:258-272)'),
 'BExpressionContextWay.accessType': ('R','basic','compares way-context results with constants (BExpressionContextWay.java:118-125)'),
 'BExpressionMetaData.readMetaData': ('H','-','only which context a failure names (part 5)'),
 'NodesCache.close': ('H','-','closes files (part 5)'),
 'NodesCache.matchWaypointsToNodes': ('R','basic','mwp.radius > RETRY_RANGE decides a second, wider preload for matching (NodesCache.java:298-300)'),
 'OsmNode.calcDistance': ('R','table','Math.round(CheapRuler.distance) (OsmNode.java:103): air distance for the open-set key and pruning (RoutingEngine.java:1854, 2230), catching range (:1637), outreach (OsmNodesMap.java:198)'),
 'OsmNodesMap.collectOutreachers': ('H','-','memory-pressure path; step 3'),
 'WaypointMatcherImpl$1.compare': ('R','basic','orders candidate ways by radius, then directionDiff (WaypointMatcherImpl.java:66-73); wayNearest.get(0) becomes the match (:229-235)'),
 'WaypointMatcherImpl.<init>': ('R','libm','directionToNext from CheapAngleMeter.getDirection, atan2 (WaypointMatcherImpl.java:52, :61), the tie-break above'),
 'WaypointMatcherImpl.checkSegment': ('R','table','radius and crosspoint of each waypoint (WaypointMatcherImpl.java:80-150); crosspoint is (int)(xm/dlon2m + wp.ilon), the first and last route node'),
 'WaypointMatcherImpl.hasMatch': ('R','basic','mwp.radius < maxDistance (WaypointMatcherImpl.java:245)'),
 'Formatter.format1': ('M','basic','text formatting'),
 'Formatter.getFormattedEnergy': ('M','basic','text formatting'),
 'Formatter.getFormattedTime2': ('M','basic','text formatting'),
 'MessageData.toString': ('M','print','message text (MessageData.java:77)'),
 'OsmPath.addAddionalPenalty': ('R','basic','the per-link cost: classifier differences (OsmPath.java:158-161), section and target costs (:306, :380), (int) casts (:166, :315, :384). Its one libm use, sin/cos at :282-283, is Rp: startDirectionValid needs forceUseStartDirection or fastPartialRecalc (RoutingEngine.java:1931)'),
 'OsmPath.interpolateEle': ('R','basic','(short)(e1*(1-f) + e2*f) (OsmPath.java:392): elevation at a partial start or end link (:244, :255, :266), which feeds the elevation cost through delta_h (:296)'),
 'OsmPathElement.calcDistance': ('D','table','Math.round(CheapRuler.distance) (OsmPathElement.java:78), summed into track.distance (RoutingEngine.java:1512, :1522, :1565)'),
 'OsmPathElement.getElev': ('O','basic','elevation in metres (OsmPathElement.java:44)'),
 'OsmTrack.appendTrack': ('O','basic','time and energy offsets (OsmTrack.java:347-348, :384)'),
 'OsmTrack.getTotalSeconds': ('O','basic','time (OsmTrack.java:426-427)'),
 'OsmTrack.processVoiceHints': ('O','basic','voice hints; no voice-hint class removes nodes or sets a cost (grep of VoiceHint*.java)'),
 'OsmTrack.readBinary': ('X','basic','body after filename != null never runs for Forager (B row 4)'),
 'RoutingContext.calcDistance': ('R','table','per-link distance for the cost (OsmPath.java:238, :263); with a waypoint in nogopoints it also computes the shortest-match point ilonshortest/ilatshortest (RoutingContext.java:504-523), the first and last route node'),
 'RoutingContext.checkMatchedWaypointAgainstNogos': ('Rp','basic','nogos only (RoutingContext.java:350-351)'),
 'RoutingContext.cleanNogoList': ('Rp','basic','nogos only (RoutingContext.java:324)'),
 'RoutingContext.getKeyValueChecksum': ('H','-','order-free sum of hash codes (part 5)'),
 'RoutingContext.getNogoChecksums': ('Rp','basic','nogo checksum for the profile cache key (RoutingContext.java:421)'),
 'RoutingContext.readGlobalConfig': ('P','basic','profile globals: != 0.f switches and float-to-int/double conversions (RoutingContext.java:118-203)'),
 'RoutingEngine._findTrack': ('R','basic','open-set key cost + (int)(airdistance*airDistanceCostFactor) (RoutingEngine.java:1994, :2022) and the pruning bound (:2234); :1896 is a log line, :2193 a voice-hint detour'),
 'RoutingEngine.addToOpenset': ('R','basic','open-set key (RoutingEngine.java:2265)'),
 'RoutingEngine.compileTrack': ('O','basic','energy and time (RoutingEngine.java:2282, :2298-2299)'),
 'RoutingEngine.doRouting': ('M','print','log line (RoutingEngine.java:317)'),
 'RoutingEngine.getStartPath': ('R','basic','endPos.radius < 1.5 and wp.radius < minradius choose the start link (RoutingEngine.java:1825, :1855)'),
 'RoutingEngine.matchWaypointsToNodes': ('R','basic','mwp.radius >= waypointCatchingRange sends a waypoint to the dynamic-range second pass (RoutingEngine.java:1621), which Forager turns on with use_dynamic_range; :1637 needs buildBeelineOnRange'),
 'RoutingEngine.postElevationCheck': ('O','parse','elevations only: setSElev (RoutingEngine.java:785-800); Double.parseDouble of incline tags (:760)'),
 'RoutingEngine.recalcTrack': ('D','basic','track.distance is the sum of int calcDistance values (RoutingEngine.java:1512-1565); the float arithmetic here is time, energy, ascend and turn angles (O)'),
 'RoutingEngine.searchRoutedTrack': ('R','basic','a negative pass coefficient skips a pass (RoutingEngine.java:1743)'),
 'RoutingEngine.snapPathConnection': ('Rp','libm','via points only: returns false unless the waypoint name starts with via or rt (RoutingEngine.java:1266-1267); Forager names its two from and to (RoutingParamCollector.java:48-50)'),
 'RoutingEngine.snapRoundaboutConnection': ('Rp','basic','called only from snapPathConnection (RoutingEngine.java:1356)'),
 'RoutingParamCollector.readPosition': ('R','parse','Double.parseDouble (:90), then (int)((lon+180.)*1000000.+0.5) (:96-97): the waypoints'),
 'RoutingParamCollector.setParams': ('H','-','caller map order (part 5)'),
 'StdPath.calcIncline': ('O','libm','exp, kinematic time and energy, detailMode only (StdPath.java:283, :292)'),
 'StdPath.computeKinematic': ('O','libm','exp, time and energy (StdPath.java:291-320)'),
 'StdPath.getTotalEnergy': ('O','basic','energy'),
 'StdPath.getTotalTime': ('O','basic','time'),
 'StdPath.processTargetNode': ('R','basic','node access and initialcost >= 1000000 (StdPath.java:237-241)'),
 'StdPath.processWaySection': ('R','basic','the way-section cost: turncost (int)((1.-cosangle)*turncostbase+0.2) (StdPath.java:89), elevation buffers (:153-219), costfactor blend (:222), dist*costfactor+0.5f (:228)'),
 'StdPath.solveCubic': ('O','basic','kinematic speed'),
 'VoiceHint.calcCommand': ('O','basic','voice hints'),
 'VoiceHint.is180DegAngle': ('O','basic','voice hints'),
 'VoiceHintProcessor.checkStraightHold': ('O','basic','voice hints'),
 'VoiceHintProcessor.postProcess': ('O','basic','voice hints'),
 'VoiceHintProcessor.process': ('O','basic','voice hints'),
 'VoiceHintProcessor.sumNonConsumedWithinCatchingRange': ('O','basic','voice hints'),
 'CheapAngleMeter.calcAngle': ('R','table','cosangle feeds turncost (StdPath.java:89); the sign of angle feeds the crossing penalty (:98, :121)'),
 'CheapAngleMeter.getAngle': ('R','libm','atan2 and toDegrees (CheapAngleMeter.java:54), for WaypointMatcherImpl directionDiff'),
 'CheapAngleMeter.getDifferenceFromDirection': ('R','basic','(b2-b1) % 360.0 (fmod, exact), abs (CheapAngleMeter.java:69-72)'),
 'CheapAngleMeter.normalize': ('R','basic','a - 360*(int)(a/360) (CheapAngleMeter.java:64-65)'),
 'CheapRuler.calcKxKyFromILat': ('R','libm','Math.cos for the 1800-entry scale table (CheapRuler.java:37), read by every distance, angle and match'),
 'CheapRuler.distance': ('R','table','sqrt(dlat*dlat + dlon*dlon) (CheapRuler.java:78); R through OsmNode.calcDistance, D through OsmPathElement.calcDistance'),
}
sites, out = sys.argv[1], sys.argv[2]
seen = {}
for i, line in enumerate(open(sites)):
    if i == 0: continue
    p = line.rstrip('\n').split('\t')
    k = p[0].split('/')[-1] + '.' + p[1]
    seen.setdefault(k, []).append(p[3])
missing = sorted(set(seen) - set(C)); extra = sorted(set(C) - set(seen))
with open(out, 'w') as f:
    f.write('method\tclass\tkind\tcategories in sites.tsv\treason (J paths, Forager e2969abe4)\n')
    for k in sorted(seen):
        c = C.get(k, ('?', '?', 'NOT CLASSIFIED'))
        f.write(f'{k}\t{c[0]}\t{c[1]}\t{",".join(sorted(set(seen[k])))}\t{c[2]}\n')
from collections import Counter
cnt = Counter(C[k][0] for k in seen if k in C)
print(f'methods in sites.tsv: {len(seen)}; classified: {len(seen) - len(missing)}; missing: {missing}; extra: {extra}')
print('by class:', dict(sorted(cnt.items())))
print('R methods by kind:', dict(Counter(C[k][1] for k in seen if k in C and C[k][0] == 'R')))
