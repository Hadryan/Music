package com.dominionos.music.ui.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.graphics.Palette;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.afollestad.async.Action;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.ImageViewTarget;
import com.dominionos.music.R;
import com.dominionos.music.adapters.PlayingSongAdapter;
import com.dominionos.music.items.Song;
import com.dominionos.music.service.MusicService;
import com.dominionos.music.ui.activity.MainActivity;
import com.dominionos.music.utils.Config;
import com.dominionos.music.utils.PaletteBitmap;
import com.dominionos.music.utils.PaletteBitmapTranscoder;
import com.dominionos.music.utils.PlayPauseDrawable;
import com.dominionos.music.utils.Utils;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class PlayerFragment extends Fragment {

    @BindView(R.id.play) private FloatingActionButton play;
    @BindView(R.id.playing_bar_action) private ImageButton playingAction;
    @BindView(R.id.playing_bar) private View playingBar;
    @BindView(R.id.player_view) private View playerView;
    @BindView(R.id.playing_list) private RecyclerView playingListView;
    @BindView(R.id.next) private ImageButton next;
    @BindView(R.id.prev) private ImageButton prev;
    @BindView(R.id.shuffle) private ImageButton shuffle;
    @BindView(R.id.repeat) private ImageButton repeat;
    @BindView(R.id.art) private ImageView playingArt;
    @BindView(R.id.control_holder) private View controlHolder;

    private Unbinder unbinder;
    private PlayPauseDrawable playPauseDrawable;
    private PlayPauseDrawable miniPlayPauseDrawable;
    private SlidingUpPanelLayout slidingUpPanelLayout;
    private MusicService service;
    private MainActivity activity;
    private Context context;

    private boolean darkMode;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        darkMode = sharedPrefs.getBoolean("dark_theme", false);
        View view = inflater.inflate(R.layout.fragment_player, container, false);
        unbinder = ButterKnife.bind(this, view);
        activity = (MainActivity) getActivity();
        context = getContext();

        setControls();
        setStyle();

        slidingUpPanelLayout = activity.getSlidingPanel();
        slidingUpPanelLayout.setScrollableView(playingListView);
        slidingUpPanelLayout.addPanelSlideListener(new SlidingUpPanelLayout.PanelSlideListener() {
            @Override
            public void onPanelSlide(View panel, float slideOffset) {
                if(playingBar != null) playingBar.setAlpha(1 - slideOffset);
            }

            @Override
            public void onPanelStateChanged(View panel, SlidingUpPanelLayout.PanelState previousState, SlidingUpPanelLayout.PanelState newState) {
                if(newState == SlidingUpPanelLayout.PanelState.EXPANDED) {
                    if(playingBar != null) playingBar.setVisibility(View.GONE);
                } else if(newState != SlidingUpPanelLayout.PanelState.EXPANDED) {
                    if(playingBar != null) playingBar.setVisibility(View.VISIBLE);
                }
            }
        });
        return view;
    }

    public void updatePlayer() {
        if(service == null) service = activity.getService();
        if(service.getCurrentSong() != null) {
            setPlayingList();
            setArt();
            updatePlayState();
            slidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
        }
    }

    private void updatePlayState() {
        setPlayingState(service.isPlaying());
    }

    private void setPlayingList() {
        if(service != null) {
            ArrayList<Song> playingList = service.getPlayingList();
            LinearLayoutManager layoutManager = new LinearLayoutManager(context);
            layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
            layoutManager.scrollToPosition(0);
            playingListView.setLayoutManager(layoutManager);
            if(playingList.size() > 0) {
                playingListView.setAdapter(new PlayingSongAdapter(context, playingList, darkMode, service.getCurrentSong(), Glide.with(getContext())));
            }
        }
    }

    private void setControls() {
        playPauseDrawable = new PlayPauseDrawable(activity);
        miniPlayPauseDrawable = new PlayPauseDrawable(activity);
        playingAction.setImageDrawable(miniPlayPauseDrawable);
        playingAction.setOnClickListener(playPauseClick());
        play.setImageDrawable(playPauseDrawable);
        play.setColorFilter(ContextCompat.getColor(context, R.color.colorAccent));
        play.setOnClickListener(playPauseClick());
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(service == null) service = activity.getService();
                if(service != null) service.next();
            }
        });
        prev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(service == null) service = activity.getService();
                if(service != null) service.prev();
            }
        });
        shuffle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(service == null) service = activity.getService();
                if(service != null) {
                    boolean shuffleState = service.shuffle();
                    shuffle.setAlpha(shuffleState
                            ? Config.BUTTON_ACTIVE
                            : Config.BUTTON_INACTIVE);
                }
            }
        });
        repeat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(service == null) service = activity.getService();
                if(service != null) {
                    service.repeat();
                }
            }
        });
    }

    private void setArt() {
        new Action<String>() {

            @NonNull
            @Override
            public String id() {
                return "get-playing-art";
            }

            @Nullable
            @Override
            protected String run() throws InterruptedException {
                if(service != null && service.getCurrentSong() != null) {
                    long albumId = service.getCurrentSong().getAlbumId();
                    return Utils.getAlbumArt(context, albumId);
                }
                return null;
            }

            @Override
            protected void done(String result) {
                Glide.with(context)
                        .load(result)
                        .asBitmap()
                        .transcode(new PaletteBitmapTranscoder(activity), PaletteBitmap.class)
                        .centerCrop()
                        .error(R.drawable.default_art)
                        .into(new ImageViewTarget<PaletteBitmap>(playingArt) {
                            @Override
                            protected void setResource(PaletteBitmap resource) {
                                super.view.setImageBitmap(resource.bitmap);
                                Palette palette = resource.palette;
                                Palette.Swatch swatch;
                                if(palette.getVibrantSwatch() != null) {
                                    swatch = palette.getVibrantSwatch();
                                    controlHolder.setBackgroundColor(swatch.getRgb());
                                    play.setColorFilter(swatch.getRgb());
                                } else if(palette.getDominantSwatch() != null) {
                                    swatch = palette.getDominantSwatch();
                                    controlHolder.setBackgroundColor(swatch.getRgb());
                                    play.setColorFilter(swatch.getRgb());
                                }
                            }
                        });
            }
        }.execute();
    }

    private void setStyle() {
        Utils.setWindowColor(playerView, context, darkMode);
        Utils.setContentColor(playingBar, context, darkMode);
        playingAction.setColorFilter(darkMode
                ? ContextCompat.getColor(context, R.color.primaryTextDark)
                : ContextCompat.getColor(context, R.color.primaryTextLight));
    }

    private View.OnClickListener playPauseClick() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(service == null) service = activity.getService();
                if(service != null) {
                    boolean isPlaying = service.togglePlay();
                    setPlayingState(isPlaying);
                }
            }
        };
    }

    private void setPlayingState(boolean isPlaying) {
        if(isPlaying) {
            playPauseDrawable.setPause();
            miniPlayPauseDrawable.setPause();
        } else {
            playPauseDrawable.setPlay();
            miniPlayPauseDrawable.setPlay();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

}
