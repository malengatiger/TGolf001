package com.boha.golfpractice.library.activities;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.FrameLayout;

import com.boha.golfpractice.library.R;
import com.boha.golfpractice.library.dto.CoachDTO;
import com.boha.golfpractice.library.dto.PlayerDTO;
import com.boha.golfpractice.library.fragments.ProfileFragment;
import com.boha.golfpractice.library.util.MonLog;
import com.boha.golfpractice.library.util.Util;

public class ProfileActivity extends AppCompatActivity implements ProfileFragment.ProfileListener {

    FrameLayout frameLayout;
    CoachDTO coach;
    ProfileFragment profileFragment;
    static final String LOG = ProfileActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        frameLayout = (FrameLayout) findViewById(R.id.frameLayout);
        coach = (CoachDTO) getIntent().getSerializableExtra("coach");
        player = (PlayerDTO) getIntent().getSerializableExtra("player");

        setFragment();
        Drawable draw = ContextCompat.getDrawable(getApplicationContext(), R.drawable.golfball48);
        Util.setCustomActionBar(getApplicationContext(),
                getSupportActionBar(), "Profile", "Do this right", draw);
    }

    private void setFragment() {
        if (coach != null) {
            profileFragment = ProfileFragment.newInstance(coach);
        }
        if (player != null) {
            profileFragment = ProfileFragment.newInstance(player);
        }
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();

        ft.setCustomAnimations(R.anim.enter_from_left, R.anim.exit_to_right);
        profileFragment.setListener(this);

        ft.add(R.id.frameLayout, profileFragment);
        ft.commit();
    }

    @Override
    public void setBusy(boolean busy) {
        setRefreshActionButtonState(busy);
    }

    @Override
    public void onPictureRequested(CoachDTO coach) {
        Intent m = new Intent(getApplicationContext(), PictureActivity.class);
        m.putExtra("coach", coach);
        startActivityForResult(m, COACH_PICTURE);
    }

    @Override
    public void onPictureRequested(PlayerDTO player) {
        Intent m = new Intent(getApplicationContext(), PictureActivity.class);
        m.putExtra("player", player);
        startActivityForResult(m, PLAYER_PICTURE);
    }

    PlayerDTO player;
    boolean playerAdded;

    @Override
    public void onPlayerAddedToServer(PlayerDTO player) {
        this.player = player;
        playerAdded = true;
    }

    @Override
    public void onBackPressed() {

        if (playerAdded) {
            Intent m = new Intent();
            m.putExtra("playerAdded",playerAdded);
            setResult(RESULT_OK,m);
        }
        finish();
    }

    @Override
    public void onActivityResult(int reqCode, int resCode, Intent data) {
        switch (reqCode) {
            case PLAYER_PICTURE:
                MonLog.d(getApplicationContext(), LOG, "**** onActivityResult PLAYER");
                if (resCode == RESULT_OK) {
                    player = (PlayerDTO) data.getSerializableExtra("player");
                    profileFragment.getImage(player.getPhotoUrl());
                }
                break;
            case COACH_PICTURE:
                MonLog.d(getApplicationContext(), LOG, "**** onActivityResult COACH");
                if (resCode == RESULT_OK) {
                    coach = (CoachDTO) data.getSerializableExtra("coach");
                    profileFragment.getImage(coach.getPhotoUrl());
                }
                break;
        }
    }

    static final int COACH_PICTURE = 172, PLAYER_PICTURE = 243;
    Menu mMenu;
    Snackbar snackbar;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.profile, menu);
        mMenu = menu;
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_add) {
            setRefreshActionButtonState(true);
            clearFields();
            return true;
        }
        if (id == R.id.action_help) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void setRefreshActionButtonState(final boolean refreshing) {
        if (mMenu != null) {
            final MenuItem refreshItem = mMenu.findItem(R.id.action_add);
            if (refreshItem != null) {
                if (refreshing) {
                    refreshItem.setActionView(com.boha.golfpractice.library.R.layout.action_bar_progess);
                } else {
                    refreshItem.setActionView(null);
                }
            }
        }
    }

    private void clearFields() {
        profileFragment.clearFields();
    }
}
