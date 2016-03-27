package com.boha.golfpractice.golfer.app.activities;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;

import com.boha.golfpractice.golfer.R;
import com.boha.golfpractice.library.activities.MonApp;
import com.boha.golfpractice.library.dto.CoachDTO;
import com.boha.golfpractice.library.dto.PlayerDTO;
import com.boha.golfpractice.library.dto.RequestDTO;
import com.boha.golfpractice.library.dto.ResponseDTO;
import com.boha.golfpractice.library.util.MonLog;
import com.boha.golfpractice.library.util.OKHttpException;
import com.boha.golfpractice.library.util.OKUtil;
import com.boha.golfpractice.library.util.SharedUtil;
import com.boha.golfpractice.library.util.SnappyGeneral;
import com.boha.golfpractice.library.util.Util;

import java.util.ArrayList;
import java.util.List;

import static android.Manifest.permission.READ_CONTACTS;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity {

    // UI references.
    private AutoCompleteTextView mEmailView;
    private EditText mPasswordView;
    private EditText mFirstName, mLastName;
    private View mProgressView;
    private View mLoginFormView;
    private Button btnSignIn;
    private RadioButton radioPlayer,radioCoach, radioOld,radioNew;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        // Set up the login form.
        mEmailView = (AutoCompleteTextView) findViewById(R.id.email);
        populateAutoComplete();

        mPasswordView = (EditText) findViewById(R.id.password);
                btnSignIn = (Button) findViewById(R.id.btnSignIn);


        btnSignIn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                signIn();
            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);
        radioCoach = (RadioButton) findViewById(R.id.radioCoach);
        radioPlayer = (RadioButton) findViewById(R.id.radioPlayer);
        radioOld = (RadioButton) findViewById(R.id.radioOld);
        radioNew = (RadioButton) findViewById(R.id.radioNew);
        checkSignedIn();
    }

    @Override
    public void onResume() {
        super.onResume();
        MonLog.e(getApplicationContext(),"LoginActivity","&&&&&&&& onResume");

    }
    private void checkSignedIn() {

        if (SharedUtil.getPlayer(getApplicationContext()) != null) {
            startPlayerMain();
            return;
        }
        if (SharedUtil.getCoach(getApplicationContext()) != null) {
            startCoachMain();
            return;
        }

    }

    private void startCoachMain() {

        Intent m = new Intent(getApplicationContext(), CoachMainActivity.class);
        startActivity(m);
    }
    private void startPlayerMain() {

        Intent m = new Intent(getApplicationContext(), PlayerMainActivity.class);
        startActivity(m);
    }

    private void signIn() {
        mEmailView.setError(null);
        mPasswordView.setError(null);

        if (!radioCoach.isChecked() && !radioPlayer.isChecked()) {
            Util.showToast(getApplicationContext(),"Please say whether you are a Golfer or Coach");
            return;
        }
        if (!radioOld.isChecked() && !radioNew.isChecked()) {
            Util.showToast(getApplicationContext(),"Please say whether you are are new or a returning user");
            return;
        }

        // Store values at the time of the login attempt.
        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        } else if (!isEmailValid(email)) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            focusView = mEmailView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            setRefreshActionButtonState(true);
            OKUtil okUtil = new OKUtil();
            RequestDTO w = new RequestDTO(RequestDTO.SIGN_IN_PLAYER);
            if (radioCoach.isChecked()) {
                w.setRequestType(RequestDTO.SIGN_IN_COACH);
            }
            if (radioNew.isChecked()) {
                w.setExistingUser(false);
            } else {
                w.setExistingUser(true);
            }
            w.setEmail(mEmailView.getText().toString());
            w.setPassword(mPasswordView.getText().toString());
            w.setZipResponse(true);
            try {
                okUtil.sendGETRequest(getApplicationContext(), w, this,new OKUtil.OKListener() {
                    @Override
                    public void onResponse(ResponseDTO response) {
                        setRefreshActionButtonState(false);
                        SnappyGeneral.addClubs((MonApp)getApplication(),response.getClubList(),null);
                        SnappyGeneral.addShotShapes((MonApp)getApplication(),response.getShotShapeList(),null);

                        if (radioPlayer.isChecked()) {
                            PlayerDTO p = response.getPlayerList().get(0);
                            SharedUtil.savePlayer(getApplicationContext(), p);
                            startPlayerMain();
                        } else {
                            CoachDTO c = response.getCoachList().get(0);
                            SharedUtil.saveCoach(getApplicationContext(),c);
                            if (!response.getPlayerList().isEmpty()) {
                                SnappyGeneral.addPlayers((MonApp) getApplication(), response.getPlayerList(), new SnappyGeneral.DBWriteListener() {
                                    @Override
                                    public void onDataWritten() {
                                        startCoachMain();
                                    }

                                    @Override
                                    public void onError(String message) {

                                    }
                                });
                            }

                        }

                    }

                    @Override
                    public void onError(String message) {
                        setRefreshActionButtonState(false);
                        Util.showErrorToast(getApplicationContext(), message);
                    }
                });
            } catch (OKHttpException e) {
                e.printStackTrace();
            }
        }
    }

    private void populateAutoComplete() {
        addEmailsToAutoComplete();
    }

    private boolean mayRequestContacts() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        if (checkSelfPermission(READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        if (shouldShowRequestPermissionRationale(READ_CONTACTS)) {
            Snackbar.make(mEmailView, R.string.permission_rationale, Snackbar.LENGTH_INDEFINITE)
                    .setAction(android.R.string.ok, new OnClickListener() {
                        @Override
                        @TargetApi(Build.VERSION_CODES.M)
                        public void onClick(View v) {
                            requestPermissions(new String[]{READ_CONTACTS}, REQUEST_READ_CONTACTS);
                        }
                    });
        } else {
            requestPermissions(new String[]{READ_CONTACTS}, REQUEST_READ_CONTACTS);
        }
        return false;
    }

    static final int REQUEST_READ_CONTACTS = 989;

    /**
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_READ_CONTACTS) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                populateAutoComplete();
            }
        }
    }


    private boolean isEmailValid(String email) {
        return email.contains("@");
    }

    private boolean isPasswordValid(String password) {
        return password.length() > 4;
    }



    List<String> mList = new ArrayList<>();
    AccountManager accountManager;
    Menu mMenu;
    Snackbar snackbar;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        mMenu = menu;
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_refresh) {
            radioNew.setChecked(false);
            radioOld.setChecked(false);
            radioCoach.setChecked(false);
            radioPlayer.setChecked(false);
            mEmailView.setText("");
            mPasswordView.setText("");

            return true;
        }
        if (id == R.id.action_help) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    private void addEmailsToAutoComplete() {
        accountManager = AccountManager.get(getApplicationContext());
        Account[] accounts = accountManager.getAccountsByType("com.google");
        if (accounts.length > 0) {
            for (Account a : accounts) {
                mList.add(a.name);
            }
        }
        //Create adapter to tell the AutoCompleteTextView what to show in its dropdown list.
        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(LoginActivity.this,
                        android.R.layout.simple_dropdown_item_1line, mList);

        mEmailView.setAdapter(adapter);
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

}

