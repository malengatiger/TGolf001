package com.boha.golfpractice.library.adapters;

import android.content.Context;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import com.boha.golfpractice.library.R;
import com.boha.golfpractice.library.dto.BagDTO;
import com.boha.golfpractice.library.dto.PracticeSessionDTO;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

/**
 * Created by aubreyM on 14/12/17.
 */
public class BagListAdapter extends RecyclerView.Adapter<BagListAdapter.BagViewHolder> {

    public interface BagListener {
        void onSummaryClicked(BagDTO bag);

        void onRefreshRequired(BagDTO bag, int days);
    }

    private BagListener mListener;
    private List<BagDTO> bagList;
    private Context ctx;
    private int days;

    public BagListAdapter(List<BagDTO> bagList,
                          Context context, int days, BagListener listener) {
        this.bagList = bagList;
        this.ctx = context;
        this.mListener = listener;
        this.days = days;
    }


    @Override
    public BagViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.coach_session_summary, parent, false);
        return new BagViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final BagViewHolder h, final int position) {
        final BagDTO bag = bagList.get(position);
        final List<PracticeSessionDTO> practiceSessionList = bag.getPracticeSessionList();
        h.count.setText(df.format(practiceSessionList.size()));
        h.golfCourse.setText(bag.getDescription());
        h.seekBar.setProgress(days);
        h.days.setText("" + days);
        int totalStrokes = 0,
                totalPar = 0,
                totalUnderPar = 0,
                totalOverPar = 0,
                totalHoles = 0,
                totalMistakes = 0;

        for (PracticeSessionDTO p : practiceSessionList) {
            totalHoles += p.getNumberOfHoles();
            totalStrokes += p.getTotalStrokes();
            totalPar += p.getPar();
            totalUnderPar += p.getUnderPar();
            totalOverPar += p.getOverPar();
            totalMistakes += p.getTotalMistakes();
        }
        h.numHoles.setText(df.format(totalHoles));
        h.numStrokes.setText(df.format(totalStrokes));
        h.numUnder.setText(df.format(totalUnderPar));
        h.numOver.setText(df.format(totalOverPar));
        h.par.setText(df.format(totalPar));
        h.numMistakes.setText(df.format(totalMistakes));
        if (totalHoles == 0) {
            h.numHolesAvg.setText("0.0");
            h.numStrokesAvg.setText("0.0");
            h.numUnderAvg.setText("0.0%");
            h.parAvg.setText("0.0%");
            h.numOverAvg.setText("0.0%");
            h.numMistakesAvg.setText("0.0%");
        } else {
            Double hAvg = Double.parseDouble("" + totalHoles) / Double.parseDouble("" + practiceSessionList.size());
            h.numHolesAvg.setText(dfAvg.format(hAvg));

            Double strokeAvg = Double.parseDouble("" + totalStrokes) / Double.parseDouble("" + totalHoles);
            h.numStrokesAvg.setText(dfAvg.format(strokeAvg));

            Double underAvg = Double.parseDouble("" + totalUnderPar) / Double.parseDouble("" + totalHoles) * 100;
            h.numUnderAvg.setText(dfAvg.format(underAvg) + "%");

            Double overAvg = Double.parseDouble("" + totalOverPar) / Double.parseDouble("" + totalHoles) * 100;
            h.numOverAvg.setText(dfAvg.format(overAvg) + "%");

            Double pAvg = Double.parseDouble("" + totalPar) / Double.parseDouble("" + totalHoles) * 100;
            h.parAvg.setText(dfAvg.format(pAvg) + "%");

            Double misAvg = Double.parseDouble("" + totalMistakes) / Double.parseDouble("" + totalHoles) * 100;
            h.numMistakesAvg.setText(dfAvg.format(misAvg) + "%");
        }
        h.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int days = h.seekBar.getProgress();
                if (days == 0) {
                    days = 90;
                }
                mListener.onRefreshRequired(bag, days);
            }
        });

        h.seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                h.days.setText("" + progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return bagList == null ? 0 : bagList.size();
    }

    static final Locale loc = Locale.getDefault();
    static final SimpleDateFormat sdf = new SimpleDateFormat("EEEE dd MMMM yyyy HH:mm", loc);
    static final DecimalFormat df = new DecimalFormat("###,###,###,###");
    static final DecimalFormat dfAvg = new DecimalFormat("###,###,##0.00");

    public class BagViewHolder extends RecyclerView.ViewHolder {
        protected TextView golfCourse, sessionDate, count, days;
        protected TextView numHoles, numStrokes, numUnder, numOver, par, numMistakes;
        protected TextView numHolesAvg, numStrokesAvg, numUnderAvg, numOverAvg, parAvg, numMistakesAvg;
        SeekBar seekBar;
        FloatingActionButton fab;

        public BagViewHolder(View itemView) {
            super(itemView);

            days = (TextView) itemView
                    .findViewById(R.id.days);
            golfCourse = (TextView) itemView
                    .findViewById(R.id.golfCourse);
            sessionDate = (TextView) itemView
                    .findViewById(R.id.days);
            numHoles = (TextView) itemView
                    .findViewById(R.id.holeCount);
            numStrokes = (TextView) itemView
                    .findViewById(R.id.strokes);

            numUnder = (TextView) itemView
                    .findViewById(R.id.underPar);
            numOver = (TextView) itemView
                    .findViewById(R.id.overpar);
            par = (TextView) itemView
                    .findViewById(R.id.par);
            numMistakes = (TextView) itemView
                    .findViewById(R.id.mistakes);
            count = (TextView) itemView
                    .findViewById(R.id.count);

            numHolesAvg = (TextView) itemView
                    .findViewById(R.id.holeAverage);
            numStrokesAvg = (TextView) itemView
                    .findViewById(R.id.strokeAverage);

            numUnderAvg = (TextView) itemView
                    .findViewById(R.id.underParAverage);
            numOverAvg = (TextView) itemView
                    .findViewById(R.id.overParAverage);
            parAvg = (TextView) itemView
                    .findViewById(R.id.parAverage);
            numMistakesAvg = (TextView) itemView
                    .findViewById(R.id.mistakeAverage);
            seekBar = (SeekBar) itemView.findViewById(R.id.seekBar);
            fab = (FloatingActionButton) itemView.findViewById(R.id.fab);


        }

    }

    static final String LOG = BagListAdapter.class.getSimpleName();
}
