package com.mobile.fleetbattle;

import java.util.concurrent.Future;

/**
 * Created by cech92 on 05/07/17.
 */

public class OnlineAdversary extends Adversary{
    @Override
    public Future<Results> attack(int y, int x) {
        return null;
    }

    @Override
    public Future<Ship> getAttack() {
        return null;
    }

    @Override
    public void giveResults(Results res) {

    }
}
