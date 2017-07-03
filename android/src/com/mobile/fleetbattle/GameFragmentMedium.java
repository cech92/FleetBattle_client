package com.mobile.fleetbattle;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.badlogic.gdx.backends.android.AndroidFragmentApplication;

/**
 * Created by Facoch on 03/07/17.
 */

public class GameFragmentMedium  extends AndroidFragmentApplication {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        Adversary enemy = new ComputerAdversary();
        return initializeForView(new FleetBattleGame(enemy));
    }


}