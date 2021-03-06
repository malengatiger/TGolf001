package com.boha.golfpractice.library.activities;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.TextView;

import com.boha.golfpractice.library.R;
import com.boha.golfpractice.library.adapters.VideoAdapter;
import com.boha.golfpractice.library.dto.PlayerDTO;
import com.boha.golfpractice.library.dto.ResponseDTO;
import com.boha.golfpractice.library.dto.VideoUploadDTO;
import com.boha.golfpractice.library.util.MonLog;
import com.boha.golfpractice.library.util.SharedUtil;
import com.google.android.youtube.player.YouTubeBaseActivity;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerFragment;

import java.util.List;

public class YouTubePlayerActivity extends YouTubeBaseActivity implements YouTubePlayer.OnInitializedListener{

    private YouTubePlayerFragment youTubePlayerFragment;
    private YouTubePlayer youTubePlayer;
    private TextView txtCount;
    private Context ctx;
    private List<VideoUploadDTO> videoList;
    private RecyclerView recycler;
    private VideoAdapter adapter;
    private int position;
    static final String LOG = YouTubePlayerActivity.class.getSimpleName();
    public static final String
            STAFF_DEBUG_API_KEY = "AIzaSyDfG9K7XcN97tl3uQpm1194Xn1eaZT_MLc",
            STAFF_PROD_API_KEY = "AIzaSyCgYFuzzP2Pz2ycziCpgO5mz6YMCy58BoU";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_youtube_player);
        txtCount = (TextView)findViewById(R.id.videoCount);
        ctx = getApplicationContext();

        ResponseDTO r = (ResponseDTO)getIntent().getSerializableExtra("videoList");
        if (r != null) {
            videoList = r.getVideoUploadList();
        }

        youTubePlayerFragment = (YouTubePlayerFragment)getFragmentManager()
                .findFragmentById(R.id.youtubeplayerfragment);

        recycler = (RecyclerView)findViewById(R.id.recycler);
        if (recycler != null) {
            LinearLayoutManager llm = new LinearLayoutManager(getApplicationContext());
            recycler.setLayoutManager(llm);
            recycler.setHasFixedSize(true);
        } else {

        }
        youTubePlayerFragment.initialize(getAPIKey(),this);
        if (savedInstanceState != null) {
            MonLog.e(ctx,LOG,"savedInstanceState is NOT null, getting saved video list");
            r = (ResponseDTO)savedInstanceState.getSerializable("list");
            videoList = r.getVideoUploadList();
            position = savedInstanceState.getInt("position",0);
        }
    }
    private void setList() {
        if (videoList == null || videoList.isEmpty()) {
            return;
        }
        txtCount.setText("" + videoList.size());
        adapter = new VideoAdapter(videoList, getApplicationContext(), new VideoAdapter.VideoListener() {
            @Override
            public void onVideoClicked(VideoUploadDTO videoUpload, int index) {
                youTubePlayer.cueVideo(videoUpload.getYouTubeID());
                position = index;
            }
        });

        recycler.setAdapter(adapter);
        youTubePlayer.cueVideo(videoList.get(position).getYouTubeID());
    }


    @Override
    public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer ytPlayer, boolean wasRestored) {
        MonLog.w(ctx,LOG, "onInitializationSuccess, wasRestored:  " + wasRestored);

        this.youTubePlayer = ytPlayer;
        youTubePlayer.setPlayerStateChangeListener(new MyPlayerStateChangeListener());
        youTubePlayer.setPlaybackEventListener(new MyPlaybackEventListener());

        setList();
        if (!wasRestored) {

        }
    }

    @Override
    public void onInitializationFailure(YouTubePlayer.Provider provider, YouTubeInitializationResult result) {

        MonLog.w(ctx,LOG, "onInitializationFailure: result.isUserRecoverableError: "
                + result.isUserRecoverableError());
        if (result.isUserRecoverableError()) {
            result.getErrorDialog(this,2).show();
            return;
        }
    }

    private String getAPIKey() {
        boolean isDebuggable = 0 != (ctx.getApplicationInfo().flags
                &= ApplicationInfo.FLAG_DEBUGGABLE);

        PlayerDTO staff = SharedUtil.getPlayer(ctx);
        if (staff != null) {
            if (isDebuggable) {
                return STAFF_DEBUG_API_KEY;
            } else {
                return STAFF_PROD_API_KEY;
            }
        }


        return null;
    }

    private final class MyPlayerStateChangeListener implements YouTubePlayer.PlayerStateChangeListener {


        @Override
        public void onAdStarted() {
            MonLog.w(ctx,LOG, "********* onAdStarted ");
        }

        @Override
        public void onError(
                com.google.android.youtube.player.YouTubePlayer.ErrorReason reason) {
            MonLog.w(ctx,LOG, "********* onError: " + reason.toString());
        }

        @Override
        public void onLoaded(String arg0) {
            MonLog.w(ctx,LOG, "********* onLoaded: " + arg0);
        }

        @Override
        public void onLoading() {
            MonLog.w(ctx,LOG, "********* onLoading ");
        }

        @Override
        public void onVideoEnded() {
            MonLog.w(ctx,LOG, "********* onVideoEnded ");
        }

        @Override
        public void onVideoStarted() {
            MonLog.w(ctx,LOG, "********* onVideoStarted ");
        }

    }

    private final class MyPlaybackEventListener implements YouTubePlayer.PlaybackEventListener {


        @Override
        public void onBuffering(boolean arg0) {
            MonLog.w(ctx,LOG, "onBuffering: " + arg0);
        }

        @Override
        public void onPaused() {
            MonLog.w(ctx,LOG, "%%%%%% onPaused ");
        }

        @Override
        public void onPlaying() {
            MonLog.w(ctx,LOG, "%%%%%% onPlaying ");
        }

        @Override
        public void onSeekTo(int arg0) {
            MonLog.w(ctx,LOG, "%%%%%% onSeekTo ");
        }

        @Override
        public void onStopped() {
            MonLog.w(ctx,LOG, "%%%%%% onStopped ");
        }

    }


    @Override
    public void onSaveInstanceState(Bundle b) {
        ResponseDTO w = new ResponseDTO();
        w.setVideoUploadList(videoList);
        b.putSerializable("list",w);
        b.putInt("position",position);
        super.onSaveInstanceState(b);
    }
    @Override
    public void onStop() {
        super.onStop();
        try {
            youTubePlayer.release();
            Log.e(LOG,"youTubePlayer has been released");
        } catch (Exception e) {}
    }
    
}
