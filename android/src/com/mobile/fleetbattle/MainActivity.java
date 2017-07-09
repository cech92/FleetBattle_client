package com.mobile.fleetbattle;

import android.content.Intent;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.games.Games;
import com.google.example.games.basegameutils.BaseGameUtils;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private static GoogleApiClient mGoogleApiClient;
    boolean mExplicitSignOut = false;
    boolean mInSignInFlow = false;

    private boolean mResolvingConnectionFailure = false;

    // Has the user clicked the sign-in button?
    private boolean mSignInClicked = false;

    // Automatically start the sign-in flow when the Activity starts
    private boolean mAutoStartSignInFlow = true;
    String mRoomId = null;

    private static final int RC_RESOLVE = 5000;
    private static final int RC_UNUSED = 5001;
    private static final int RC_SIGN_IN = 9001;

    public static GoogleApiClient getMGoogleApiClient() {
        return mGoogleApiClient;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Games.API).addScope(Games.SCOPE_GAMES)
                .setViewForPopups(findViewById(android.R.id.content))
                .build();

        mGoogleApiClient.connect();
    }



    public void goDrop(View v) {
        Config.setCurrentGameType(Config.SINGLEEASYGAME);
        Intent intent = new Intent(MainActivity.this, GameLauncher.class);
//        String enemy = "foo";
//        intent.putExtra("ENEMY", enemy);
        startActivity(intent);
    }

    public GoogleApiClient getGoogleApiClient() {
        return mGoogleApiClient;
    }

    public void startAiGame(View v) {
        Config.setCurrentGameType(Config.SINGLEMEDIUMGAME);
        Intent intent = new Intent(MainActivity.this, GameLauncher.class);
//        String enemy = "medium";
//        intent.putExtra("ENEMY", enemy);
        startActivity(intent);
    }

    public void startP2PGame(View v) {
        Config.setCurrentGameType(Config.P2PGAME);
        Intent intent = new Intent(MainActivity.this, GameLauncher.class);
        startActivity(intent);
    }

    public void startOnlineGame(View v) {
        Config.setCurrentGameType(Config.ONLINEGAME);
//        mGoogleApiClient = new GoogleApiClient.Builder(this)
//                .addConnectionCallbacks(this)
//                .addOnConnectionFailedListener(this)
//                .addApi(Games.API).addScope(Games.SCOPE_GAMES)
//                .setViewForPopups(findViewById(android.R.id.content))
//                .build();
//
//        mGoogleApiClient.connect();
        Intent intent = new Intent(MainActivity.this, OnlineGameLauncher.class);
        startActivity(intent);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!mInSignInFlow && !mExplicitSignOut) {
            // auto sign in
            mGoogleApiClient.connect();
        }
    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Toast.makeText(MainActivity.this, "Connesso", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast.makeText(MainActivity.this, "Non Connesso" + connectionResult.toString(), Toast.LENGTH_LONG).show();

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
}
