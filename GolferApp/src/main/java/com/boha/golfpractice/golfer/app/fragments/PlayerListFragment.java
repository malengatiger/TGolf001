package com.boha.golfpractice.golfer.app.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.boha.golfpractice.golfer.R;
import com.boha.golfpractice.library.activities.MonApp;
import com.boha.golfpractice.library.activities.SessionSummaryActivity;
import com.boha.golfpractice.library.adapters.PlayerListAdapter;
import com.boha.golfpractice.library.dto.PlayerDTO;
import com.boha.golfpractice.library.dto.ResponseDTO;
import com.boha.golfpractice.library.fragments.PageFragment;
import com.boha.golfpractice.library.util.SnappyGeneral;
import com.boha.golfpractice.library.util.Util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link PlayerListener} interface
 * to handle interaction events.
 */
public class PlayerListFragment extends Fragment implements PageFragment {

    private PlayerListener mListener;
    private List<PlayerDTO> playerList;
    TextView txtCount;
    RecyclerView recycler;
    View view, handle;
    PlayerListAdapter adapter;
    FloatingActionButton fab;
    static final String LOG = PlayerListFragment.class.getSimpleName();

    public PlayerListFragment() {
    }
    public static PlayerListFragment newInstance(List<PlayerDTO> list) {
        PlayerListFragment f = new PlayerListFragment();
        ResponseDTO w = new ResponseDTO();
        w.setPlayerList(list);
        Bundle b = new Bundle();
        b.putSerializable("response",w);
        f.setArguments(b);
        return f;
    }
    @Override
    public void onCreate(Bundle b) {
        super.onCreate(b);
        if (getArguments() != null) {
            ResponseDTO w = (ResponseDTO)getArguments().getSerializable("response");
            playerList = w.getPlayerList();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_player_list, container, false);
        setFields();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        setList();
    }

    public void refreshList() {
        SnappyGeneral.getLookups((MonApp) getActivity().getApplication(),
                new SnappyGeneral.DBReadListener() {
            @Override
            public void onDataRead(ResponseDTO response) {
                playerList = response.getPlayerList();
                if (recycler != null) {
                    setList();
                }
            }

            @Override
            public void onError(String message) {

            }
        });
    }
    private void setList() {
        txtCount.setText("" + playerList.size());
        Collections.sort(playerList);
        adapter = new PlayerListAdapter(playerList, getContext(), new PlayerListAdapter.PlayerAdapterListener() {
            @Override
            public void onPlayerClicked(PlayerDTO player) {
                showPopup(player);
            }
        });
        recycler.setAdapter(adapter);
    }
    private void setFields() {
        recycler = (RecyclerView)view.findViewById(R.id.recycler);
        txtCount = (TextView)view.findViewById(R.id.count);
        fab = (FloatingActionButton) view.findViewById(R.id.fab);
        handle = view.findViewById(R.id.handle);

        LinearLayoutManager llm = new LinearLayoutManager(getContext());
        recycler.setLayoutManager(llm);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onNewPlayerRequired();
            }
        });
    }

    private void showPopup(final PlayerDTO player) {
        List<String> list = new ArrayList<>();
        list.add("Practice Session Summary");
        list.add("Start Video Session");
        list.add("Update Profile Details");
        list.add("Set Lesson Schedule");
        list.add("Remove Player");

        Util.showPopupBasicWithHeroImage(getContext(),
                getActivity(), list, handle,
                player.getFullName(), false, player.getPhotoUrl(),
                new Util.UtilPopupListener() {
            @Override
            public void onItemSelected(int index) {
                switch (index) {
                    case 0:
                        Intent m = new Intent(getContext(), SessionSummaryActivity.class);
                        m.putExtra("player",player);
                        startActivity(m);
                        break;
                    case 1:
                        mListener.onPlayerProfileUpdate(player);
                        break;
                    case 2:
                        Util.showToast(getContext(),"Under Construction");
                        break;
                    case 3:
                        Util.showToast(getContext(),"Under Construction");
                        break;
                }
            }
        });


    }
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof PlayerListener) {
            mListener = (PlayerListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement PlayerListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public String getPageTitle() {
        return "Player List";
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
    public interface PlayerListener {
        void onPlayerClicked(PlayerDTO player);
        void onPlayerProfileUpdate(PlayerDTO player);
        void onVideoSession(PlayerDTO player);
        void onNewPlayerRequired();
    }
}
