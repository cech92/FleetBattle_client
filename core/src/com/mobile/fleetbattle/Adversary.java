package com.mobile.fleetbattle;

import java.util.concurrent.Future;

/**
 * Created by Facoch on 26/06/17.
 * Abstract class to implement different kinds of Adversary (AI, remote, local...)
 */

abstract class Adversary {


    //used to attack the adversary
    public abstract Future<Results> attack(final int y, final int x);

    //returns the coordinates attacked by the enemy:  ship is used only for the y, x coordinates (size and up should always be 0)
    public abstract Future<Ship> getAttack();

    //used to respond to an adversary attack.
    public abstract void giveResults(Results res);
    //Should this be a future as well? probably not.
}
