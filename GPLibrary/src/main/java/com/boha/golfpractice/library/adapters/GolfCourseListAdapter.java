package com.boha.golfpractice.library.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.boha.golfpractice.library.R;
import com.boha.golfpractice.library.dto.GolfCourseDTO;
import com.boha.golfpractice.library.util.Statics;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

/**
 * Created by aubreyM on 14/12/17.
 */
public class GolfCourseListAdapter extends RecyclerView.Adapter<GolfCourseListAdapter.GolfCourseViewHolder> {

    public interface GolfCourseListener {
        void onCourseClicked(GolfCourseDTO course);

        void onStartSession(GolfCourseDTO course);

        void onGetSessions(GolfCourseDTO course);

        void onGetDirections(GolfCourseDTO course);
    }

    private GolfCourseListener mListener;
    private List<GolfCourseDTO> golfCourseList;
    private Context ctx;

    public GolfCourseListAdapter(List<GolfCourseDTO> courseList,
                                 Context context, GolfCourseListener listener) {
        this.golfCourseList = courseList;
        this.ctx = context;
        this.mListener = listener;
    }


    @Override
    public GolfCourseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.golfcourse_item, parent, false);
        return new GolfCourseViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final GolfCourseViewHolder holder, final int position) {

        final GolfCourseDTO p = golfCourseList.get(position);
        holder.golfCourse.setText(p.getGolfCourseName());
        holder.number.setText("" + (position + 1));
        holder.golfCourse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onCourseClicked(p);
            }
        });
        holder.number.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onCourseClicked(p);
            }
        });

        Statics.setRobotoFontLight(ctx, holder.golfCourse);
        if (p.getDistance() != null) {
            holder.distance.setText(df.format(p.getDistance()));
            holder.distance.setVisibility(View.VISIBLE);
            holder.distanceLabel.setVisibility(View.VISIBLE);
        } else {
            holder.distance.setVisibility(View.GONE);
            holder.distanceLabel.setVisibility(View.GONE);
        }

        holder.iconDirections.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onGetDirections(p);
            }
        });
        holder.iconStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onStartSession(p);
            }
        });
        holder.iconSessions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onGetSessions(p);
            }
        });

    }

    @Override
    public int getItemCount() {
        return golfCourseList == null ? 0 : golfCourseList.size();
    }

    static final Locale loc = Locale.getDefault();
    static final SimpleDateFormat sdf = new SimpleDateFormat("EEEE dd MM yyyy HH:mm", loc);
    static final DecimalFormat df = new DecimalFormat("###,###,###,##0.00");

    public class GolfCourseViewHolder extends RecyclerView.ViewHolder {
        protected TextView golfCourse, number, distance, distanceLabel;
        protected ImageView iconDirections, iconStart, iconSessions;


        public GolfCourseViewHolder(View itemView) {
            super(itemView);

            golfCourse = (TextView) itemView
                    .findViewById(R.id.courseName);
            number = (TextView) itemView
                    .findViewById(R.id.number);
            distance = (TextView) itemView
                    .findViewById(R.id.distance);
            distanceLabel = (TextView) itemView
                    .findViewById(R.id.distLabel);
            iconDirections = (ImageView) itemView
                    .findViewById(R.id.iconDirections);
            iconStart = (ImageView) itemView
                    .findViewById(R.id.iconStartSession);
            iconSessions = (ImageView) itemView
                    .findViewById(R.id.iconSessions);
        }

    }

    static final String LOG = GolfCourseListAdapter.class.getSimpleName();
}
