package example.grid

/** class used just to play with the implementation */

import jason.environment.grid.GridWorldView
import jason.future.*
import kotlin.system.exitProcess

fun main() {
    val env = GridEnvModel()
    val view = GridWorldView(env,  "Future!", 800)
    view.isVisible = true

    val initialState = GridLocation(env.getAgPos(0).x, env.getAgPos(0).y)
    val goalState    = GridLocation(15,25)

    val plan = Simulator(  env, Robot() )
        .simulate( initialState, goalState)

    println("Plan: $plan")
    for ( (s,a) in plan) {
        println( "in $s do $a")
        env.setAgPos(0, s.x(), s.y())
        Thread.sleep(200)
    }
    exitProcess(0)
}

class Robot : AgentModel<GridLocation> {

    private val visited = mutableSetOf<GridLocation>()

    override fun decide(e: EnvironmentModel<GridLocation>, s:GridLocation, goal:GridLocation): Action {
        //println("deciding for $s "+visited)
        visited.add(s)

        return e.actions()
            .associateWith { e.next(s,it) }
            .filterValues { it != s && !visited.contains(it) }
            .minBy { a -> a.value.distance(goal) }
            .key

        // get all action, select the closer to goal
//        var bestAction = Action("n")
//        var bestDist = Double.MAX_VALUE
//        for (a in e.actions()) {
//            val nextS = e.next(s,a) as GridLocation
//            if (nextS != s && !visited.contains(nextS)) {
//                val nextDist = nextS.distance(goal as GridLocation)
//                if (nextDist < bestDist) {
//                    bestDist = nextDist
//                    bestAction = a
//                }
//            }
//        }
//        return bestAction
    }
}

