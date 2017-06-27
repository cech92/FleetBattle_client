package com.mobile.fleetbattle;

/**
 * Created by Facoch on 27/06/17.
 */

public class Results {
    public boolean hit;
    public Ship sank;
    public boolean lost;

    public Results(boolean a, Ship b, boolean c){
        hit=a; sank=b; lost=c;
    }
}
