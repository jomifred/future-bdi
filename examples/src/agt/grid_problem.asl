// agent that illustrates the problem
// it is not well implemented for walls

+destination(X,Y) <- !pos(X,Y). // create a goal when my destination is perceived
-destination(X,Y) <- .drop_all_desires. // drop everything if my destination is removed

{ include("move.asl") }
