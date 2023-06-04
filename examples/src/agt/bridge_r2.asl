destination(2,6).
+destination(X,Y) <- !pos(X,Y). // create a goal when my destination is perceived

{ include("move.asl") }