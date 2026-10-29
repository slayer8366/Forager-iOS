package probe.port

import kotlin.concurrent.Volatile
import kotlin.time.TimeSource

/** A stand-in for the port's surface: the shapes Forager's adapter would call. */
class RouteRequest(val startLon: Double, val startLat: Double, val endLon: Double, val endLat: Double, val maxRunningTimeMillis: Long = 30_000L)

sealed interface RouteOutcome {
    data class Found(val iLat: IntArray, val iLon: IntArray, val distance: Int) : RouteOutcome
    data class Failed(val message: String) : RouteOutcome
}

class CancelFlag {
    @Volatile var terminated: Boolean = false
}

/** Open, as Forager's cancel test subclasses RoutingEngine and overrides doRun. */
open class Engine(val request: RouteRequest, val cancel: CancelFlag) {
    open fun doRun(maxRunningTimeMillis: Long): RouteOutcome {
        val mark = TimeSource.Monotonic.markNow()
        if (cancel.terminated) return RouteOutcome.Failed("operation killed by thread")
        val ms = mark.elapsedNow().inWholeMilliseconds
        return if (ms > maxRunningTimeMillis) RouteOutcome.Failed("timeout") else
            RouteOutcome.Found(intArrayOf(130_000_000), intArrayOf(105_000_000), 0)
    }
}

/** An inline function: its body is inlined into the caller's bytecode, so the caller reads more than signatures. */
inline fun <T> withEngine(request: RouteRequest, block: (Engine) -> T): T = block(Engine(request, CancelFlag()))

fun javaSplit(s: String, regex: Regex): List<String> = s.split(regex).dropLastWhile { it.isEmpty() }
