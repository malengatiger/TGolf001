package com.boha.golfpractice.library.adapters;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.boha.golfpractice.library.R;
import com.boha.golfpractice.library.dto.PlayerDTO;
import com.boha.golfpractice.library.dto.PracticeSessionDTO;
import com.boha.golfpractice.library.util.Statics;
import com.boha.golfpractice.library.util.Util;
import com.squareup.picasso.Picasso;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by aubreyM on 14/12/17.
 */
public class PlayerListAdapter extends RecyclerView.Adapter<PlayerListAdapter.PlayerViewHolder> {

    public interface PlayerAdapterListener {
        void onPlayerClicked(PlayerDTO session);
    }

    private PlayerAdapterListener mListener;
    private List<PlayerDTO> playerList;
    private Context ctx;

    public PlayerListAdapter(List<PlayerDTO> sessionList,
                             Context context, PlayerAdapterListener listener) {
        this.playerList = sessionList;
        this.ctx = context;
        this.mListener = listener;
    }


    @Override
    public PlayerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.player_item, parent, false);
        return new PlayerViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final PlayerViewHolder holder, final int position) {

        final PlayerDTO player = playerList.get(position);
        if (player.getFirstName() == null) {
            holder.playerName.setText(player.getEmail());
        } else {
            holder.playerName.setText(player.getFullName());
        }
        Statics.setRobotoFontLight(ctx,holder.playerName);
        holder.numberOfSessions.setText("" + player.getPracticeSessionList().size());
        holder.number.setVisibility(View.GONE);
        holder.lastPractice.setVisibility(View.GONE);
        if (!player.getPracticeSessionList().isEmpty()) {
            PracticeSessionDTO p = player.getPracticeSessionList().get(0);
            holder.golfCourse.setText(p.getGolfCourseName());
            holder.sessionDate.setText(sdf.format(new Date(p.getSessionDate())));
            holder.numHoles.setText("" + p.getNumberOfHoles());
            holder.numStrokes.setText("" + p.getTotalStrokes());
            holder.numUnder.setText("" + p.getUnderPar());
            holder.par.setText("" + p.getPar());
            holder.numOver.setText("" + p.getOverPar());



            holder.numMistakes.setText("" + p.getTotalMistakes());
            if (p.getClosed() == false) {
                holder.btnClose.setEnabled(true);
                holder.btnClose.setText("Close Session");
                holder.btnClose.setAlpha(1.0f);
                holder.golfCourse.setTextColor(ContextCompat.getColor(ctx, R.color.black));

            } else {
                holder.btnClose.setEnabled(false);
                holder.btnClose.setText("Closed");
                holder.btnClose.setAlpha(0.5f);
                holder.golfCourse.setTextColor(ContextCompat.getColor(ctx, R.color.grey_600));
            }
            Statics.setRobotoFontLight(ctx, holder.golfCourse);
        }


        holder.numberOfSessions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (player.getPracticeSessionList().isEmpty()) {
                    return;
                }
                if (holder.lastPractice.getVisibility() == View.VISIBLE) {
                    Util.collapse(holder.lastPractice,1000,null);
                } else {
                    Util.expand(holder.lastPractice,1000,null);
                }
            }
        });
        holder.image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onPlayerClicked(player);
            }
        });
        holder.playerName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onPlayerClicked(player);
            }
        });

        if (player.getPhotoUrl() != null) {
            Picasso.with(ctx).load(player.getPhotoUrl()).into(holder.image);
            holder.image.setAlpha(1.0f);
        } else {
            Picasso.with(ctx).load(R.drawable.boy).into(holder.image);
            holder.image.setAlpha(0.4f);
        }

    }

    @Override
    public int getItemCount() {
        return playerList == null ? 0 : playerList.size();
    }

    static final Locale loc = Locale.getDefault();
    static final SimpleDateFormat sdf = new SimpleDateFormat("EEEE dd MMMM yyyy HH:mm", loc);
    static final DecimalFormat df = new DecimalFormat("###,###,###,###");

    public class PlayerViewHolder extends RecyclerView.ViewHolder  {
        protected TextView golfCourse, sessionDate, number, playerName, numberOfSessions;
        protected Button btnClose;
        protected View lastPractice;
        protected CircleImageView image;
        protected TextView numHoles, numStrokes, numUnder, numOver, par, numMistakes;


        public PlayerViewHolder(View itemView) {
            super(itemView);
            lastPractice = itemView
                    .findViewById(R.id.lastPractice);
            golfCourse = (TextView) itemView
                    .findViewById(R.id.golfCourse);
            playerName = (TextView) itemView
                    .findViewById(R.id.name);
            image = (CircleImageView) itemView
                    .findViewById(R.id.playerImage);
            numberOfSessions = (TextView) itemView
                    .findViewById(R.id.practiceCount);
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
            number = (TextView) itemView
                    .findViewById(R.id.number);
            btnClose = (Button) itemView
                    .findViewById(R.id.btnClose);




        }

    }

    static final String LOG = PlayerListAdapter.class.getSimpleName();
}
