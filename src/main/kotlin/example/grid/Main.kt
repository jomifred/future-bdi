package example.grid

import jason.environment.grid.GridWorldView
import jason.future.*

fun main(args: Array<String>) {
    val env = Grid()
    val view = GridWorldView(env,  "Future!", 800)
    view.setVisible(true)

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
    System.exit(0)
}

class Robot : AgentModel<GridLocation> {

    val visited = mutableSetOf<GridLocation>()

    override fun decide(e: EnvironmentModel<GridLocation>, s:GridLocation, goal:GridLocation): Action {
        //println("deciding for $s "+visited)
        visited.add(s)

        return e.actions()
            .associate { it to e.next(s,it) }
            .filter { it.value != s && !visited.contains(it.value) }
            .minBy { a -> a.value.distance(goal as GridLocation) }
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

