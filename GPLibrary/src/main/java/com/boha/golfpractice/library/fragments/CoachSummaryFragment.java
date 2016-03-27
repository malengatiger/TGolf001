package com.boha.golfpractice.library.fragments;


import android.app.Activity;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import com.boha.golfpractice.library.R;
import com.boha.golfpractice.library.activities.MonApp;
import com.boha.golfpractice.library.adapters.BagListAdapter;
import com.boha.golfpractice.library.dto.BagDTO;
import com.boha.golfpractice.library.dto.PlayerDTO;
import com.boha.golfpractice.library.dto.PracticeSessionDTO;
import com.boha.golfpractice.library.dto.RequestDTO;
import com.boha.golfpractice.library.dto.ResponseDTO;
import com.boha.golfpractice.library.util.MonLog;
import com.boha.golfpractice.library.util.OKHttpException;
import com.boha.golfpractice.library.util.OKUtil;
import com.boha.golfpractice.library.util.SharedUtil;
import com.boha.golfpractice.library.util.SnappyGeneral;
import com.boha.golfpractice.library.util.Util;
import com.squareup.leakcanary.RefWatcher;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class CoachSummaryFragment extends Fragment implements PageFragment {

    protected TextView golfCourse, sessionDate, count, date;
    protected TextView numHoles, numStrokes, numUnder, numOver, par, numMistakes;
    protected TextView numHolesAvg, numStrokesAvg, numUnderAvg, numOverAvg, parAvg, numMistakesAvg;
    RecyclerView recycler;
    SeekBar seekBar;
    View view;
    MonApp app;
    FloatingActionButton fab;
    List<PracticeSessionDTO> practiceSessionList;
    static final String LOG = CoachSummaryFragment.class.getSimpleName();

    public CoachSummaryFragment() {
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_coachsum, container, false);
        recycler = (RecyclerView) view.findViewById(R.id.recycler);
        LinearLayoutManager llm = new LinearLayoutManager(getContext());
        recycler.setLayoutManager(llm);

        app = (MonApp) getActivity().getApplication();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        MonLog.w(getActivity(), LOG, "########### onResume");
        getCachedData();
    }

    List<BagDTO> bagList = new ArrayList<>();
    BagListAdapter adapter;
    int days = 90;
    private void setSummaryData() {
        BagDTO bag1 = new BagDTO();
        bag1.setPracticeSessionList(new ArrayList<PracticeSessionDTO>());
        bag1.setDescription("Male Players");
        BagDTO bag2 = new BagDTO();
        bag2.setPracticeSessionList(new ArrayList<PracticeSessionDTO>());
        bag2.setDescription("Female Players");

        for (PracticeSessionDTO ps: practiceSessionList) {
            if (ps.getGender() == 1) {
                bag1.getPracticeSessionList().add(ps);
            }
            if (ps.getGender() == 2) {
                bag2.getPracticeSessionList().add(ps);
            }
        }
        bagList.clear();
        bagList.add(bag1);
        bagList.add(bag2);

        adapter = new BagListAdapter(bagList, getContext(), days,new BagListAdapter.BagListener() {
            @Override
            public void onSummaryClicked(BagDTO bag) {

            }

            @Override
            public void onRefreshRequired(BagDTO bag, int days) {
                getRemoteData(days);
            }
        });
        recycler.setAdapter(adapter);
    }

    @Override
    public String getPageTitle() {
        return "Coach's Summary";
    }

    private void getCachedData() {
        SnappyGeneral.getLookups(app, new SnappyGeneral.DBReadListener() {
            @Override
            public void onDataRead(ResponseDTO response) {
                List<PlayerDTO> list = response.getPlayerList();
                practiceSessionList = new ArrayList<>();
                for (PlayerDTO p : list) {
                    practiceSessionList.addAll(p.getPracticeSessionList());
                }
                if (view != null) {
                    setSummaryData();
                }
            }

            @Override
            public void onError(String message) {

            }
        });
    }

    private void getRemoteData(int days) {
        this.days = days;
        RequestDTO w = new RequestDTO(RequestDTO.GET_COACH_DATA);
        w.setCoachID(SharedUtil.getCoach(getContext()).getCoachID());
        w.setDays(days);
        w.setZipResponse(true);

        mListener.setBusy(true);
        OKUtil util = new OKUtil();
        try {
            util.sendGETRequest(getActivity(), w, getActivity(), new OKUtil.OKListener() {
                @Override
                public void onResponse(ResponseDTO response) {
                    mListener.setBusy(false);
                    List<PlayerDTO> list = response.getPlayerList();
                    practiceSessionList = new ArrayList<PracticeSessionDTO>();
                    for (PlayerDTO p : list) {
                        practiceSessionList.addAll(p.getPracticeSessionList());
                    }
                    if (view != null) {
                        setSummaryData();
                    }
                    SnappyGeneral.addPlayers(app, response.getPlayerList(), new SnappyGeneral.DBWriteListener() {
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
                    mListener.setBusy(false);
                    Util.showErrorToast(getContext(), message);
                }
            });
        } catch (OKHttpException e) {
            e.printStackTrace();
        }

    }

    CoachSummaryListener mListener;

    @Override
    public void onAttach(Activity a) {
        if (a instanceof CoachSummaryListener) {
            mListener = (CoachSummaryListener) a;
        } else {
            throw new RuntimeException("Host Activity " + a.getLocalClassName() +
                    " must implement CoachSummaryListener");
        }
        super.onAttach(a);
    }

    public interface CoachSummaryListener {
        void setBusy(boolean busy);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        MonApp app = (MonApp) getActivity().getApplication();
        RefWatcher refWatcher = app.getRefWatcher();
        refWatcher.watch(this);
    }
}
