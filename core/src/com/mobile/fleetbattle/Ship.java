package com.mobile.fleetbattle;

/**
 * Created by Facoch on 26/06/17.
 */

public class Ship {
    //coordinates
    int y;
    int x;
    //length
    int size;
    //1 if facing up, 0 if facing right.
    int up= 0;
    public Ship(int a, int b, int c, int dir) {
        y=a; x=b; size=c; up=dir;
    }
}
