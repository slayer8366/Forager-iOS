package com.zynergylabs.forager.brouter.expressions

import com.zynergylabs.forager.brouter.util.BitCoderContext
import com.zynergylabs.forager.brouter.util.Crc32
import com.zynergylabs.forager.brouter.util.IByteArrayUnifier
import com.zynergylabs.forager.brouter.util.LruMap

/**
 * Port of BRouter 1.7.10's `btools.expressions.BExpressionContext`, in three files: this one (state, decoding, the
 * result cache, variables), `BExpressionContextLookups.kt` (the lookup table) and `BExpressionContextParse.kt`
 * (`parseFile` and the tokenizer). Java's private members the other two files use are `internal` here.
 *
 * Not ported, as T2 does not reach them: `encode`, `decode(byte[])`, `getKeyValueList`, `getLookupKey`,
 * `getLookupValue(boolean, byte[], int)`, `cacheStats`, `dumpStatistics`, `createNewLookupData`,
 * `generateRandomValues`, `assertAllVariablesEqual`, `variableName`, `addLookupValue(String, int)`,
 * `addSmallestLookupValue`, `getBooleanLookupValue`, `setVariableValue`, `usedTagList`.
 *
 * `showErrors` and `disableExpressionCache` are Java's `Boolean.getBoolean` reads; Forager sets neither, so both are the
 * constant `false` (RECORD -39).
 */
