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
    P2PFleetBattleGame p2pfbg;

    public OnlineGameFragment() {
        if (Config.getCurrentGameType() == Config.SINGLEEASYGAME)
            enemy = new FooAdversary();
        else if (Config.getCurrentGameType() == Config.SINGLEMEDIUMGAME)
            enemy = new ComputerAdversary();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        System.out.println("GAME TYPE: " + Config.getCurrentGameType());
        if (Config.getCurrentGameType() == Config.ONLINEGAME) {
            ofbg = new OnlineFleetBattleGame();
            return initializeForView(ofbg);
        } else if (Config.getCurrentGameType() == Config.P2PGAME) {
            p2pfbg = new P2PFleetBattleGame(getActivity());
            return initializeForView(p2pfbg);
        }
        return initializeForView(new FleetBattleGame(enemy));
    }

    public OnlineFleetBattleGame getOnlineFleetBattleGame() {
        return ofbg;
    }

    public P2PFleetBattleGame getP2PFleetBattleGame() {
        return p2pfbg;
    }

    public void enableStartButton() {
        ofbg.enableStartButton();
    }

    public void receiveAttack(byte[] b) {
        ofbg.checkAttack(b);
    }

    public void getAttackResponse(byte[] b) {
        ofbg.responseAttack(b);
    }


}

