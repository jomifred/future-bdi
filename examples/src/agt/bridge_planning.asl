// this agent computes the future to foresee problems
// the problem in case is being stuck in the middle of the bridge


destination(17,6).
+destination(X,Y) <- !pos(X,Y). // create a goal when my destination is perceived

// recovery plan
-!pos(X,Y)[error(no_future),error_msg(M)]
    : pos(CX,CY) // my location
   <- .print("Future failure for goal pos(",X,",",Y,"): ",M);
      jason.future.plan_for(
          pos(X,Y),
          { @[cost(0), preference(0)]+!pos(X,Y) : pos(CX,CY) },
          Plan, "SOLVE_P", stop_cond(full));
      .print("New plan = ",Plan);
      .add_plan(Plan, chunking, begin);
      !pos(X,Y);
   .
{ include("move.asl") }