internal abstract class BExpressionContext protected constructor(
    internal var context: String,
    hashSize: Int,
    val meta: BExpressionMetaData?,
) : IByteArrayUnifier {
    // The tokenizer's state. Java holds a BufferedReader; the port holds the file's text and a read position.
    internal var inOurContext = false
    internal var parseText: String? = null
    internal var parsePos = 0
    internal var readerDone = false

    var modelClass: String? = null // Java's public `_modelClass`

    internal val lookupNumbers = HashMap<String, Int>()
    internal val lookupValues = ArrayList<Array<BExpressionLookupValue>>()
    internal val lookupNames = ArrayList<String>()
    internal val lookupHistograms = ArrayList<IntArray>()
    internal var lookupIdxUsed: BooleanArray? = null
    internal var lookupDataFrozen = false
    internal var lookupData = IntArray(0)

    private val ctxDecode = BitCoderContext(ByteArray(0))

    internal val variableNumbers = HashMap<String, Int>()
    internal var lastAssignedExpression: MutableList<BExpression?>? = ArrayList()
    internal var skipConstantExpressionOptimizations = false
    internal var expressionNodeCount = 0
    internal var variableData: FloatArray? = null

    // hash-cache for function results
    private val probeCacheNode = CacheNode()
    private var cache: LruMap? = null
    private val probeVarSet = VarWrapper()
    private var resultVarCache: LruMap? = null

    internal var expressionList: List<BExpression>? = null
    internal var minWriteIdx = 0

    // build-in variable indexes for fast access
    internal var buildInVariableIdx: IntArray? = null
    internal var nBuildInVars = 0

    private var currentVars: FloatArray? = null
    private var currentVarOffset = 0
    private var foreignContext: BExpressionContext? = null

    var noStartWays = IntArray(0)

    internal val showErrors = false
    internal var linenr = 0
    private var lookupDataValid = false
    internal var parsedLines = 0
    internal var fixTagsWritten = false

    private var requests = 0L
    private var requests2 = 0L
    private var cachemisses = 0L
    private var lastCacheNode = CacheNode()
    private val ld2 = IntArray(512)

    init {
        meta?.registerListener(context, this)
        // disableExpressionCache is false (above), so hashSize stands
        if (hashSize > 0) {
            cache = LruMap(4 * hashSize, hashSize)
            resultVarCache = LruMap(4096, 4096)
        }
    }

    protected fun setInverseVars() {
        currentVarOffset = nBuildInVars
    }

    internal abstract fun getBuildInVariableNames(): Array<String>

    fun getBuildInVariable(idx: Int): Float = currentVars!![idx + currentVarOffset]

    /** Decodes a byte array into a lookup data array. */
    fun decode(ld: IntArray, inverseDirection: Boolean, ab: ByteArray) {
        val ctx = ctxDecode
        ctx.reset(ab)

        // start with first bit hardwired ("reversedirection")
        ld[0] = if (inverseDirection) 2 else 0

        // all others are generic
        var inum = 1
        while (true) {
            var delta = ctx.decodeVarBits()
            if (delta == 0) break
            if (inum + delta > ld.size) break // higher minor version is o.k.

            while (delta-- > 1) ld[inum++] = 0

            // see encoder for value rotation
            val dd = ctx.decodeVarBits()
            var d = if (dd == 7) 1 else if (dd < 7) dd + 2 else dd + 1
            if (d >= lookupValues[inum].size && d < 1000) d = 1 // map out-of-range to unknown
            ld[inum++] = d
        }
        while (inum < ld.size) ld[inum++] = 0
    }

    fun getKeyValueDescription(inverseDirection: Boolean, ab: ByteArray): String {
        val sb = StringBuilder(200)
        decode(lookupData, inverseDirection, ab)
        for (inum in 0 until lookupValues.size) { // loop over lookup names
            val va = lookupValues[inum]
            val v = lookupData[inum]
            val value = if (v >= 1000) ((v - 1000) / 100f).toString() else va[v].toString()
            if (value.isNotEmpty()) {
                if (sb.isNotEmpty()) sb.append(' ')
                sb.append(lookupNames[inum] + "=" + value)
            }
        }
        return sb.toString()
    }

    fun getLookupValue(key: Int): Float {
        val v = lookupData[key]
        if (v == 0) return Float.NaN
        return if (v < 900) {
            try {
                lookupValues[key][v].toString().toFloat()
            } catch (e: NumberFormatException) {
                0f
            }
        } else {
            (v - 1000) / 100f
        }
    }

    fun evaluate(lookupData2: IntArray) {
        lookupData = lookupData2
        evaluate()
    }

    private fun evaluate() {
        val list = expressionList!!
        val n = list.size
        for (expidx in 0 until n) {
            list[expidx].evaluate(this)
        }
    }

    override fun unify(ab: ByteArray, offset: Int, len: Int): ByteArray {
        probeCacheNode.ab = null // crc based cache lookup only
        probeCacheNode.hash = Crc32.crc(ab, offset, len)

        var cn = cache!!.get(probeCacheNode) as CacheNode?
        if (cn != null) {
            val cab = cn.ab!!
            if (cab.size == len) {
                for (i in 0 until len) {
                    if (cab[i] != ab[i + offset]) {
                        cn = null
                        break
                    }
                }
                if (cn != null) {
                    lastCacheNode = cn
                    return cn.ab!!
                }
            }
        }
        return ab.copyOfRange(offset, offset + len)
    }

    fun evaluate(inverseDirection: Boolean, ab: ByteArray) {
        requests++
        lookupDataValid = false // this is an assertion for a nasty pifall

        val cache = cache
        if (cache == null) {
            decode(lookupData, inverseDirection, ab)
            var vars = currentVars
            if (vars == null || vars.size != nBuildInVars) {
                vars = FloatArray(nBuildInVars)
                currentVars = vars
            }
            evaluateInto(vars, 0)
            currentVarOffset = 0
            return
        }

        var cn: CacheNode?
        if (lastCacheNode.ab === ab) {
            cn = lastCacheNode
        } else {
            probeCacheNode.ab = ab
            probeCacheNode.hash = Crc32.crc(ab, 0, ab.size)
            cn = cache.get(probeCacheNode) as CacheNode?
        }

        if (cn == null) {
            cachemisses++

            cn = cache.removeLru() as CacheNode? ?: CacheNode()
            cn.hash = probeCacheNode.hash
            cn.ab = ab
            cache.put(cn)

            val probeVars = probeVarSet.vars ?: FloatArray(2 * nBuildInVars).also { probeVarSet.vars = it }

            // forward direction
            decode(lookupData, false, ab)
            evaluateInto(probeVars, 0)

            // inverse direction
            lookupData[0] = 2 // inverse shortcut: reuse decoding
            evaluateInto(probeVars, nBuildInVars)

            probeVarSet.hash = probeVars.contentHashCode() // Java's Arrays.hashCode(float[])

            // unify the result variable set
            val rvc = resultVarCache!!
            var vw = rvc.get(probeVarSet) as VarWrapper?
            if (vw == null) {
                vw = rvc.removeLru() as VarWrapper? ?: VarWrapper()
                vw.hash = probeVarSet.hash
                vw.vars = probeVarSet.vars
                probeVarSet.vars = null
                rvc.put(vw)
            }
            cn.vars = vw.vars
        } else {
            if (ab === cn.ab) requests2++

            cache.touch(cn)
        }

        currentVars = cn.vars
        currentVarOffset = if (inverseDirection) nBuildInVars else 0
    }

    private fun evaluateInto(vars: FloatArray, offset: Int) {
        evaluate()
        val idxs = buildInVariableIdx!!
        for (vi in 0 until nBuildInVars) {
            val idx = idxs[vi]
            vars[vi + offset] = if (idx == -1) 0f else variableData!![idx]
        }
    }

    fun getOutputVariableIndex(name: String, mustExist: Boolean): Int {
        val idx = getVariableIdx(name, false)
        if (idx < 0) {
            if (mustExist) {
                throw IllegalArgumentException("unknown variable: $name")
            }
        } else if (idx < minWriteIdx) {
            throw IllegalArgumentException("bad access to global variable: $name")
        }
        val idxs = buildInVariableIdx!!
        for (i in 0 until nBuildInVars) {
            if (idxs[i] == idx) {
                return i
            }
        }
        val extended = IntArray(nBuildInVars + 1)
        idxs.copyInto(extended, 0, 0, nBuildInVars)
        extended[nBuildInVars] = idx
        buildInVariableIdx = extended
        return nBuildInVars++
    }

    fun setForeignContext(foreignContext: BExpressionContext?) {
        this.foreignContext = foreignContext
    }

    fun getForeignVariableValue(foreignIndex: Int): Float = foreignContext!!.getBuildInVariable(foreignIndex)

    fun getForeignVariableIdx(context: String, name: String): Int {
        val fc = foreignContext
        if (fc == null || context != fc.context) {
            throw IllegalArgumentException("unknown foreign context: $context")
        }
        return fc.getOutputVariableIndex(name, true)
    }

    fun getVariableValue(name: String, defaultValue: Float): Float {
        val num = variableNumbers[name]
        return if (num == null) defaultValue else getVariableValue(num)
    }

    internal fun getVariableValue(variableIdx: Int): Float = variableData!![variableIdx]

    internal fun getVariableIdx(name: String, create: Boolean): Int {
        var num = variableNumbers[name]
        if (num == null) {
            if (create) {
                num = variableNumbers.size
                variableNumbers[name] = num
                lastAssignedExpression!!.add(null)
            } else {
                return -1
            }
        }
        return num
    }

    internal fun getLookupMatch(nameIdx: Int, valueIdxArray: IntArray): Float {
        for (i in valueIdxArray.indices) {
            if (lookupData[nameIdx] == valueIdxArray[i]) {
                return 1.0f
            }
        }
        return 0.0f
    }

    fun getLookupNameIdx(name: String): Int = lookupNumbers[name] ?: -1

    fun markLookupIdxUsed(idx: Int) {
        lookupIdxUsed!![idx] = true
    }

    fun isLookupIdxUsed(idx: Int): Boolean {
        val used = lookupIdxUsed!!
        return idx < used.size && used[idx]
    }

    fun setAllTagsUsed() {
        val used = lookupIdxUsed!!
        for (i in used.indices) {
            used[i] = true
        }
    }

    internal fun getLookupValueIdx(nameIdx: Int, value: String): Int {
        val values = lookupValues[nameIdx]
        for (i in values.indices) {
            if (values[i].equals(value)) return i // Java's equals(String): the primary value only
        }
        return -1
    }

    internal fun assign(variableIdx: Int, value: Float): Float {
        variableData!![variableIdx] = value
        return value
    }

    // Java's checkStartWay(byte[]) returns true for null; TagValueValidator (step 3) takes a non-null array here.
    fun checkStartWay(ab: ByteArray): Boolean {
        ld2.fill(0)
        decode(ld2, false, ab)
        var i = 0
        while (i < noStartWays.size) {
            val key = noStartWays[i]
            val value = noStartWays[i + 1]
            if (ld2[key] == value) return false
            i += 2
        }
        return true
    }

    fun freeNoWays() {
        noStartWays = IntArray(0)
    }
}
