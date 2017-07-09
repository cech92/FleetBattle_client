//package com.mobile.fleetbattle;
//
//import android.content.BroadcastReceiver;
//import android.content.Context;
//import android.content.IntentFilter;
//import android.net.wifi.p2p.WifiP2pManager;
//import android.os.Bundle;
//import android.support.v4.app.Fragment;
//import android.support.v4.app.FragmentManager;
//import android.support.v4.app.FragmentTransaction;
//import android.support.v7.app.AppCompatActivity;
//
//import com.badlogic.gdx.backends.android.AndroidFragmentApplication;
//
//import static com.mobile.fleetbattle.R.layout.game;
//
//
//public class GameP2PLauncher extends AppCompatActivity implements AndroidFragmentApplication.Callbacks {
//	WifiP2pManager mManager;
//	WifiP2pManager.Channel mChannel;
//	BroadcastReceiver mReceiver;
//	IntentFilter mIntentFilter;
//
//	@Override
//	protected void onCreate(Bundle savedInstanceState) {
//		super.onCreate(savedInstanceState);
//		setContentView(game);
//
//		mManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
//		mChannel = mManager.initialize(this, getMainLooper(), null);
//		mReceiver = new WiFiDirectBroadcastReceiver(mManager, mChannel, this);
//
//		mIntentFilter = new IntentFilter();
//		mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
//		mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
//		mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
//		mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
//
//		mManager.discoverPeers(mChannel, new WifiP2pManager.ActionListener() {
//			@Override
//			public void onSuccess() {
//				System.out.println("trovato");
//			}
//
//			@Override
//			public void onFailure(int reasonCode) {
//				System.out.println("non trovato");
//			}
//		});
//
//		FragmentManager fragmentManager = getSupportFragmentManager();
//		FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
//		Config.setCurrentGameType(Config.P2PGAME);
//		Fragment game = new GameFragment();
//		fragmentTransaction.add(R.id.fragment_container, game, "gioco");
//		fragmentTransaction.commit();
//	}
//
//	@Override
//	protected void onResume() {
//		super.onResume();
//		registerReceiver(mReceiver, mIntentFilter);
//	}
//
//	@Override
//	protected void onPause() {
//		super.onPause();
//		unregisterReceiver(mReceiver);
//	}
//
//	@Override
//	public void onBackPressed() {
//		boolean gameRunning = FleetBattleGame.getRunning();
//		if (!gameRunning){
//			super.onBackPressed();
//		}
//	}
//
//
//	@Override
//	public void onStart() {
//		super.onStart();
//	}
//
//
//	@Override
//	public void onStop() {
//		super.onStop();
//	}
//
//	@Override
//	public void exit() {}
//
//
//}
