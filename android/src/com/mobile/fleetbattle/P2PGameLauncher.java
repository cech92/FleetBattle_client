package com.mobile.fleetbattle;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.badlogic.gdx.backends.android.AndroidFragmentApplication;

import static com.mobile.fleetbattle.R.layout.game;


public class P2PGameLauncher extends AppCompatActivity implements AndroidFragmentApplication.Callbacks,
		WifiP2pManager.ChannelListener, DeviceListFragment.DeviceActionListener {
	public static final String TAG = "P2PGameLauncher";
	WifiP2pManager mManager;
	WifiP2pManager.Channel mChannel;
	BroadcastReceiver mReceiver;
	IntentFilter mIntentFilter;
    Fragment gameFrag;
	boolean state = false;
	private boolean retryChannel = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(game);

		mIntentFilter = new IntentFilter();
		mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
		mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
		mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
		mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

		mManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
		mChannel = mManager.initialize(this, getMainLooper(), null);

		FragmentManager fragmentManager = getSupportFragmentManager();
		FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
		Config.setCurrentGameType(Config.P2PGAME);
		gameFrag = new OnlineGameFragment();
		fragmentTransaction.add(R.id.fragment_container, gameFrag, "gioco");
		fragmentTransaction.hide(gameFrag);
		fragmentTransaction.commit();
		//mReceiver = new WiFiDirectBroadcastReceiver(mManager, mChannel, this);

//		final DeviceListFragment fragment = (DeviceListFragment) getFragmentManager()
//				.findFragmentById(R.id.frag_list);
//		fragment.onInitiateDiscovery();
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

//		FragmentManager fragmentManager = getSupportFragmentManager();
//		FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
//		Config.setCurrentGameType(Config.P2PGAME);
//		gameFrag = new OnlineGameFragment();
//		fragmentTransaction.add(R.id.fragment_container, gameFrag, "gioco");
//		fragmentTransaction.commit();


//		if (!state)
//			startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.action_items, menu);
		return true;
	}

	/*
     * (non-Javadoc)
     * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
     */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.atn_direct_enable:
				if (mManager != null && mChannel != null) {

					// Since this is the system wireless settings activity, it's
					// not going to send us a result. We will be notified by
					// WiFiDeviceBroadcastReceiver instead.

					startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
				} else {
					Log.e(TAG, "channel or manager is null");
				}
				return true;

			case R.id.atn_direct_discover:
				if (!state) {
					Toast.makeText(P2PGameLauncher.this, R.string.p2p_off_warning,
							Toast.LENGTH_SHORT).show();
					return true;
				}
				final DeviceListFragment fragment = (DeviceListFragment) getFragmentManager()
						.findFragmentById(R.id.frag_list);
				fragment.onInitiateDiscovery();
				mManager.discoverPeers(mChannel, new WifiP2pManager.ActionListener() {

					@Override
					public void onSuccess() {
						Toast.makeText(P2PGameLauncher.this, "Discovery Initiated",
								Toast.LENGTH_SHORT).show();
					}

					@Override
					public void onFailure(int reasonCode) {
						Toast.makeText(P2PGameLauncher.this, "Discovery Failed : " + reasonCode,
								Toast.LENGTH_SHORT).show();
					}
				});
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		mReceiver = new WiFiDirectBroadcastReceiver(mManager, mChannel, this);
		registerReceiver(mReceiver, mIntentFilter);
	}

	@Override
	protected void onPause() {
		super.onPause();
		unregisterReceiver(mReceiver);
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

	public void setIsWifiP2pEnabled(boolean isWifiP2pEnabled) {
		state = isWifiP2pEnabled;
		System.out.println("P2P Enabled: " + isWifiP2pEnabled);
	}

	public void resetData() {
		DeviceListFragment fragmentList = (DeviceListFragment) getFragmentManager()
				.findFragmentById(R.id.frag_list);
		DeviceDetailFragment fragmentDetails = (DeviceDetailFragment) getFragmentManager()
				.findFragmentById(R.id.frag_detail);
		if (fragmentList != null) {
			fragmentList.clearPeers();
		}
		if (fragmentDetails != null) {
			fragmentDetails.resetViews();
		}
	}

	@Override
	public void showDetails(WifiP2pDevice device) {
		DeviceDetailFragment fragment = (DeviceDetailFragment) getFragmentManager()
				.findFragmentById(R.id.frag_detail);
		fragment.showDetails(device);

	}

	@Override
	public void connect(WifiP2pConfig config) {
		mManager.connect(mChannel, config, new WifiP2pManager.ActionListener() {

			@Override
			public void onSuccess() {
				// WiFiDirectBroadcastReceiver will notify us. Ignore for now.
			}

			@Override
			public void onFailure(int reason) {
				Toast.makeText(P2PGameLauncher.this, "Connect failed. Retry.",
						Toast.LENGTH_SHORT).show();
			}
		});
	}

	@Override
	public void disconnect() {
		final DeviceDetailFragment fragment = (DeviceDetailFragment) getFragmentManager()
				.findFragmentById(R.id.frag_detail);
		fragment.resetViews();
		mManager.removeGroup(mChannel, new WifiP2pManager.ActionListener() {

			@Override
			public void onFailure(int reasonCode) {
				Log.d(TAG, "Disconnect failed. Reason :" + reasonCode);

			}

			@Override
			public void onSuccess() {
				fragment.getView().setVisibility(View.GONE);
			}

		});
	}

	@Override
	public void onChannelDisconnected() {
		// we will try once more
		if (mManager != null && !retryChannel) {
			Toast.makeText(this, "Channel lost. Trying again", Toast.LENGTH_LONG).show();
			resetData();
			retryChannel = true;
			mManager.initialize(this, getMainLooper(), this);
		} else {
			Toast.makeText(this,
					"Severe! Channel is probably lost premanently. Try Disable/Re-Enable P2P.",
					Toast.LENGTH_LONG).show();
		}
	}

	@Override
	public void cancelDisconnect() {

        /*
         * A cancel abort request by user. Disconnect i.e. removeGroup if
         * already connected. Else, request WifiP2pManager to abort the ongoing
         * request
         */
		if (mManager != null) {
			final DeviceListFragment fragment = (DeviceListFragment) getFragmentManager()
					.findFragmentById(R.id.frag_list);
			if (fragment.getDevice() == null
					|| fragment.getDevice().status == WifiP2pDevice.CONNECTED) {
				disconnect();
			} else if (fragment.getDevice().status == WifiP2pDevice.AVAILABLE
					|| fragment.getDevice().status == WifiP2pDevice.INVITED) {

				mManager.cancelConnect(mChannel, new WifiP2pManager.ActionListener() {

					@Override
					public void onSuccess() {
						Toast.makeText(P2PGameLauncher.this, "Aborting connection",
								Toast.LENGTH_SHORT).show();
					}

					@Override
					public void onFailure(int reasonCode) {
						Toast.makeText(P2PGameLauncher.this,
								"Connect abort request failed. Reason Code: " + reasonCode,
								Toast.LENGTH_SHORT).show();
					}
				});
			}
		}

	}

	public void launchGame(int turn) {
		FragmentManager fragmentManager = getSupportFragmentManager();
		FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
		fragmentTransaction.show(gameFrag);
		fragmentTransaction.commit();
		invalidateOptionsMenu();
		((OnlineGameFragment) gameFrag).getP2PFleetBattleGame().setTurn(turn);
	}

	public void receiveAttack(byte[] b) {
		System.out.println("COORDS: " + String.valueOf(b[1]) + String.valueOf(b[2]));
		((OnlineGameFragment) gameFrag).getP2PFleetBattleGame().checkAttack(b);
	}

	public void responseAttack(byte[] b) {
		((OnlineGameFragment) gameFrag).getP2PFleetBattleGame().responseAttack(b);
	}


}
