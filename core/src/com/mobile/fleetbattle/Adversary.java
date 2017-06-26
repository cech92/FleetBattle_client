package com.mobile.fleetbattle;

/**
 * Created by Facoch on 26/06/17.
 */

public abstract class Adversary {

    // should return true if on coordinates x,y there's a non previously hit ship portion.
    public abstract boolean hit(int y,int x);

    //should return the ship id in coordinates x,y there's a fully destroyed ship. else returns a ship of size 0.
    public abstract Ship destroyed(int y, int x);

    //should return true if there are no more unhit ship portions.
    public abstract boolean lost();
}
