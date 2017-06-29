package com.mobile.fleetbattle;

/**
 * Created by Facoch on 26/06/17.
 * Utility class to store game coordinates (0-9, 0-9) as well as a ship size (1-4, 0 if only
 * coordinates are used or to mean no ship) and if the ship is facing up. This is an integer to
 * simplify graphic computation of the heigth and length of a ship.
 */

class Ship {
    //coordinates
    int y;
    int x;
    //length
    int size;
    //1 if facing up, 0 if facing right.
    int up= 0;
    Ship(int a, int b, int c, int dir) {
        y=a; x=b; size=c; up=dir;
    }
}
