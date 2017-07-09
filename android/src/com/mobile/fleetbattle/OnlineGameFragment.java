package com.mobile.fleetbattle;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import com.badlogic.gdx.backends.android.AndroidFragmentApplication;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.multiplayer.Participant;
import com.google.android.gms.games.multiplayer.realtime.RoomConfig;


import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import com.badlogic.gdx.backends.android.AndroidFragmentApplication;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.multiplayer.realtime.RoomConfig;

public class OnlineGameFragment extends AndroidFragmentApplication {
    Adversary enemy = null;
    OnlineFleetBattleGame ofbg;

    public OnlineGameFragment() {
        if (Config.getCurrentGameType() == Config.P2PGAME)
            enemy = new P2PAdversary();
        else if (Config.getCurrentGameType() == Config.SINGLEEASYGAME)
            enemy = new FooAdversary();
        else if (Config.getCurrentGameType() == Config.ONLINEGAME)
            enemy = new OnlineAdversary();
        else
            enemy = new ComputerAdversary();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (Config.getCurrentGameType() == Config.ONLINEGAME) {
            ofbg = new OnlineFleetBattleGame(enemy);
            return initializeForView(ofbg);
        }
        return initializeForView(new FleetBattleGame(enemy));
    }

    public OnlineFleetBattleGame getOnlineFleetBattleGame() {
        return ofbg;
    }

    public void receiveAttack(byte[] b) {
        System.out.println("COORDS: " + String.valueOf(b[1]) + String.valueOf(b[2]));
        ofbg.checkAttack(b);
    }

    public void getAttackResponse(byte[] b) {
        ofbg.responseAttack(b);
    }

}

