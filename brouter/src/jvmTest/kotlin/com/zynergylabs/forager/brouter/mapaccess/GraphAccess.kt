package com.zynergylabs.forager.brouter.mapaccess

import com.zynergylabs.forager.brouter.util.OracleLibm

/**
 * One engine's decoded graph, read the same way on both (step 5b, item I). [N] is the engine's `OsmNode`, [L] its
 * `OsmLink`. Every read is a public member or, for the map, the private `hmap` by reflection, so nothing in the port is
 * widened for the tests.
 */
internal interface GraphAccess<N : Any, L : Any> {
    fun ilon(n: N): Int
    fun ilat(n: N): Int
    fun selev(n: N): Short
    fun description(n: N): ByteArray?
    fun visitID(n: N): Int
    fun isHollow(n: N): Boolean
    fun firstLink(n: N): L?
    fun restrictions(n: N): List<String>
    fun turnForbidden(n: N, fromLon: Int, fromLat: Int, toLon: Int, toLat: Int, bike: Boolean, car: Boolean): Boolean

    fun next(l: L, source: N): L?
    fun target(l: L, source: N): N?
    fun isReverse(l: L, source: N): Boolean
    fun isBidirectional(l: L): Boolean
    fun linkDescription(l: L): ByteArray?
    fun geometry(l: L): ByteArray?

    /** The transfer nodes of [l]'s geometry seen from [source] towards [target], as "lon,lat,elev" joined by ";". */
    fun geometryText(decoder: Any, l: L, source: N, target: N): String
    fun newGeometryDecoder(): Any

    /** Three holders added (two from [source], one from [target]), then each side's chain read back as holder ids. */
    fun linkHolderText(l: L, source: N, target: N): String

    /** The nodes in the map's `hmap`, in its own iteration order. */
    fun hollowNodes(map: Any): List<N>
    fun nodesCreated(map: Any): Int
    fun lastVisitID(map: Any): Int
    fun baseID(map: Any): Int
}

/** A link holder with an id, for [GraphAccess.linkHolderText]. */
private class OracleHolder(val id: Int) : btools.mapaccess.OsmLinkHolder {
    private var nextHolder: btools.mapaccess.OsmLinkHolder? = null
    override fun setNextForLink(holder: btools.mapaccess.OsmLinkHolder?) {
        nextHolder = holder
    }
    override fun getNextForLink(): btools.mapaccess.OsmLinkHolder? = nextHolder
}

private class PortHolder(val id: Int) : OsmLinkHolder {
    private var nextHolder: OsmLinkHolder? = null
    override fun setNextForLink(holder: OsmLinkHolder?) {
        nextHolder = holder
    }
    override fun getNextForLink(): OsmLinkHolder? = nextHolder
}

internal object OracleGraph : GraphAccess<btools.mapaccess.OsmNode, btools.mapaccess.OsmLink> {
    override fun ilon(n: btools.mapaccess.OsmNode) = n.ilon
    override fun ilat(n: btools.mapaccess.OsmNode) = n.ilat
    override fun selev(n: btools.mapaccess.OsmNode) = n.selev
    override fun description(n: btools.mapaccess.OsmNode): ByteArray? = n.nodeDescription
    override fun visitID(n: btools.mapaccess.OsmNode) = n.visitID
    override fun isHollow(n: btools.mapaccess.OsmNode) = n.isHollow
    override fun firstLink(n: btools.mapaccess.OsmNode): btools.mapaccess.OsmLink? = n.firstlink
    override fun restrictions(n: btools.mapaccess.OsmNode): List<String> = generateSequence(n.firstRestriction) { it.next }
        .map { "${it.exceptions} ${it.isPositive} ${it.fromLon} ${it.fromLat} ${it.toLon} ${it.toLat} ${it.exceptBikes()} ${it.exceptMotorcars()}" }
        .toList()
    override fun turnForbidden(n: btools.mapaccess.OsmNode, fromLon: Int, fromLat: Int, toLon: Int, toLat: Int, bike: Boolean, car: Boolean) =
        btools.mapaccess.TurnRestriction.isTurnForbidden(n.firstRestriction, fromLon, fromLat, toLon, toLat, bike, car)

    override fun next(l: btools.mapaccess.OsmLink, source: btools.mapaccess.OsmNode): btools.mapaccess.OsmLink? = l.getNext(source)
    override fun target(l: btools.mapaccess.OsmLink, source: btools.mapaccess.OsmNode): btools.mapaccess.OsmNode? = l.getTarget(source)
    override fun isReverse(l: btools.mapaccess.OsmLink, source: btools.mapaccess.OsmNode) = l.isReverse(source)
    override fun isBidirectional(l: btools.mapaccess.OsmLink) = l.isBidirectional
    override fun linkDescription(l: btools.mapaccess.OsmLink): ByteArray? = l.descriptionBitmap
    override fun geometry(l: btools.mapaccess.OsmLink): ByteArray? = l.geometry

