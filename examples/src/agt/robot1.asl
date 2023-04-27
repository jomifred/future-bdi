w_size(30,30).

destination(15,25).

+destination(X,Y) <- !pos(X,Y).

@[preference(0)] +!pos(X,Y) : pos(X,Y). // done!
@[preference(D)] +!pos(X,Y) : ok(s, D)  <- s; !pos(X,Y).
@[preference(D)] +!pos(X,Y) : ok(sw,D) <- sw; !pos(X,Y).
@[preference(D)] +!pos(X,Y) : ok(se,D) <- se; !pos(X,Y).
@[preference(D)] +!pos(X,Y) : ok(w ,D) <- w;  !pos(X,Y).
@[preference(D)] +!pos(X,Y) : ok(e ,D) <- e;  !pos(X,Y).
@[preference(D)] +!pos(X,Y) : ok(n ,D) <- n;  !pos(X,Y).
@[preference(D)] +!pos(X,Y) : ok(nw,D) <- nw; !pos(X,Y).
@[preference(D)] +!pos(X,Y) : ok(ne,D) <- ne; !pos(X,Y).

// checks if go to some direction is possible (free cell)
// and computes the distance from goal to there
ok(s ,D) :- pos(X,Y) & free(X  ,Y+1) & distance(X  ,Y+1,D).
ok(sw,D) :- pos(X,Y) & free(X-1,Y+1) & distance(X-1,Y+1,D).
ok(se,D) :- pos(X,Y) & free(X+1,Y+1) & distance(X+1,Y+1,D).
ok(w ,D) :- pos(X,Y) & free(X-1,Y)   & distance(X-1,Y  ,D).
ok(e ,D) :- pos(X,Y) & free(X+1,Y)   & distance(X+1,Y  ,D).
ok(n, D) :- pos(X,Y) & free(X  ,Y-1) & distance(X  ,Y-1,D).
ok(nw,D) :- pos(X,Y) & free(X-1,Y-1) & distance(X-1,Y-1,D).
ok(ne,D) :- pos(X,Y) & free(X+1,Y-1) & distance(X+1,Y-1,D).

free(X,Y) :- X >= 0 & Y >= 0 & not obstacle(X,Y) & w_size(W,H) & X < W & Y < H.

distance(X,Y,D) :- destination(GX,GY) & D = math.sqrt( (X-GX)**2 + (Y-GY)**2).