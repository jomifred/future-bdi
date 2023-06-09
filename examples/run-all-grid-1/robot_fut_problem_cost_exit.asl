// this agent computes the future to foresee problems
// the problem in case is returning (in the future) to a visited location
// (and so it does not need to maintain visited locations to avoid loops)

+destination(X,Y) <- !pos(X,Y); .stopMAS. // create a goal when my destination is perceived
-destination(X,Y) <- .drop_all_desires. // drop everything if my destination is removed
-!pos(_,_) <- .stopMAS.

@  [preference(0),cost(0.0)] +!pos(X,Y) : pos(X,Y).
@s [preference(D),cost(1.0)] +!pos(X,Y) : ok(s) & distance(s ,D) <- s;  !pos(X,Y).
@sw[preference(D),cost(1.4)] +!pos(X,Y) : ok(sw)& distance(sw,D) <- sw; !pos(X,Y).
@se[preference(D),cost(1.4)] +!pos(X,Y) : ok(se)& distance(se,D) <- se; !pos(X,Y).
@w [preference(D),cost(1.0)] +!pos(X,Y) : ok(w) & distance(w ,D) <- w;  !pos(X,Y).
@e [preference(D),cost(1.0)] +!pos(X,Y) : ok(e) & distance(e ,D) <- e;  !pos(X,Y).
@n [preference(D),cost(1.0)] +!pos(X,Y) : ok(n) & distance(n ,D) <- n;  !pos(X,Y).
@nw[preference(D),cost(1.4)] +!pos(X,Y) : ok(nw)& distance(nw,D) <- nw; !pos(X,Y).
@ne[preference(D),cost(1.4)] +!pos(X,Y) : ok(ne)& distance(ne,D) <- ne; !pos(X,Y).

// checks if go to some direction is possible (free cell)
ok(D) :- next(D,X,Y) & free(X,Y).

next(s ,X  ,Y+1) :- pos(X,Y). // my next location if doing south
next(sw,X-1,Y+1) :- pos(X,Y).
next(se,X+1,Y+1) :- pos(X,Y).
next(w ,X-1,Y  ) :- pos(X,Y).
next(e ,X+1,Y  ) :- pos(X,Y).
next(n ,X  ,Y-1) :- pos(X,Y).
next(nw,X-1,Y-1) :- pos(X,Y).
next(ne,X+1,Y-1) :- pos(X,Y).

free(X,Y) :- X >= 0 & Y >= 0 & w_size(W,H) & X < W & Y < H & not obstacle(X,Y).
distance(Dir,Dist) :- next(Dir,X,Y) & destination(GX,GY) &
                      Dist = math.sqrt( (X-GX)**2 + (Y-GY)**2 ).