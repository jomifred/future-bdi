// this agent computes the future to foresee problems
// the problem in case is returning (in the future) to a visited location
// (and so it does not need to maintain visited locations to avoid loops)

+destination(X,Y) <- !pos(X,Y). // create a goal when my destination is perceived
// NOT NEEDED: +pos(X,Y)     <- +visited(X,Y). // remember visited locations

@  [preference(0)] +!pos(X,Y) : pos(X,Y).
@s [preference(D)] +!pos(X,Y) : ok(s, D) <- s;  !pos(X,Y).
@sw[preference(D)] +!pos(X,Y) : ok(sw,D) <- sw; !pos(X,Y).
@se[preference(D)] +!pos(X,Y) : ok(se,D) <- se; !pos(X,Y).
@w [preference(D)] +!pos(X,Y) : ok(w ,D) <- w;  !pos(X,Y).
@e [preference(D)] +!pos(X,Y) : ok(e ,D) <- e;  !pos(X,Y).
@n [preference(D)] +!pos(X,Y) : ok(n ,D) <- n;  !pos(X,Y).
@nw[preference(D)] +!pos(X,Y) : ok(nw,D) <- nw; !pos(X,Y).
@ne[preference(D)] +!pos(X,Y) : ok(ne,D) <- ne; !pos(X,Y).

// checks if go to some direction is possible (free cell)
// and computes the distance from goal to there
//
ok(s ,D) :- pos(X,Y) & unknown_free(X  ,Y+1) & distance(X  ,Y+1,D).
ok(sw,D) :- pos(X,Y) & unknown_free(X-1,Y+1) & distance(X-1,Y+1,D).
ok(se,D) :- pos(X,Y) & unknown_free(X+1,Y+1) & distance(X+1,Y+1,D).
ok(w ,D) :- pos(X,Y) & unknown_free(X-1,Y)   & distance(X-1,Y  ,D).
ok(e ,D) :- pos(X,Y) & unknown_free(X+1,Y)   & distance(X+1,Y  ,D).
ok(n, D) :- pos(X,Y) & unknown_free(X  ,Y-1) & distance(X  ,Y-1,D).
ok(nw,D) :- pos(X,Y) & unknown_free(X-1,Y-1) & distance(X-1,Y-1,D).
ok(ne,D) :- pos(X,Y) & unknown_free(X+1,Y-1) & distance(X+1,Y-1,D).

unknown_free(X,Y) :-
    X >= 0 & Y >= 0 & w_size(W,H) & X < W & Y < H
    & not obstacle(X,Y).
    // NOT NEEDED: & not visited(X,Y).

//distance(X,Y,D) :- visited(X,Y) & destination(GX,GY) & D = 100+math.sqrt( (X-GX)**2 + (Y-GY)**2 ).

distance(X,Y,D) :- destination(GX,GY) & D = math.sqrt( (X-GX)**2 + (Y-GY)**2 ).