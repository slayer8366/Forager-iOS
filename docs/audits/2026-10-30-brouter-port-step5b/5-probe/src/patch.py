# Adds counters to copies of four BRouter 1.7.10 classes. Each edit must match exactly once, or the patch stops.
import sys
root = sys.argv[1]
def patch(rel, edits):
    p = root + '/' + rel
    s = open(p).read()
    for old, new in edits:
        if s.count(old) != 1:
            raise SystemExit('patch %s: %d matches for %r' % (rel, s.count(old), old))
        s = s.replace(old, new)
    open(p, 'w').write(s)

patch('btools/util/BitCoderContext.java', [
    ("      if (idx++ < idxMax) {\n        b |= (ab[idx] & 0xff) << bits;\n      }\n",
     "      if (idx++ < idxMax) {\n        b |= (ab[idx] & 0xff) << bits;\n      } else {\n        probe.Probe.endOfArray(this, ab.length);\n      }\n"),
])
patch('btools/codec/StatCoderContext.java', [
    ("  public int decodeNoisyNumber(int noisybits) {\n",
     "  public int decodeNoisyNumber(int noisybits) {\n    probe.Probe.noisyNumber(noisybits);\n"),
    ("  public int decodeNoisyDiff(int noisybits) {\n",
     "  public int decodeNoisyDiff(int noisybits) {\n    probe.Probe.noisyDiff(noisybits);\n"),
    ("    return predictor + decodeNoisyDiff(noisybits + noisy_bits[p]);",
     "    probe.Probe.inPredicted = true;\n    try {\n      return predictor + decodeNoisyDiff(noisybits + noisy_bits[p]);\n    } finally {\n      probe.Probe.inPredicted = false;\n    }"),
])
patch('btools/mapaccess/OsmNodesMap.java', [
    ("          } catch (StackOverflowError soe) {\n",
     "          } catch (StackOverflowError soe) {\n            probe.Probe.stackOverflows++;\n"),
    ("  private int minVisitIdInSubtree(OsmNode source, OsmNode n) {\n",
     "  private int minVisitIdInSubtree(OsmNode source, OsmNode n) {\n    probe.Probe.enter();\n    try {\n      return minVisitIdInSubtree0(source, n);\n    } finally {\n      probe.Probe.leave();\n    }\n  }\n\n  private int minVisitIdInSubtree0(OsmNode source, OsmNode n) {\n"),
    ("    return hmap.put(node, node);",
     "    OsmNode previous = hmap.put(node, node);\n    probe.Probe.afterPut(hmap, node);\n    return previous;"),
])
patch('btools/mapaccess/OsmNode.java', [
    ("  public final void parseNodeBody2(MicroCache2 mc, OsmNodesMap hollowNodes, IByteArrayUnifier expCtxWay) {\n",
     "  public final void parseNodeBody2(MicroCache2 mc, OsmNodesMap hollowNodes, IByteArrayUnifier expCtxWay) {\n    probe.Probe.parseNodeBody2++;\n"),
    ("    nodeDescription = nodeDescSize == 0 ? null : mc.readUnified(nodeDescSize, abUnifier);",
     "    if (nodeDescSize != 0) probe.Probe.readUnified134++;\n    nodeDescription = nodeDescSize == 0 ? null : mc.readUnified(nodeDescSize, abUnifier);"),
    ("  public final void parseNodeBody(MicroCache mc, OsmNodesMap hollowNodes, IByteArrayUnifier expCtxWay) {\n",
     "  public final void parseNodeBody(MicroCache mc, OsmNodesMap hollowNodes, IByteArrayUnifier expCtxWay) {\n    probe.Probe.parseNodeBody++;\n"),
])
print('patched')
