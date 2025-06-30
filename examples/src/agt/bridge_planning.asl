// this agent computes the future to foresee problems
// the problem in case is being stuck in the middle of the bridge


destination(17,6).
+destination(X,Y) <- .wait(100); // wait for perception
                     !pos(X,Y).  // create a goal when my destination is perceived

// recovery plan
-!pos(X,Y)[error(future_issue(FI)),error_msg(M)]
    : pos(CX,CY) // my location
   <- .print("Future problem for goal pos(",X,",",Y,"): ",FI,", ",M);
      jason.future.plan_for(
          pos(X,Y),
          { @[cost(0), preference(0)]+!pos(X,Y) : pos(CX,CY) },
          Plan, "SOLVE_F", stop_cond(full));
      .print("New plan = ",Plan);
      .add_plan(Plan, chunking, begin);
      !pos(X,Y);
   .
{ include("move.asl") }