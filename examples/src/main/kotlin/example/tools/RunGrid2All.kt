package example.tools

import java.io.FileWriter
import java.util.*
import java.util.concurrent.TimeUnit


fun main(args: Array<String>) {
    run("SOLVE_P","grid2-l")
    run("SOLVE_M","grid2-l")
    run("SOLVE_F","grid2-l")
    run("SOLVE_P","grid2-u")
    run("SOLVE_M","grid2-u")
    run("SOLVE_F","grid2-u")
    run("SOLVE_P","grid2-h")
    run("SOLVE_M","grid2-h")
    run("SOLVE_F","grid2-h")
}

fun run(strategy: String, grid: String) {
    val conf = Properties()
    conf.setProperty("recover_strategy", strategy)
    conf.store(FileWriter("examples/params.properties"), "conf")

    val process = Runtime.getRuntime()
        .exec("./gradlew -q :examples:run --args=run-all-grid-2/$grid.mas2j")


    var failure = "ok"
    process.waitFor(30, TimeUnit.SECONDS)
    if (process.isAlive) {
        process.destroyForcibly()
        failure = "timeout"
    }

    println("$strategy: $failure")

}
