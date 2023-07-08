// this agent computes the future to foresee problems
// the problem in case is returning (in the future) to a visited location
// (and so it does not need to maintain visited locations to avoid loops)

+destination(X,Y) <- !pos(X,Y). // create a goal when my destination is perceived
-destination(X,Y) <- .drop_all_desires. // drop everything if my destination is removed

// recovery plan
-!pos(X,Y)[error(no_future),error_msg(M)]
    : pos(CX,CY) // my location
      & r_strategy(Sg)
   <- .print("Future failure for goal pos(",X,",",Y,"): ",M);
      jason.future.plan_for(
          pos(X,Y),
          { @[cost(0), preference(0)]+!pos(X,Y) : pos(CX,CY) },
          Plan, Sg, stop_cond(full)); // full or ag
      .print("New plan = ",Plan);
      .add_plan(Plan, chunking, begin);
      !pos(X,Y);
   .

-!pos(X,Y)[error(action_failed),error_msg(M)]
    : pos(CX,CY) // my location
   <- .print("Action failure for goal pos(",X,",",Y,"): ",M);
      !pos(X,Y);
   .

// NB. if the internal action 'plan_for' fails, the goal should be dropped

{ include("move.asl") }
