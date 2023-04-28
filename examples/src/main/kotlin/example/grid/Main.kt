package example.grid

/** class used just to play with the implementation */

import jason.environment.grid.GridWorldView
import jason.future.Action
import jason.future.AgentModel
import jason.future.EnvironmentModel
import jason.future.Simulator
import kotlin.system.exitProcess

fun main() {
    val env = GridEnvModel(GridState(15,5),GridState(15,25))
    val view = GridWorldView(env,  "Future!", 800)
    view.isVisible = true

    val initialState = env.currentState()

    val plan = Simulator(  env, Robot() )
        .simulate( initialState, env.goalState)

    println("Plan: $plan")
    for ( (s,a) in plan) {
        println( "in $s do $a")
        env.setAgPos(0, s.l.x, s.l.y)
        Thread.sleep(200)
    }

    exitProcess(0)
}

class Robot : AgentModel<GridState> {

    private val visited = mutableSetOf<GridState>()

    override fun decide(e: EnvironmentModel<GridState>, s:GridState, goal:GridState): Action {
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

