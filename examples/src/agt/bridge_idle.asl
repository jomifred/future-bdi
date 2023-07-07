// this agent computes the future to foresee problems
// the problem in case is being stuck in the middle of the bridge
//
// and in case of failure, does idle and try again

destination(17,6).
+destination(X,Y) <- !pos(X,Y). // create a goal when my destination is perceived

// recovery plan
-!pos(X,Y)
   <- .print("problem in the future, idle for now... try later");
      idle;
      !pos(X,Y).

{ include("move.asl") }