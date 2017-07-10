package com.mobile.fleetbattle;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import com.badlogic.gdx.backends.android.AndroidFragmentApplication;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.multiplayer.realtime.RoomConfig;

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

