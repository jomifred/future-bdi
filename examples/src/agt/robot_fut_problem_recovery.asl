// this agent computes the future to foresee problems
// the problem in case is returning (in the future) to a visited location
// (and so it does not need to maintain visited locations to avoid loops)

+destination(X,Y) <- !pos(X,Y). // create a goal when my destination is perceived
-destination(X,Y) <- .drop_all_desires. // drop everything if my destination is removed

// recovery plan
-!pos(X,Y)[error(no_future),error_msg(M)]
   <- .print(M);
      jason.future.plan_for(pos(X,Y),Plan);
      .print("New plan = ",Plan);
   .

{ include("move.asl") }
