package com.mobile.fleetbattle;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.android.AndroidFragmentApplication;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.GamesStatusCodes;
import com.google.android.gms.games.Player;
import com.google.android.gms.games.multiplayer.Multiplayer;
import com.google.android.gms.games.multiplayer.Participant;
import com.google.android.gms.games.multiplayer.realtime.RealTimeMessage;
import com.google.android.gms.games.multiplayer.realtime.RealTimeMessageReceivedListener;
import com.google.android.gms.games.multiplayer.realtime.Room;
import com.google.android.gms.games.multiplayer.realtime.RoomConfig;
import com.google.android.gms.games.multiplayer.realtime.RoomStatusUpdateListener;
import com.google.android.gms.games.multiplayer.realtime.RoomUpdateListener;
import com.google.example.games.basegameutils.BaseGameUtils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.backends.android.AndroidFragmentApplication;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.Player;
import com.google.example.games.basegameutils.BaseGameUtils;

import java.util.ArrayList;
import java.util.List;

import static com.mobile.fleetbattle.R.layout.game;


public class OnlineGameLauncher extends AppCompatActivity implements AndroidFragmentApplication.Callbacks,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
        RoomStatusUpdateListener, RealTimeMessageReceivedListener, RoomUpdateListener{
    public static final String TAG = "OnlineGameLauncher";

    private static GoogleApiClient mGoogleApiClient;

    private boolean mResolvingConnectionFailure = false;

    // Has the user clicked the sign-in button?
    private boolean mSignInClicked = false;

    // Automatically start the sign-in flow when the Activity starts
    private boolean mAutoStartSignInFlow = true;
    static String mRoomId = null;

    private static final int RC_RESOLVE = 5000;
    private static final int RC_UNUSED = 5001;
    private static final int RC_SIGN_IN = 9001;

    boolean mPlaying = false;

    final static int MIN_PLAYERS = 2;

    static ArrayList<Participant> mParticipants = null;

    String mMyId = null;

    Fragment gameFrag;

    public static GoogleApiClient getmGoogleApiClient() {
        return  mGoogleApiClient;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(game);

        if (Config.getCurrentGameType() == Config.ONLINEGAME) {
            mGoogleApiClient = MainActivity.getMGoogleApiClient();
//            mGoogleApiClient = new GoogleApiClient.Builder(this)
//                    .addConnectionCallbacks(this)
//                    .addOnConnectionFailedListener(this)
//                    .addApi(Games.API).addScope(Games.SCOPE_GAMES)
//                    .setViewForPopups(findViewById(android.R.id.content))
//                    .build();
//
//            mGoogleApiClient.connect();

        }

        System.out.println("CLIENT " + mGoogleApiClient.toString());
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        gameFrag = new OnlineGameFragment();
        fragmentTransaction.add(R.id.fragment_container, gameFrag, "gioco");
        fragmentTransaction.commit();
        System.out.println("1");


        startQuickGame();

//		String enemy;
//		Bundle extras = getIntent().getExtras();
//		enemy= extras.getString("ENEMY");

        System.out.println("2");

    }

    public GoogleApiClient getMGoogleApiClient() {
        return mGoogleApiClient;
    }

    @Override
    public void onBackPressed() {
        boolean gameRunning = FleetBattleGame.getRunning();
        if (!gameRunning){
            super.onBackPressed();
        }
    }

//	@Override
//	public void onResume() {
//		super.onResume();
//		mReceiver = new WiFiDirectBroadcastReceiver(mManager, mChannel, this);
//		registerReceiver(mReceiver, mIntentFilter);
//	}
//
//	@Override
//	public void onPause() {
//		super.onPause();
//		unregisterReceiver(mReceiver);
//	}

    private void startQuickGame() {
        // auto-match criteria to invite one random automatch opponent.
        // You can also specify more opponents (up to 3).
        Bundle am = RoomConfig.createAutoMatchCriteria(1, 1, 0);

        // build the room config:
        RoomConfig.Builder roomConfigBuilder = makeBasicRoomConfigBuilder();
        roomConfigBuilder.setAutoMatchCriteria(am);
        RoomConfig roomConfig = roomConfigBuilder.build();

        // create room:
        Games.RealTimeMultiplayer.create(mGoogleApiClient, roomConfig);

        // prevent screen from sleeping during handshake
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // go to game screen

    }

    private RoomConfig.Builder makeBasicRoomConfigBuilder() {
        return RoomConfig.builder(this)
                .setMessageReceivedListener(this)
                .setRoomStatusUpdateListener(this);
    }


    @Override
    public void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mGoogleApiClient.disconnect();
    }

    @Override
    public void exit() {}

