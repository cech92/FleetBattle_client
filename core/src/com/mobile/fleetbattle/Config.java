package com.mobile.fleetbattle;


public class Config {
    private static int currentGameType;
    public static final int P2PGAME = 0;
    public static final int ONLINEGAME = 1;
    public static final int SINGLEEASYGAME = 2;
    public static final int SINGLEMEDIUMGAME = 3;

    public static void setCurrentGameType(int value) {
        currentGameType = value;
    }

    public static int getCurrentGameType() {
        return currentGameType;
    }
}
