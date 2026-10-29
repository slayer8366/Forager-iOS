import probe.port.CancelFlag
import probe.port.Engine
import probe.port.RouteOutcome
import probe.port.RouteRequest
import probe.port.javaSplit
import probe.port.withEngine

/** As Forager's cancel test: a subclass that overrides doRun. */
class HeldEngine(r: RouteRequest) : Engine(r, CancelFlag()) {
    override fun doRun(maxRunningTimeMillis: Long): RouteOutcome = RouteOutcome.Failed("held")
}

fun main() {
    val req = RouteRequest(-74.1, 41.2, -74.0, 41.3)
    val out = withEngine(req) { it.doRun(req.maxRunningTimeMillis) }
    val text = when (out) {   // exhaustive over a sealed interface from another module
        is RouteOutcome.Found -> "found ${out.iLat.size} ${out.distance}"
        is RouteOutcome.Failed -> "failed ${out.message}"
    }
    println("consumer kotlin=${KotlinVersion.CURRENT} result=$text held=${(HeldEngine(req).doRun(1) as RouteOutcome.Failed).message} split=${javaSplit("a|b||", Regex("\\|"))}")
}
