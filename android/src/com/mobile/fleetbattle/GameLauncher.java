package com.mobile.fleetbattle;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.badlogic.gdx.backends.android.AndroidFragmentApplication;


public class GameLauncher extends AppCompatActivity implements AndroidFragmentApplication.Callbacks {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.game);
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
