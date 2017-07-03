package com.mobile.fleetbattle;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void goDrop(View v) {
        Intent intent = new Intent(MainActivity.this, GameLauncher.class);
        String enemy = "foo";
        intent.putExtra("ENEMY", enemy);
        startActivity(intent);
    }

    public void startAiGame(View v) {
        Intent intent = new Intent(MainActivity.this, GameLauncher.class);
        String enemy = "medium";
        intent.putExtra("ENEMY", enemy);
        startActivity(intent);
    }

}
