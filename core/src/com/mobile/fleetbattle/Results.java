package com.mobile.fleetbattle;

/**
 * Created by Facoch on 27/06/17.
 * Utility class to store the results of an attack: if a ship was it, a Ship if it was sank,
 * and if the attacked player has lost
 */

class Results {
    boolean hit;
    Ship sank;
    boolean lost;

    Results(boolean a, Ship b, boolean c){
        hit=a; sank=b; lost=c;
    }
}
