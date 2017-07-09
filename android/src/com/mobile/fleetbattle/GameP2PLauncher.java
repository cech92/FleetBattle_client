package com.mobile.fleetbattle;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;

import com.badlogic.gdx.backends.android.AndroidFragmentApplication;


public class GameP2PLauncher extends AppCompatActivity implements AndroidFragmentApplication.Callbacks {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.game);

		String enemy;
		Bundle extras = getIntent().getExtras();
		enemy= extras.getString("ENEMY");

		FragmentManager fragmentManager = getSupportFragmentManager();
		FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
		Fragment game;
		if(enemy!=null){
			switch (enemy){
				case "foo" : game = new GameFragmentFoo(); break;
				case "medium" : game = new GameFragmentMedium(); break;
				default : game = new GameFragmentMedium(); break;
			}
		}else{
			game = new GameFragmentMedium();
		}
		fragmentTransaction.add(R.id.fragment_container,game,"gioco");
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
