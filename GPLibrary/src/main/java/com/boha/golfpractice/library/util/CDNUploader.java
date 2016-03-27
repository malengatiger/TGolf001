package com.boha.golfpractice.library.util;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.boha.golfpractice.library.dto.CoachDTO;
import com.boha.golfpractice.library.dto.PlayerDTO;
import com.boha.golfpractice.library.dto.RequestDTO;
import com.boha.golfpractice.library.dto.ResponseDTO;
import com.cloudinary.Cloudinary;

import org.acra.ACRA;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * This class manages the image uploadToYouTube to the Cloudinary CDN.
 * The uploadToYouTube runs in a Thread and returns its response via CDNUploaderListener
 * <p/>
 * Created by aubreyM on 15/06/08.
 */
public class CDNUploader {
    public interface CDNUploaderListener {
        void onFileUploaded(PlayerDTO player);
        void onFileUploaded(CoachDTO coach);

        void onError(String message);
    }

    static CDNUploaderListener mListener;
    static final String LOG = CDNUploader.class.getSimpleName();
    static Context ctx;
    static Activity activity;

    public static final String
            API_KEY = "397571984789619",
            API_SECRET = "2RBq1clEHC5X_0eQlNP-K3yhA8U",
            CLOUD_NAME = "bohatmx";

    /**
     * Upload photo to CDN (Cloudinary at this time). On return of the CDN response, a call is made
     * to the backend to add the metadata of the photo to the backend database
     *
          */
    public static void uploadFile(final Context context,
                                  final PlayerDTO dto,
                                  final Activity a,
                                  CDNUploaderListener uploaderListener) {
        mListener = uploaderListener;
        ctx = context;
        activity = a;
        Log.d(LOG, "##### starting CDNUploader Player uploadFile: " + dto.getFilePath());
        CDNTask task = new CDNTask(dto);
        task.execute();
    }
    public static void uploadFile(final Context context,
                                  final CoachDTO dto,
                                  final Activity a,
                                  CDNUploaderListener uploaderListener) {
        mListener = uploaderListener;
        ctx = context;
        activity = a;
        Log.d(LOG, "##### starting CDNUploader Coach uploadFile: " + dto.getFilePath());
        CDNTask task = new CDNTask(dto,true);
        task.execute();
    }

    static class CDNTask extends AsyncTask<Void, Void, ResponseDTO> {
        private  PlayerDTO player;
        private CoachDTO coach;
        public CDNTask(PlayerDTO player) {
            this.player = player;
        }
        public CDNTask(CoachDTO coach, boolean isNothing) {
            this.coach = coach;
        }
        @Override
        protected ResponseDTO doInBackground(Void... params) {
            final long start = System.currentTimeMillis();
            final ResponseDTO resp = new ResponseDTO();
            Map config = new HashMap();
            config.put("cloud_name", CLOUD_NAME);
            config.put("api_key", API_KEY);
            config.put("api_secret", API_SECRET);

            Cloudinary cloudinary = new Cloudinary(config);
            File file = null;
            if (player != null) {
                 file = new File(player.getFilePath());
            }
            if (coach != null) {
                file = new File(coach.getFilePath());
            }
            Map map;
            try {
                map = cloudinary.uploader().upload(file, config);
                long end = System.currentTimeMillis();
                Log.i(LOG, "----> photo uploaded: " + map.get("url") + " elapsed: "
                        + Util.getElapsed(start, end) + " seconds");

                if (player != null) {
                    player.setPhotoUrl((String) map.get("secure_url"));
                    resp.getPlayerList().add(player);
                }
                if (coach != null) {
                    coach.setPhotoUrl((String) map.get("secure_url"));
                    resp.getCoachList().add(coach);
                }
                OKUtil util = new OKUtil();
                RequestDTO w = new RequestDTO(RequestDTO.EDIT_PLAYER);
                if (coach != null) {
                    w.setRequestType(RequestDTO.EDIT_COACH);
                    w.setCoach(coach);
                } else {
                    w.setPlayer(player);
                }
                util.sendGETRequest(ctx, w, activity, new OKUtil.OKListener() {
                    @Override
                    public void onResponse(ResponseDTO response) {
                        resp.setCoachList(response.getCoachList());
                        resp.setPlayerList(response.getPlayerList());
                    }

                    @Override
                    public void onError(String message) {

                    }
                });


            } catch (Exception e) {
                Log.e(LOG, "CDN uploadToYouTube Failed", e);
                resp.setStatusCode(9);
                resp.setMessage("Failed to upload picture");
                try {
                    ACRA.getErrorReporter().handleException(e, false);
                } catch (Exception ex) {//ignore}
                }
            }
            return resp;
        }

        @Override
        protected void onPostExecute(final ResponseDTO dto) {
            if (dto == null || dto.getStatusCode() != 0) {
                mListener.onError("Error uploading image to CDN");
                return;
            }
            if (!dto.getCoachList().isEmpty()) {
                mListener.onFileUploaded(dto.getCoachList().get(0));
            }
            if (!dto.getPlayerList().isEmpty()) {
                mListener.onFileUploaded(dto.getPlayerList().get(0));
            }

        }
    }
}
