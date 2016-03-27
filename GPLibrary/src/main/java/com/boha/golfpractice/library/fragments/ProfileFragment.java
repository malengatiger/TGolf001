package com.boha.golfpractice.library.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;

import com.boha.golfpractice.library.R;
import com.boha.golfpractice.library.activities.MonApp;
import com.boha.golfpractice.library.dto.CoachDTO;
import com.boha.golfpractice.library.dto.PlayerDTO;
import com.boha.golfpractice.library.dto.RequestDTO;
import com.boha.golfpractice.library.dto.ResponseDTO;
import com.boha.golfpractice.library.util.OKHttpException;
import com.boha.golfpractice.library.util.OKUtil;
import com.boha.golfpractice.library.util.SharedUtil;
import com.boha.golfpractice.library.util.SnappyGeneral;
import com.boha.golfpractice.library.util.Util;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileFragment extends Fragment {

    private ProfileListener mListener;
    private PlayerDTO player;
    private CoachDTO coach;
    private EditText editFirstName, editLastName, editEmail, editCellphone;
    private RadioButton radioMale, radioFemale, radioActive, radioInactive;
    Button btnSend;
    FloatingActionButton fab;
    View view;
    TextView txtName;
    CircleImageView imageView;
    ImageView backdrop;

    public ProfileFragment() {
    }

    public static ProfileFragment newInstance(PlayerDTO player) {
        ProfileFragment fragment = new ProfileFragment();
        Bundle args = new Bundle();
        args.putSerializable("player", player);
        fragment.setArguments(args);
        return fragment;
    }

    public static ProfileFragment newInstance(CoachDTO coach) {
        ProfileFragment fragment = new ProfileFragment();
        Bundle args = new Bundle();
        args.putSerializable("coach", coach);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            player = (PlayerDTO) getArguments().getSerializable("player");
            coach = (CoachDTO) getArguments().getSerializable("coach");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_profile, container, false);
        setFields();
        return view;
    }

    public void getImage(String url) {

        Picasso.with(getContext()).load(url).into(imageView);
        Picasso.with(getContext()).load(url).into(backdrop);

        if (player != null) {
            player.setPhotoUrl(url);
        }
        if (coach != null) {
            coach.setPhotoUrl(url);
        }
    }

    public void clearFields() {
        editEmail.setText("");
        editLastName.setText("");
        editFirstName.setText("");
        editEmail.setText("");
        editCellphone.setText("");
        radioActive.setChecked(false);
        radioMale.setChecked(false);
        radioFemale.setChecked(false);
        mListener.setBusy(false);
        fab.setVisibility(View.GONE);

        Picasso.with(getContext()).load(R.drawable.boy).into(imageView);
        Picasso.with(getContext()).load(R.drawable.boy).into(backdrop);
    }

    private void setFields() {
        backdrop = (ImageView)view.findViewById(R.id.FMP_backdrop);
        editFirstName = (EditText) view.findViewById(R.id.FMP_editFirstName);
        editLastName = (EditText) view.findViewById(R.id.FMP_editLastName);
        editCellphone = (EditText) view.findViewById(R.id.FMP_editCell);
        editEmail = (EditText) view.findViewById(R.id.FMP_editEmail);
        radioFemale = (RadioButton) view.findViewById(R.id.FMP_radioFemale);
        radioMale = (RadioButton) view.findViewById(R.id.FMP_radioMale);
        radioActive = (RadioButton) view.findViewById(R.id.FMP_radioActive);
        radioInactive = (RadioButton) view.findViewById(R.id.FMP_radioInactive);
        btnSend = (Button) view.findViewById(R.id.FMP_btnSave);
        imageView = (CircleImageView) view.findViewById(R.id.FMP_personImage);
        txtName = (TextView) view.findViewById(R.id.FMP_person);
        fab = (FloatingActionButton) view.findViewById(R.id.fab);
        fab.setVisibility(View.GONE);

        if (coach != null) {
            setCoachFields();
        }
        if (player != null) {
            setPlayerFields();
        }
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Util.flashOnce(imageView, 300, new Util.UtilAnimationListener() {
                    @Override
                    public void onAnimationEnded() {
                        if (player != null)
                            if (player.getPlayerID() != null)
                                mListener.onPictureRequested(player);
                            else
                                Util.showToast(getContext(),"Please save the profile first");
                        if (coach != null) {
                            if (coach.getCoachID() != null)
                                mListener.onPictureRequested(coach);
                            else
                                Util.showToast(getContext(),"Please save the profile first");
                        }
                    }
                });

            }
        });
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Util.flashOnce(btnSend, 300, new Util.UtilAnimationListener() {
                    @Override
                    public void onAnimationEnded() {
                        sendData();
                    }
                });
            }
        });

        editFirstName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                setPersonName();
                btnSend.setVisibility(View.VISIBLE);
            }
        });
        editLastName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                setPersonName();
                btnSend.setVisibility(View.VISIBLE);
            }
        });
    }
    private void setPersonName() {
        txtName.setText(editFirstName.getText().toString() + " " + editLastName.getText().toString());
    }
    private void hideKeyboard() {

        InputMethodManager imm = (InputMethodManager) getContext()
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(editFirstName.getWindowToken(), 0);
    }

    private void sendData() {
        hideKeyboard();
        if (editFirstName.getText().toString().isEmpty()) {
            Util.showToast(getContext(),"Please enter First Name");
            return;
        }
        if (editLastName.getText().toString().isEmpty()) {
            Util.showToast(getContext(),"Please enter Last Name");
            return;
        }
        if (editEmail.getText().toString().isEmpty()) {
            Util.showToast(getContext(),"Please enter Email address");
            return;
        }
        if (editCellphone.getText().toString().isEmpty()) {
            Util.showToast(getContext(),"Please enter Cellphone number");
            return;
        }
        RequestDTO w = new RequestDTO(RequestDTO.EDIT_PLAYER);
        if (coach != null) {
            w.setRequestType(RequestDTO.EDIT_COACH);
            w.setCoach(coach);
        } else {
            PlayerDTO p = new PlayerDTO();
            p.setPlayerID(player.getPlayerID());
            p.setFirstName(editFirstName.getText().toString());
            p.setLastName(editLastName.getText().toString());
            p.setCellphone(editCellphone.getText().toString());
            p.setEmail(editEmail.getText().toString());

            w.setPlayer(p);
            CoachDTO c = SharedUtil.getCoach(getContext());
            if (c != null) {
                w.setCoachID(c.getCoachID());
            }
        }

        OKUtil util = new OKUtil();
        btnSend.setEnabled(false);
        btnSend.setAlpha(0.3f);
        mListener.setBusy(true);
        try {
            util.sendGETRequest(getContext(), w, getActivity(), new OKUtil.OKListener() {
                @Override
                public void onResponse(ResponseDTO response) {
                    mListener.setBusy(false);
                    btnSend.setEnabled(true);
                    btnSend.setAlpha(1.0f);
                    fab.setVisibility(View.VISIBLE);
                    Util.flashOnce(imageView,500,null);
                    Util.flashOnce(fab,500,null);
                    Snackbar.make(btnSend,"Player added to database, please take profile picture",Snackbar.LENGTH_LONG).show();
                    if (coach != null) {
                        coach = response.getCoachList().get(0);
                        SharedUtil.saveCoach(getContext(), coach);
                    } else {
                        player = response.getPlayerList().get(0);
                        PlayerDTO px = SharedUtil.getPlayer(getContext());
                        if (px != null) {
                            if (px.getPlayerID().intValue() == player.getPlayerID().intValue()) {
                                SharedUtil.savePlayer(getContext(), player);
                            }
                        }
                        List<PlayerDTO> list = new ArrayList<PlayerDTO>();
                        list.add(player);
                        SnappyGeneral.addPlayers((MonApp) getActivity().getApplication(),
                                list, new SnappyGeneral.DBWriteListener() {
                            @Override
                            public void onDataWritten() {
                                mListener.onPlayerAddedToServer(player);
                            }

                            @Override
                            public void onError(String message) {

                            }
                        });
                    }
                }

                @Override
                public void onError(String message) {
                    mListener.setBusy(false);
                    btnSend.setEnabled(true);
                    btnSend.setAlpha(1.0f);
                    Util.showErrorToast(getContext(), message);
                }
            });
        } catch (OKHttpException e) {
            e.printStackTrace();
        }
    }

    private void setPlayerFields() {
        if (player.getFirstName() != null) {
            editFirstName.setText(player.getFirstName());
        }
        if (player.getLastName() != null) {
            editLastName.setText(player.getLastName());
        }
        if (player.getCellphone() != null) {
            editCellphone.setText(player.getCellphone());
        }
        if (player.getEmail() != null) {
            editEmail.setText(player.getEmail());
        }
    }

    private void setCoachFields() {
        if (coach.getFirstName() != null) {
            editFirstName.setText(coach.getFirstName());
        }
        if (coach.getLastName() != null) {
            editLastName.setText(coach.getLastName());
        }
        if (coach.getCellphone() != null) {
            editCellphone.setText(coach.getCellphone());
        }
        if (coach.getEmail() != null) {
            editEmail.setText(coach.getEmail());
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof ProfileListener) {
            mListener = (ProfileListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement ProfileListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface ProfileListener {
        void setBusy(boolean busy);

        void onPictureRequested(CoachDTO coach);

        void onPictureRequested(PlayerDTO player);
        void onPlayerAddedToServer(PlayerDTO player);
    }

    public void setListener(ProfileListener mListener) {
        this.mListener = mListener;
    }
}
