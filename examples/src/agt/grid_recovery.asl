// this agent computes the future to foresee problems
// the problem is returning (in the future) to a visited location
// (and so it does not need to maintain visited locations to avoid loops)
//
// in case of future problems, it replans using a different options from the plan library


+destination(X,Y) <- npl.reset; !pos(X,Y). // create a goal when my destination is perceived
-destination(X,Y) <- .drop_all_desires. // drop everything if my destination is removed

// recovery plan
-!pos(X,Y)[error(future_issue(FI)),error_msg(M)]
    : pos(CX,CY) // my location
      & r_strategy(Sg) & Sg \== "ONE"  // for now, r_strategy is "perceived" in the environment
   <- .print("Future problem for goal pos(",X,",",Y,"): ",FI,", ",M);
      jason.future.plan_for(
          pos(X,Y), // the goal to plan for
          { @[cost(0), preference(0)]+!pos(X,Y) : pos(CX,CY) }, // initial plan
          Plan,  // the produced plan
          Sg, 
          stop_cond(ag)); // full (default conditions) or ag (agent specific conditions)
      .print("New plan = ",Plan);
      .add_plan(Plan, chunking, begin);
      !pos(X,Y);
   .

-!pos(X,Y)[error(action_failed),error_msg(M)]
    : pos(CX,CY) // my location
   <- .print("Action failure for goal pos(",X,",",Y,"): ",M);
      idle;
      !pos(X,Y);
   .

// NB. if the internal action 'plan_for' fails, the goal should be dropped

{ include("move.asl") }
