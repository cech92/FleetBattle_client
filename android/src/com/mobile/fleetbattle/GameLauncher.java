package com.mobile.fleetbattle;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;

import com.badlogic.gdx.backends.android.AndroidFragmentApplication;


public class GameLauncher extends AppCompatActivity implements AndroidFragmentApplication.Callbacks {
	public static final String TAG = "GameLauncher";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.game);

		FragmentManager fragmentManager = getSupportFragmentManager();
		FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
		Fragment game;
		if(Config.getCurrentGameType() == Config.SINGLEEASYGAME)
			game = new GameFragmentFoo();
		else
			game = new GameFragmentMedium();
		fragmentTransaction.add(R.id.fragment_container, game, "gioco");
		fragmentTransaction.commit();
	}

	@Override
	public void onBackPressed() {
		boolean gameRunning = FleetBattleGame.getRunning();
		if (!gameRunning){
			super.onBackPressed();
		}
	}


	@Override
	public void onStart() {
		super.onStart();
	}


	@Override
	public void onStop() {
		super.onStop();
	}

	@Override
	public void exit() {}
}