//	public void resetData() {
//		DeviceListFragment fragmentList = (DeviceListFragment) getFragmentManager()
//				.findFragmentById(R.id.frag_list);
//		DeviceDetailFragment fragmentDetails = (DeviceDetailFragment) getFragmentManager()
//				.findFragmentById(R.id.frag_detail);
//		if (fragmentList != null) {
//			fragmentList.clearPeers();
//		}
//		if (fragmentDetails != null) {
//			fragmentDetails.resetViews();
//		}
//	}
//
//	@Override
//	public boolean onCreateOptionsMenu(Menu menu) {
//		MenuInflater inflater = getMenuInflater();
//		inflater.inflate(R.menu.action_items, menu);
//		return true;
//	}
//
//	/*
//     * (non-Javadoc)
//     * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
//     */
//	@Override
//	public boolean onOptionsItemSelected(MenuItem item) {
//		switch (item.getItemId()) {
//			case R.id.atn_direct_enable:
//				if (mManager != null && mChannel != null) {
//
//					// Since this is the system wireless settings activity, it's
//					// not going to send us a result. We will be notified by
//					// WiFiDeviceBroadcastReceiver instead.
//
//					startActivity(new Intent(Settings.ACTION_WIRELESS_SETTINGS));
//				} else {
//					Log.e(TAG, "channel or manager is null");
//				}
//				return true;
//
//			case R.id.atn_direct_discover:
//				if (!isWifiP2pEnabled) {
//					Toast.makeText(GameLauncher.this, R.string.p2p_off_warning,
//							Toast.LENGTH_SHORT).show();
//					return true;
//				}
//				final DeviceListFragment fragment = (DeviceListFragment) getFragmentManager()
//						.findFragmentById(R.id.frag_list);
//				fragment.onInitiateDiscovery();
//				mManager.discoverPeers(mChannel, new WifiP2pManager.ActionListener() {
//
//					@Override
//					public void onSuccess() {
//						Toast.makeText(GameLauncher.this, "Discovery Initiated",
//								Toast.LENGTH_SHORT).show();
//					}
//
//					@Override
//					public void onFailure(int reasonCode) {
//						Toast.makeText(GameLauncher.this, "Discovery Failed : " + reasonCode,
//								Toast.LENGTH_SHORT).show();
//					}
//				});
//				return true;
//			default:
//				return super.onOptionsItemSelected(item);
//		}
//	}
//
//	@Override
//	public void showDetails(WifiP2pDevice device) {
//		DeviceDetailFragment fragment = (DeviceDetailFragment) getFragmentManager()
//				.findFragmentById(R.id.frag_detail);
//		fragment.showDetails(device);
//
//	}
//
//	@Override
//	public void connect(WifiP2pConfig config) {
//		mManager.connect(mChannel, config, new WifiP2pManager.ActionListener() {
//
//			@Override
//			public void onSuccess() {
//				Toast.makeText(GameLauncher.this, "Connect failed. Retry.",
//						Toast.LENGTH_SHORT).show();
//			}
//
//			@Override
//			public void onFailure(int reason) {
//				Toast.makeText(GameLauncher.this, "Connect failed. Retry.",
//						Toast.LENGTH_SHORT).show();
//			}
//		});
//	}
//
//	@Override
//	public void disconnect() {
//		final DeviceDetailFragment fragment = (DeviceDetailFragment) getFragmentManager()
//				.findFragmentById(R.id.frag_detail);
//		fragment.resetViews();
//		mManager.removeGroup(mChannel, new WifiP2pManager.ActionListener() {
//
//			@Override
//			public void onFailure(int reasonCode) {
//				Log.d(TAG, "Disconnect failed. Reason :" + reasonCode);
//
//			}
//
//			@Override
//			public void onSuccess() {
//				fragment.getView().setVisibility(View.GONE);
//			}
//
//		});
//	}
//
//	@Override
//	public void onChannelDisconnected() {
//		// we will try once more
//		if (mManager != null && !retryChannel) {
//			Toast.makeText(this, "Channel lost. Trying again", Toast.LENGTH_LONG).show();
//			resetData();
//			retryChannel = true;
//			mManager.initialize(this, getMainLooper(), this);
//		} else {
//			Toast.makeText(this,
//					"Severe! Channel is probably lost premanently. Try Disable/Re-Enable P2P.",
//					Toast.LENGTH_LONG).show();
//		}
//	}
//
//	@Override
//	public void cancelDisconnect() {
//
//        /*
//         * A cancel abort request by user. Disconnect i.e. removeGroup if
//         * already connected. Else, request WifiP2pManager to abort the ongoing
//         * request
//         */
//		if (mManager != null) {
//			final DeviceListFragment fragment = (DeviceListFragment) getFragmentManager()
//					.findFragmentById(R.id.frag_list);
//			if (fragment.getDevice() == null
//					|| fragment.getDevice().status == WifiP2pDevice.CONNECTED) {
//				disconnect();
//			} else if (fragment.getDevice().status == WifiP2pDevice.AVAILABLE
//					|| fragment.getDevice().status == WifiP2pDevice.INVITED) {
//
//				mManager.cancelConnect(mChannel, new WifiP2pManager.ActionListener() {
//
//					@Override
//					public void onSuccess() {
//						Toast.makeText(GameLauncher.this, "Aborting connection",
//								Toast.LENGTH_SHORT).show();
//					}
//
//					@Override
//					public void onFailure(int reasonCode) {
//						Toast.makeText(GameLauncher.this,
//								"Connect abort request failed. Reason Code: " + reasonCode,
//								Toast.LENGTH_SHORT).show();
//					}
//				});
//			}
//		}
//
//	}

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Toast.makeText(OnlineGameLauncher.this, "Connesso", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast.makeText(OnlineGameLauncher.this, "Non Connesso" + connectionResult.toString(), Toast.LENGTH_LONG).show();

//        if (mResolvingConnectionFailure) {
//            // already resolving
//            return;
//        }
//
//        // if the sign-in button was clicked or if auto sign-in is enabled,
//        // launch the sign-in flow
        if (mSignInClicked || mAutoStartSignInFlow) {
            mAutoStartSignInFlow = false;
            mSignInClicked = false;
            mResolvingConnectionFailure = true;

            // Attempt to resolve the connection failure using BaseGameUtils.
            // The R.string.signin_other_error value should reference a generic
            // error string in your strings.xml file, such as "There was
            // an issue with sign-in, please try again later."
            if (!BaseGameUtils.resolveConnectionFailure(this,
                    mGoogleApiClient, connectionResult,
                    RC_SIGN_IN, R.string.signin_other_error)) {
                mResolvingConnectionFailure = false;
            }
        }
    }

    private void signInClicked() {
        mSignInClicked = true;
        mGoogleApiClient.connect();
    }

    // Call when the sign-out button is clicked
    private void signOutclicked() {
        mSignInClicked = false;
        Games.signOut(mGoogleApiClient);
    }

    @Override
    public void onActivityResult(int request, int response, Intent data) {
        System.out.println(request + " " + response);
//        if (request == RC_SELECT_PLAYERS) {
//            if (response != Activity.RESULT_OK) {
//                // user canceled
//                return;
//            }
//
//            // get the invitee list
//            Bundle extras = data.getExtras();
//            final ArrayList<String> invitees =
//                    data.getStringArrayListExtra(Games.EXTRA_PLAYER_IDS);
//
//            // get auto-match criteria
            Bundle autoMatchCriteria = null;
            int minAutoMatchPlayers =
                    data.getIntExtra(Multiplayer.EXTRA_MIN_AUTOMATCH_PLAYERS, 0);
            int maxAutoMatchPlayers =
                    data.getIntExtra(Multiplayer.EXTRA_MAX_AUTOMATCH_PLAYERS, 0);

            if (minAutoMatchPlayers > 0) {
                autoMatchCriteria = RoomConfig.createAutoMatchCriteria(
                        minAutoMatchPlayers, maxAutoMatchPlayers, 0);
            } else {
                autoMatchCriteria = null;
            }

            // create the room and specify a variant if appropriate
            RoomConfig.Builder roomConfigBuilder = makeBasicRoomConfigBuilder();
            //roomConfigBuilder.addPlayersToInvite(invitees);
            if (autoMatchCriteria != null) {
                roomConfigBuilder.setAutoMatchCriteria(autoMatchCriteria);
            }
            RoomConfig roomConfig = roomConfigBuilder.build();
            Games.RealTimeMultiplayer.create(mGoogleApiClient, roomConfig);

            // prevent screen from sleeping during handshake
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
//        }
    }

    // returns whether there are enough players to start the game
    boolean shouldStartGame(Room room) {
        int connectedPlayers = 0;
        for (Participant p : room.getParticipants()) {
            if (p.isConnectedToRoom()) ++connectedPlayers;
        }
        return connectedPlayers >= MIN_PLAYERS;
    }

    // Returns whether the room is in a state where the game should be canceled.
    boolean shouldCancelGame(Room room) {
        // TODO: Your game-specific cancellation logic here. For example, you might decide to
        // cancel the game if enough people have declined the invitation or left the room.
        // You can check a participant's status with Participant.getStatus().
        // (Also, your UI should have a Cancel button that cancels the game too)
        return true;
    }

    // create a RoomConfigBuilder that's appropriate for your implementation

    @Override
    public void onRoomCreated(int statusCode, Room room) {
        Toast.makeText(getApplicationContext() ,"Room Created", Toast.LENGTH_LONG).show();
        if (statusCode != GamesStatusCodes.STATUS_OK) {
            // let screen go to sleep
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

            // show error message, return to main screen.
        }
    }

    @Override
    public void onJoinedRoom(int statusCode, Room room) {
        Toast.makeText(getApplicationContext() ,"Room Joined", Toast.LENGTH_LONG).show();

        if (statusCode != GamesStatusCodes.STATUS_OK) {
            // let screen go to sleep
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

            // show error message, return to main screen.
        }
    }

    @Override
    public void onLeftRoom(int i, String s) {

    }

    @Override
    public void onRoomConnected(int statusCode, Room room) {
        Toast.makeText(getApplicationContext() ,"Room Connected", Toast.LENGTH_LONG).show();

        if (statusCode != GamesStatusCodes.STATUS_OK) {
            // let screen go to sleep
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

            // show error message, return to main screen.
        }
    }

    @Override
    public void onRealTimeMessageReceived(RealTimeMessage realTimeMessage) {
        final byte[] b = realTimeMessage.getMessageData();

        if ((int)b[0] == 0)
            ((OnlineGameFragment) gameFrag).receiveAttack(b);
        if ((int)b[0] == 1)
            ((OnlineGameFragment) gameFrag).getAttackResponse(b);
    }

    @Override
    public void onRoomConnecting(Room room) {

    }

    @Override
    public void onRoomAutoMatching(Room room) {

    }

    @Override
    public void onPeerInvitedToRoom(Room room, List<String> list) {

    }

    @Override
    public void onPeerJoined(Room room, List<String> list) {

    }

    @Override
    public void onConnectedToRoom(Room room) {
        Log.d(TAG, "onConnectedToRoom.");

        // get room ID, participants and my ID:
        mRoomId = room.getRoomId();
        mParticipants = room.getParticipants();
        //mMyId = room.getParticipantId(getGamesClient().getCurrentPlayerId());

        if (mParticipants.get(0).getPlayer() != null) {
            if (mParticipants.get(0).getDisplayName().equals(mParticipants.get(0).getPlayer().getDisplayName()))
                ((OnlineGameFragment) gameFrag).getOnlineFleetBattleGame().setTurn(1);
        }


        // print out the list of participants (for debug purposes)
        Log.d(TAG, "Room ID: " + mRoomId);
        //Log.d(TAG, "My ID " + mMyId);
        Log.d(TAG, "<< CONNECTED TO ROOM>>");
    }

    @Override
    public void onDisconnectedFromRoom(Room room) {
        // leave the room
        Games.RealTimeMultiplayer.leave(mGoogleApiClient, null, mRoomId);

        // clear the flag that keeps the screen on
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // show error message and return to main screen
    }

    @Override
    public void onPeersConnected(Room room, List<String> peers) {
        if (mPlaying) {
            // add new player to an ongoing game
        } else if (shouldStartGame(room)) {
            // start game!
        }
    }

    @Override
    public void onPeersDisconnected(Room room, List<String> peers) {
        if (mPlaying) {
            // do game-specific handling of this -- remove player's avatar
            // from the screen, etc. If not enough players are left for
            // the game to go on, end the game and leave the room.
        } else if (shouldCancelGame(room)) {
            // cancel the game
            Games.RealTimeMultiplayer.leave(mGoogleApiClient, null, mRoomId);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
    }

    @Override
    public void onPeerLeft(Room room, List<String> peers) {
        // peer left -- see if game should be canceled
        if (!mPlaying && shouldCancelGame(room)) {
            Games.RealTimeMultiplayer.leave(mGoogleApiClient, null, mRoomId);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
    }

    @Override
    public void onPeerDeclined(Room room, List<String> peers) {
        // peer declined invitation -- see if game should be canceled
        if (!mPlaying && shouldCancelGame(room)) {
            Games.RealTimeMultiplayer.leave(mGoogleApiClient, null, mRoomId);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
    }

    @Override
    public void onP2PConnected(String s) {

    }

    @Override
    public void onP2PDisconnected(String s) {

    }


}