    override fun newGeometryDecoder(): Any = btools.mapaccess.GeometryDecoder()
    override fun geometryText(decoder: Any, l: btools.mapaccess.OsmLink, source: btools.mapaccess.OsmNode, target: btools.mapaccess.OsmNode): String =
        generateSequence((decoder as btools.mapaccess.GeometryDecoder).decodeGeometry(l.geometry, source, target, l.isReverse(source))) { it.next }
            .joinToString(";") { "${it.ilon},${it.ilat},${it.selev}" }

    override fun linkHolderText(l: btools.mapaccess.OsmLink, source: btools.mapaccess.OsmNode, target: btools.mapaccess.OsmNode): String {
        l.addLinkHolder(OracleHolder(1), source)
        l.addLinkHolder(OracleHolder(2), source)
        l.addLinkHolder(OracleHolder(3), target)
        fun chain(from: btools.mapaccess.OsmNode) =
            generateSequence(l.getFirstLinkHolder(from)) { it.nextForLink }.joinToString(",") { (it as OracleHolder).id.toString() }
        return "${chain(source)}|${chain(target)}"
    }

    @Suppress("UNCHECKED_CAST")
    override fun hollowNodes(map: Any): List<btools.mapaccess.OsmNode> =
        (MapaccessEngines.field(map, "hmap") as Map<btools.mapaccess.OsmNode, btools.mapaccess.OsmNode>).values.toList()
    override fun nodesCreated(map: Any) = (map as btools.mapaccess.OsmNodesMap).nodesCreated
    override fun lastVisitID(map: Any) = (map as btools.mapaccess.OsmNodesMap).lastVisitID
    override fun baseID(map: Any) = (map as btools.mapaccess.OsmNodesMap).baseID
}

internal object PortGraph : GraphAccess<OsmNode, OsmLink> {
    override fun ilon(n: OsmNode) = n.ilon
    override fun ilat(n: OsmNode) = n.ilat
    override fun selev(n: OsmNode) = n.selev
    override fun description(n: OsmNode): ByteArray? = n.nodeDescription
    override fun visitID(n: OsmNode) = n.visitID
    override fun isHollow(n: OsmNode) = n.isHollow()
    override fun firstLink(n: OsmNode): OsmLink? = n.firstlink
    override fun restrictions(n: OsmNode): List<String> = generateSequence(n.firstRestriction) { it.next }
        .map { "${it.exceptions} ${it.isPositive} ${it.fromLon} ${it.fromLat} ${it.toLon} ${it.toLat} ${it.exceptBikes()} ${it.exceptMotorcars()}" }
        .toList()
    override fun turnForbidden(n: OsmNode, fromLon: Int, fromLat: Int, toLon: Int, toLat: Int, bike: Boolean, car: Boolean) =
        TurnRestriction.isTurnForbidden(n.firstRestriction, fromLon, fromLat, toLon, toLat, bike, car)

    override fun next(l: OsmLink, source: OsmNode): OsmLink? = l.getNext(source)
    override fun target(l: OsmLink, source: OsmNode): OsmNode? = l.getTarget(source)
    override fun isReverse(l: OsmLink, source: OsmNode) = l.isReverse(source)
    override fun isBidirectional(l: OsmLink) = l.isBidirectional()
    override fun linkDescription(l: OsmLink): ByteArray? = l.descriptionBitmap
    override fun geometry(l: OsmLink): ByteArray? = l.geometry

    override fun newGeometryDecoder(): Any = GeometryDecoder()
    override fun geometryText(decoder: Any, l: OsmLink, source: OsmNode, target: OsmNode): String =
        generateSequence((decoder as GeometryDecoder).decodeGeometry(l.geometry, source, target, l.isReverse(source))) { it.next }
            .joinToString(";") { "${it.ilon},${it.ilat},${it.selev}" }

    override fun linkHolderText(l: OsmLink, source: OsmNode, target: OsmNode): String {
        l.addLinkHolder(PortHolder(1), source)
        l.addLinkHolder(PortHolder(2), source)
        l.addLinkHolder(PortHolder(3), target)
        fun chain(from: OsmNode) =
            generateSequence(l.getFirstLinkHolder(from)) { it.getNextForLink() }.joinToString(",") { (it as PortHolder).id.toString() }
        return "${chain(source)}|${chain(target)}"
    }

    override fun hollowNodes(map: Any): List<OsmNode> =
        (MapaccessEngines.field(map, "hmap") as NodeHashMap).valuesIterator().asSequence().toList()
    override fun nodesCreated(map: Any) = (map as OsmNodesMap).nodesCreated
    override fun lastVisitID(map: Any) = (map as OsmNodesMap).lastVisitID
    override fun baseID(map: Any) = (map as OsmNodesMap).baseID
}

/** The logic run's seam for `calcDistance` (Amendment 3: logic runs inject the oracle's own table). */
internal val logicLibm = OracleLibm
