package jason.future

import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.io.IOException

/** statistical data */
open class StatData {
    companion object {
        var gamma = 0.0
        var pChange = 0.0
        var scenario = "none"
        var nbPlanFor = 0
        private var nbMatrices = 0
        var nbVisitedStates = 0
        var strategy = ExplorationStrategy.SOLVE_M
        var requiredCertainty = 0.0
        var nbActions = 0
        var actionsCost = 0.0
        private var startT: Long = System.currentTimeMillis()

        fun addNbMatrices() {
            nbMatrices++
            /*if (nbMatrices > 5000) {
                System.exit(0)
            }*/
        }

        fun storeStats(timeout: Boolean) {
            try {
                val toS = if (timeout) "timeout" else "ontime"
                val newf = !File("stats.csv").exists()
                BufferedWriter(FileWriter("stats.csv", true)).use { out ->
                    if (newf)
                        out.appendLine("scenario, pChange, gamma, recovery_strategy, required_certainty, build_plans, matrices, visited_states, actions, time, timeout, cost")
                    //val sNbAct = if (timeout) (nbActions+500).toDouble() else nbActions.toString()
                    val sNbAct = nbActions.toString()
                    out.appendLine(
                        "$scenario, ${"%.2f".format(pChange)}, ${"%.4f".format(gamma)}, $strategy, ${
                            "%.2f".format(
                                requiredCertainty
                            )
                        }, $nbPlanFor, $nbMatrices, $nbVisitedStates, $sNbAct, ${System.currentTimeMillis() - startT}, $toS, ${
                            "%.2f".format(
                                actionsCost
                            )
                        }"
                    )
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }
}
