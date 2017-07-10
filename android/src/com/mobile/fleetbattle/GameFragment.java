package com.mobile.fleetbattle;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.badlogic.gdx.backends.android.AndroidFragmentApplication;

/**
 * Created by Facoch on 20/06/17.
 */

public class GameFragment extends AndroidFragmentApplication {
    Adversary enemy = null;

    public GameFragment() {
        if (Config.getCurrentGameType() == Config.SINGLEEASYGAME)
            enemy = new FooAdversary();
        else if (Config.getCurrentGameType() == Config.SINGLEMEDIUMGAME)
            enemy = new ComputerAdversary();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return initializeForView(new FleetBattleGame(enemy));
    }


}

