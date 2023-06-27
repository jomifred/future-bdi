// this agent computes the future to foresee problems
// the problem in case is being stuck in the middle of the bridge


destination(17,6).
+destination(X,Y) <- !pos(X,Y). // create a goal when my destination is perceived

{ include("move.asl") }