package com.boha.golfpractice.library.activities;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import com.boha.golfpractice.library.R;
import com.boha.golfpractice.library.dto.CoachDTO;
import com.boha.golfpractice.library.dto.PlayerDTO;
import com.boha.golfpractice.library.util.CDNUploader;
import com.boha.golfpractice.library.util.ImageUtil;
import com.boha.golfpractice.library.util.ScalingUtilities;
import com.boha.golfpractice.library.util.Util;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;

public class PictureActivity extends AppCompatActivity {

    FloatingActionButton fab;
    CoachDTO coach;
    PlayerDTO player;
    File photoFile, currentThumbFile;
    Context ctx;
    ImageView backDrop;
    static final String LOG = PictureActivity.class.getSimpleName();
    static final int COACH_PICTURE = 152, PLAYER_PICTURE = 987;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_picture);
        ctx = getApplicationContext();
        coach = (CoachDTO)getIntent().getSerializableExtra("coach");
        player = (PlayerDTO)getIntent().getSerializableExtra("player");


        fab = (FloatingActionButton) findViewById(R.id.fab);
        backDrop = (ImageView)findViewById(R.id.backDrop);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dispatchTakePictureIntent();
            }
        });
        String name = "";
        if (player != null) {
            name = player.getFullName();
            if (name.isEmpty()) {
                name = player.getEmail();
            }
        }
        if (coach != null) {
            name = coach.getFullName();
            if (name.isEmpty()) {
                name = coach.getEmail();
            }
        }
        Drawable draw = ContextCompat.getDrawable(getApplicationContext(),R.drawable.golfball48);
        Util.setCustomActionBar(getApplicationContext(),
                getSupportActionBar(),"Profile Picture",name,draw);
    }
    @Override
    public void onActivityResult(final int requestCode, final int resultCode,
                                 final Intent data) {
        Log.e(LOG, "##### onActivityResult requestCode: " + requestCode
                + " resultCode: " + resultCode);

        switch (requestCode) {
            case COACH_PICTURE:
                if (resultCode == RESULT_OK) {
                    new PhotoTask().execute();
                }
                break;
            case PLAYER_PICTURE:
                if (resultCode == RESULT_OK) {
                    new PhotoTask().execute();
                }
                break;
        }
    }


    /**
     * Start default on-board camera app
     */
    private void dispatchTakePictureIntent() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            Log.e(LOG, "WRITE_EXTERNAL_STORAGE permission not granted yet");
            return;
        }

        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            photoFile = null;
            try {
                createPhotoFile();
                if (photoFile != null) {
                    Log.w(LOG, "photoFile created: " + photoFile.getAbsolutePath());
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                            Uri.fromFile(photoFile));
                    if (player != null)
                    startActivityForResult(takePictureIntent, PLAYER_PICTURE);
                    if (coach != null)
                        startActivityForResult(takePictureIntent,COACH_PICTURE);
                } else {
                    Util.showErrorToast(ctx, "Unable to start camera");
                }

            } catch (IOException ex) {
                Log.e(LOG, "Fuck!", ex);
                Util.showErrorToast(ctx, "Unable to create photo file");
            }
        }
    }
    private void createPhotoFile() throws IOException {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            Log.d(LOG, "WRITE_EXTERNAL_STORAGE permission not granted yet");
            return;
        }

        String imageFileName = "photoFile";

        File root;
        if (Util.hasStorage(true)) {
            Log.i(LOG, "###### get file from getExternalStoragePublicDirectory");
            root = Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_PICTURES);
        } else {
            Log.i(LOG, "###### get file from getDataDirectory");
            root = Environment.getDataDirectory();
        }
        File pics = new File(root, "monitor_app");
        if (!pics.exists()) {
            boolean isOK = pics.mkdir();
            if (!isOK) {
                Util.showErrorToast(this, "Unable to get file storage for picture");
                return;
            }
        }

        photoFile = new File(pics, "photoFile.jpg");

    }
    private void uploadPicture() {

        snackbar = Snackbar.make(backDrop,"Uploading photo, chill just a few seconds ...",
                Snackbar.LENGTH_INDEFINITE);
        setRefreshActionButtonState(true);
        snackbar.show();
        if (player != null) {
            CDNUploader.uploadFile(getApplicationContext(),
                    player, this, new CDNUploader.CDNUploaderListener() {
                @Override
                public void onFileUploaded(PlayerDTO player) {
                    setRefreshActionButtonState(false);
                    snackbar.dismiss();
                    onBackPressed();
                }

                @Override
                public void onFileUploaded(CoachDTO coach) {
                    setRefreshActionButtonState(false);
                    snackbar.dismiss();
                    onBackPressed();
                }

                @Override
                public void onError(String message) {
                    setRefreshActionButtonState(false);
                    snackbar.dismiss();
                }
            });
        }
        if (coach != null) {
            CDNUploader.uploadFile(getApplicationContext(),
                    coach, this, new CDNUploader.CDNUploaderListener() {
                @Override
                public void onFileUploaded(PlayerDTO player) {
                    setRefreshActionButtonState(false);
                    snackbar.dismiss();
                    onBackPressed();
                }

                @Override
                public void onFileUploaded(CoachDTO coach) {
                    setRefreshActionButtonState(false);
                    snackbar.dismiss();
                    onBackPressed();
                }

                @Override
                public void onError(String message) {
                    setRefreshActionButtonState(false);
                }
            });
        }
    }

    /**
     * AsyncTask to process the image received from the camera
     */
    class PhotoTask extends AsyncTask<Void, Void, Integer> {


        /**
         * Scale the image to required size and delete the larger one
         *
         * @param voids
         * @return
         */
        @Override
        protected Integer doInBackground(Void... voids) {
            try {
                if (photoFile == null || photoFile.length() == 0) {
                    Log.e(LOG, "----->> photoFile is null or length 0, exiting");
                    return 99;
                } else {
                    Log.w(LOG, "## PhotoTask starting, photoFile length: "
                            + photoFile.length());
                }
                processFile();
                //resizePhoto();
            } catch (Exception e) {
                Log.e(LOG, "Camera file processing failed", e);
                return 9;
            }


            return 0;
        }

        private Bitmap decode(Bitmap bm, int width) {
            float aspectRatio = bm.getWidth() /
                    (float) bm.getHeight();
            int height = Math.round(width / aspectRatio);

            Bitmap x = Bitmap.createScaledBitmap(
                    bm, width, height, false);

            return x;
        }

        protected void processFile() throws Exception {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 4;
            Bitmap main = BitmapFactory.decodeFile(photoFile.getAbsolutePath(), options);
            Bitmap bm = ScalingUtilities.createScaledBitmap(
                    main, 600, 800, ScalingUtilities.ScalingLogic.CROP);

            if (main.getWidth() > main.getHeight()) {
                Log.d(LOG, "*** this image in landscape");
                // bm = Util.rotateBitmap(bm);

            }
            getLog(bm, "decoded Bitmap");
            currentThumbFile = ImageUtil.getFileFromBitmap(bm,
                    "t" + System.currentTimeMillis() + ".jpg");
            main.recycle();
            bm.recycle();
            Log.i(LOG, "## photo file length: " + getLength(currentThumbFile.length())
                    + ", original size: " + getLength(photoFile.length()));

        }

        private void resizePhoto() throws Exception {
            Log.w(LOG, "## PhotoTask starting doInBackground, file length: " + photoFile.length());
            if (photoFile == null || photoFile.length() == 0) {
                Log.e(LOG, "----- photoFile is null or length 0, exiting");
                throw new Exception();
            }

            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 4;
            Bitmap bm = BitmapFactory.decodeFile(photoFile.getAbsolutePath(), options);
            getLog(bm, "Raw Camera- sample size = 4");
            Matrix matrixThumbnail = new Matrix();
            matrixThumbnail.postScale(0.6f, 0.6f);

            Bitmap thumb = Bitmap.createBitmap
                    (bm, 0, 0, bm.getWidth(), bm.getHeight(), matrixThumbnail, true);
            getLog(thumb, "Photo bitMap created");

            currentThumbFile = ImageUtil.getFileFromBitmap(
                    thumb, "t" + System.currentTimeMillis() + ".jpg");

            //clean up
            boolean del = photoFile.delete();
            bm.recycle();
            thumb.recycle();
            Log.i(LOG, "## Thumbnail file length: " + currentThumbFile.length()
                    + " main image file deleted: " + del);


        }

        private String getLength(long num) {
            BigDecimal decimal = new BigDecimal(num).divide(new BigDecimal(1024), 2, BigDecimal.ROUND_HALF_UP);

            return "" + decimal.doubleValue() + " KB";
        }

        @Override
        protected void onPostExecute(Integer result) {
            Log.e(LOG, "onPostExecute result: " + result.intValue());
            if (result > 0) {
                pictureTakenOK = false;
                Util.showErrorToast(ctx, "Unable to get camera image");
                return;
            }
            pictureTakenOK = true;
            Picasso.with(ctx).load(currentThumbFile).into(backDrop);
            if (player != null) {
                player.setFilePath(currentThumbFile.getAbsolutePath());
            }
            if (coach != null) {
                coach.setFilePath(currentThumbFile.getAbsolutePath());
            }

        }
    }

    boolean pictureTakenOK;
    private void getLog(Bitmap bm, String which) {
        if (bm == null) return;
        Log.e(LOG, which + " - bitmap: width: "
                + bm.getWidth() + " height: "
                + bm.getHeight() + " rowBytes: "
                + bm.getRowBytes());
    }
    Menu mMenu;
    Snackbar snackbar;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.picture, menu);
        mMenu = menu;
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_upload) {
            if (player != null) {
                uploadPicture();
            }
            return true;
        }
        if (id == R.id.action_help) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    @Override
    public void onBackPressed() {
        if (pictureTakenOK) {
            Intent m = new Intent();
            if (player != null) {
                m.putExtra("player",player);
            }
            if (coach != null) {
              m.putExtra("coach",coach);
            }
            setResult(RESULT_OK,m);
        }
        finish();
    }
    public void setRefreshActionButtonState(final boolean refreshing) {
        if (mMenu != null) {
            final MenuItem refreshItem = mMenu.findItem(R.id.action_upload);
            if (refreshItem != null) {
                if (refreshing) {
                    refreshItem.setActionView(com.boha.golfpractice.library.R.layout.action_bar_progess);
                } else {
                    refreshItem.setActionView(null);
                }
            }
        }
    }

}
