package com.boha.golfpractice.golfer.app.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import com.boha.golfpractice.golfer.R;
import com.boha.golfpractice.golfer.app.fragments.PlayerListFragment;
import com.boha.golfpractice.library.activities.SessionControllerActivity;
import com.boha.golfpractice.library.activities.MonApp;
import com.boha.golfpractice.library.activities.ProfileActivity;
import com.boha.golfpractice.library.dto.CoachDTO;
import com.boha.golfpractice.library.dto.GolfCourseDTO;
import com.boha.golfpractice.library.dto.HoleDTO;
import com.boha.golfpractice.library.dto.HoleStatDTO;
import com.boha.golfpractice.library.dto.PlayerDTO;
import com.boha.golfpractice.library.dto.PracticeSessionDTO;
import com.boha.golfpractice.library.dto.RequestDTO;
import com.boha.golfpractice.library.dto.ResponseDTO;
import com.boha.golfpractice.library.fragments.CoachSummaryFragment;
import com.boha.golfpractice.library.fragments.GolfCourseListFragment;
import com.boha.golfpractice.library.fragments.PageFragment;
import com.boha.golfpractice.library.util.DepthPageTransformer;
import com.boha.golfpractice.library.util.MonLog;
import com.boha.golfpractice.library.util.OKHttpException;
import com.boha.golfpractice.library.util.OKUtil;
import com.boha.golfpractice.library.util.SharedUtil;
import com.boha.golfpractice.library.util.SnappyGeneral;
import com.boha.golfpractice.library.util.SnappyGolfCourse;
import com.boha.golfpractice.library.util.SnappyPractice;
import com.boha.golfpractice.library.util.Util;
import com.boha.golfpractice.library.util.WebCheck;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CoachMainActivity extends AppCompatActivity
        implements PlayerListFragment.PlayerListener, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener,
        GolfCourseListFragment.GolfCourseListListener, CoachSummaryFragment.CoachSummaryListener {

    GoogleApiClient mGoogleApiClient;
    LocationRequest mLocationRequest;
    Location mCurrentLocation;
    View appBar;
    ImageView hero;
    ViewPager viewPager;
    CollapsingToolbarLayout collapsingToolbarLayout;
    List<PageFragment> pageFragmentList;
    List<PlayerDTO> playerList;
    PlayerListFragment playerListFragment;
    CoachSummaryFragment coachSummaryFragment;
    CoachPagerAdapter adapter;
    int currentPageIndex;
    CoachDTO coach;
    GolfCourseListFragment golfCourseListFragment;
    static final int ADD_PLAYER = 677, UPDATE_PROFILE = 265;
    static final String LOG = CoachMainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_coach_main);

        collapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(
                R.id.collapsingToolbarLayout);
        viewPager = (ViewPager) findViewById(R.id.viewpager);
        appBar = findViewById(R.id.appbar);
        hero = (ImageView) findViewById(R.id.image);
        collapsingToolbarLayout.setTitle("Golf Coach");

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();


        coach = SharedUtil.getCoach(getApplicationContext());
        Drawable d = ContextCompat.getDrawable(getApplicationContext(), R.drawable.golfball48);
        Util.setCustomActionBar(getApplicationContext(), getSupportActionBar(), "Coach's Corner", coach.getFullName(), d);

    }

    @Override
    public void onActivityResult(int reqCode, int rescode, Intent data) {
        switch (reqCode) {
            case ADD_PLAYER:
                if (rescode == RESULT_OK) {
                    playerListFragment.refreshList();
                }
                break;
            case UPDATE_PROFILE:
                if (rescode == RESULT_OK) {

                }
                break;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        hero.setImageDrawable(Util.getRandomBackgroundImage(getApplicationContext()));
        getCachedPlayers();
    }

    private void buildPages() {
        pageFragmentList = new ArrayList<>();

        playerListFragment = PlayerListFragment.newInstance(playerList);
        coachSummaryFragment = new CoachSummaryFragment();
        golfCourseListFragment = GolfCourseListFragment.newInstance(golfCourseList);


        pageFragmentList.add(playerListFragment);
        pageFragmentList.add(coachSummaryFragment);
        pageFragmentList.add(golfCourseListFragment);

        adapter = new CoachPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(adapter);
        viewPager.setPageTransformer(true, new DepthPageTransformer());
        viewPager.setCurrentItem(currentPageIndex, true);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                currentPageIndex = position;
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });


    }

    private void getCachedPlayers() {
        SnappyGeneral.getLookups((MonApp) getApplication(), new SnappyGeneral.DBReadListener() {
            @Override
            public void onDataRead(ResponseDTO response) {
                playerList = response.getPlayerList();
                if (playerList.isEmpty()) {
                    getRemoteCoachData();
                    return;
                }
                getCachedCourses();
            }

            @Override
            public void onError(String message) {

            }
        });
    }

    private void getRemoteCoachData() {

        RequestDTO w = new RequestDTO(RequestDTO.GET_COACH_DATA);
        w.setCoachID(SharedUtil.getCoach(getApplicationContext()).getCoachID());
        w.setZipResponse(true);
        setRefreshActionButtonState(true);
        snackbar = Snackbar.make(appBar, "Refreshing your data, please chill a lttle ...", Snackbar.LENGTH_INDEFINITE);
        OKUtil util = new OKUtil();
        snackbar.show();
        try {
            util.sendGETRequest(getApplicationContext(), w, this,
                    new OKUtil.OKListener() {
                        @Override
                        public void onResponse(ResponseDTO r) {
                            setRefreshActionButtonState(false);
                            snackbar.dismiss();
                            playerList = r.getPlayerList();
                            buildPages();
                            startLocationUpdates();
                            SnappyGeneral.addPlayers((MonApp) getApplication(),
                                    playerList, new SnappyGeneral.DBWriteListener() {
                                        @Override
                                        public void onDataWritten() {

                                        }

                                        @Override
                                        public void onError(String message) {

                                        }
                                    });

                        }

                        @Override
                        public void onError(String message) {
                            setRefreshActionButtonState(false);
                            snackbar.dismiss();
                            Util.showErrorToast(getApplicationContext(), message);
                        }
                    });
        } catch (OKHttpException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onPlayerClicked(PlayerDTO player) {

    }

    @Override
    public void onPlayerProfileUpdate(PlayerDTO player) {
        Intent m = new Intent(getApplicationContext(), ProfileActivity.class);
        m.putExtra("player", player);
        startActivityForResult(m, UPDATE_PROFILE);
    }

    @Override
    public void onVideoSession(PlayerDTO player) {
        Util.showToast(getApplicationContext(),"Video session under construction");
    }

    @Override
    public void onNewPlayerRequired() {
        Intent m = new Intent(getApplicationContext(), ProfileActivity.class);
        PlayerDTO p = new PlayerDTO();
        m.putExtra("player", p);
        startActivityForResult(m, ADD_PLAYER);
    }

    @Override
    public void onGolfCourseClicked(GolfCourseDTO course) {
        MonLog.d(getApplicationContext(), LOG, "####### onGolfCourseClicked");
    }

    @Override
    public void onCourseSearchRequired() {
        startLocationUpdates();
    }

    PracticeSessionDTO practiceSession;

    @Override
    public void onStartSession(GolfCourseDTO c) {
        MonLog.d(getApplicationContext(),LOG,"++++++ onStartSession: "
                + c.getGolfCourseName());
        practiceSession = new PracticeSessionDTO();
        practiceSession.setGolfCourseID(c.getGolfCourseID());
        practiceSession.setGolfCourseName(c.getGolfCourseName());
        practiceSession.setGolfCourse(c);
        practiceSession.setCoachID(SharedUtil.getCoach(getApplicationContext()).getCoachID());
        practiceSession.setSessionDate(new Date().getTime());
        for (HoleDTO hole : c.getHoleList()) {
            HoleStatDTO m = new HoleStatDTO();
            m.setHole(hole);
            practiceSession.getHoleStatList().add(m);
        }
        sendPracticeSession(practiceSession);
    }

    @Override
    public void onGetSessions(GolfCourseDTO course) {
        Util.showToast(getApplicationContext(),"Under Construction");
    }

    boolean directionsRequired;
    GolfCourseDTO golfCourse;

    @Override
    public void onGetDirections(GolfCourseDTO course) {
        directionsRequired = true;
        golfCourse = course;
        snackbar = Snackbar.make(hero, "Confirming device location before starting directions", Snackbar.LENGTH_INDEFINITE);
        snackbar.show();
        startLocationUpdates();
    }

    private void sendPracticeSession(PracticeSessionDTO s) {

        snackbar = Snackbar.make(hero, "Sending data for Practice Session, hang on a sec", Snackbar.LENGTH_INDEFINITE);

        RequestDTO w = new RequestDTO(RequestDTO.ADD_PRACTICE_SESSION);
        w.setPracticeSession(s);
        w.setZipResponse(false);

        if (WebCheck.checkNetworkAvailability(getApplicationContext()).isNetworkUnavailable()) {
            cacheSession(practiceSession);
            startHoleStat();
            return;
        }
        snackbar.show();
        setRefreshActionButtonState(true);
        OKUtil util = new OKUtil();
        setRefreshActionButtonState(true);
        try {
            util.sendPOSTRequest(this, w, this, new OKUtil.OKListener() {
                @Override
                public void onResponse(final ResponseDTO response) {
                    setRefreshActionButtonState(false);
                    snackbar.dismiss();
                    practiceSession = response.getPracticeSessionList().get(0);
                    cacheSession(practiceSession);
                    startHoleStat();
                }

                @Override
                public void onError(String message) {
                    setRefreshActionButtonState(false);
                    snackbar.dismiss();
                    cacheSession(practiceSession);
                    startHoleStat();

                }
            });
        } catch (OKHttpException e) {
            e.printStackTrace();
        }
    }

    static final int EDIT_HOLE_STATS_REQUIRED = 1765;

    private void startHoleStat() {
        Intent m = new Intent(getApplicationContext(), SessionControllerActivity.class);
        m.putExtra("session", practiceSession);
        startActivityForResult(m, EDIT_HOLE_STATS_REQUIRED);
    }

    private void cacheSession(PracticeSessionDTO practiceSession) {
        this.practiceSession = practiceSession;
        SnappyPractice.addCurrentPracticeSession((MonApp) getApplication(),
                practiceSession, new SnappyPractice.DBWriteListener() {
                    @Override
                    public void onDataWritten() {
                        onBackPressed();
                    }

                    @Override
                    public void onError(String message) {

                    }
                });
    }

    @Override
    public void setBusy(boolean busy) {
        setRefreshActionButtonState(busy);
    }

    private class CoachPagerAdapter extends FragmentStatePagerAdapter {

        public CoachPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public android.support.v4.app.Fragment getItem(int i) {

            return (android.support.v4.app.Fragment) pageFragmentList.get(i);
        }

        @Override
        public int getCount() {
            return pageFragmentList.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            PageFragment pf = pageFragmentList.get(position);
            return pf.getPageTitle();
        }
    }

    Menu mMenu;
    Snackbar snackbar;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.coach_main, menu);
        mMenu = menu;
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_refresh) {
            getRemoteCoachData();
            return true;
        }
        if (id == R.id.action_help) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void setRefreshActionButtonState(final boolean refreshing) {
        if (mMenu != null) {
            final MenuItem refreshItem = mMenu.findItem(com.boha.golfpractice.library.R.id.action_refresh);
            if (refreshItem != null) {
                if (refreshing) {
                    refreshItem.setActionView(com.boha.golfpractice.library.R.layout.action_bar_progess);
                } else {
                    refreshItem.setActionView(null);
                }
            }
        }
    }

    List<GolfCourseDTO> golfCourseList;

    private void getCachedCourses() {
        SnappyGolfCourse.getFavouriteGolfCourses((MonApp) getApplication(), new SnappyGolfCourse.DBReadListener() {
            @Override
            public void onDataRead(ResponseDTO response) {
                if (!response.getGolfCourseList().isEmpty()) {
                    golfCourseList = response.getGolfCourseList();
                    buildPages();

                }
            }

            @Override
            public void onError(String message) {

            }
        });
    }

    private void getCourses() {
        MonLog.w(getApplicationContext(), LOG, "============== getCourses .............");
        RequestDTO w = new RequestDTO(RequestDTO.GET_GOLF_COURSES_BY_LOCATION);
        w.setLatitude(mCurrentLocation.getLatitude());
        w.setLongitude(mCurrentLocation.getLongitude());
        w.setRadius(50);
        w.setZipResponse(true);
        setRefreshActionButtonState(true);
        snackbar.show();
        OKUtil okUtil = new OKUtil();
        try {
            okUtil.sendGETRequest(getApplicationContext(), w, this, new OKUtil.OKListener() {
                @Override
                public void onResponse(ResponseDTO response) {
                    setRefreshActionButtonState(false);
                    snackbar.dismiss();
                    MonLog.i(getApplicationContext(), LOG, "Golf Courses found: " + response.getGolfCourseList().size());
                    golfCourseList = response.getGolfCourseList();
                    buildPages();
                    SnappyGolfCourse.addFavoriteGolfCourses((MonApp) getApplication(), golfCourseList, null);
                }

                @Override
                public void onError(String message) {
                    setRefreshActionButtonState(false);
                    snackbar.dismiss();
                    if (golfCourseList == null || golfCourseList.isEmpty()) {
                        Util.showErrorToast(getApplicationContext(), "No golf courses found. There may be a problem with the server");
                    }
                }
            });
        } catch (OKHttpException e) {
            e.printStackTrace();
        }
    }

    public void onStart() {
        MonLog.d(getApplicationContext(), LOG,
                "##******************* onStart - GoogleApiClient connecting ... ");
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }

        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mGoogleApiClient != null) {
            mGoogleApiClient.disconnect();
            MonLog.e(getApplicationContext(), LOG, "### onStop - locationClient disconnecting ");
        }

    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {
        MonLog.e(getApplicationContext(), LOG,
                "+++  GoogleApiClient onConnected() ...");
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            Log.e(LOG, "Manifest.permission.ACCESS_FINE_LOCATION, is a problemo, returning ...");
            return;
        }
        mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);

        mLocationRequest = LocationRequest.create();
        mLocationRequest.setInterval(2000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setFastestInterval(1000);
        startLocationUpdates();
    }

    protected void startLocationUpdates() {
        snackbar = Snackbar.make(appBar, "Getting GPS location so we can find golf courses around you", Snackbar.LENGTH_INDEFINITE);
        MonLog.d(getApplicationContext(), LOG, "### startLocationUpdates ....");
        if (mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.requestLocationUpdates(
                    mGoogleApiClient, mLocationRequest, this);


            MonLog.d(getApplicationContext(), LOG, "## GoogleApiClient connected, requesting location updates ...");
        } else {
            MonLog.e(getApplicationContext(), LOG, "------- GoogleApiClient is NOT connected, not sure where we are...");
            mGoogleApiClient.connect();

        }
    }


    protected void stopLocationUpdates() {

        if (mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(
                    mGoogleApiClient, this);
            MonLog.w(getApplicationContext(), LOG, "### stopLocationUpdates ...removeLocationUpdates fired");
        } else {
            MonLog.e(getApplicationContext(), LOG, "##################### stopLocationUpdates googleApiClient is NULL ...");
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    static final float ACCURACY_THRESHOLD = 50;

    @Override
    public void onLocationChanged(Location location) {
        MonLog.d(getApplicationContext(), LOG, "------ onLocationChanged " + location.getLatitude()
                + " " + location.getLongitude() + " " + location.getAccuracy());

        if (location.getAccuracy() <= ACCURACY_THRESHOLD) {
            mCurrentLocation = location;
            stopLocationUpdates();
            if (snackbar != null)
                snackbar.dismiss();
            if (directionsRequired) {
                directionsRequired = false;
                Util.startDirections(getApplicationContext(),
                        mCurrentLocation.getLatitude(),
                        mCurrentLocation.getLongitude(),
                        golfCourse.getLatitude(),
                        golfCourse.getLongitude());

            } else {
                getCourses();
            }
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onBackPressed() {
        MonLog.w(getApplicationContext(), LOG, "---------- onBackPressed");

    }

}
