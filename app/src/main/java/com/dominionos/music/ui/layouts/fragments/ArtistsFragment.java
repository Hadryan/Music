package com.dominionos.music.ui.layouts.fragments;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.dominionos.music.R;
import com.dominionos.music.utils.adapters.ArtistAdapter;
import com.dominionos.music.utils.items.ArtistListItem;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class ArtistsFragment extends Fragment {

    private View mainView;
    private FastScrollRecyclerView rv;
    private Context context;
    private boolean darkMode;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_artists, container, false);
        context = v.getContext();
        this.mainView = v;

        darkMode = getArguments().getBoolean("dark_theme", false);

        init();

        getArtistList();

        return v;
    }

    private void init() {
        rv = (FastScrollRecyclerView) mainView.findViewById(R.id.artist_list);
    }

    private void getArtistList() {
        ArrayList<ArtistListItem> artistList = new ArrayList<>();
        System.gc();
        final String orderBy = MediaStore.Audio.Artists.ARTIST;
        Cursor musicCursor = context.getContentResolver().
                query(MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI, null, null, null, orderBy);

        if (musicCursor != null && musicCursor.moveToFirst()) {
            int titleColumn = musicCursor.getColumnIndex
                    (MediaStore.Audio.Artists.ARTIST);
            int idColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Artists._ID);
            int numOfAlbumsColumn = musicCursor.getColumnIndex
                    (MediaStore.Audio.Artists.NUMBER_OF_ALBUMS);
            int numOfTracksColumn = musicCursor.getColumnIndex
                    (MediaStore.Audio.Artists.NUMBER_OF_TRACKS);
            do {
                artistList.add(new ArtistListItem(
                        musicCursor.getLong(idColumn),
                        musicCursor.getString(titleColumn),
                        musicCursor.getInt(numOfTracksColumn),
                        musicCursor.getInt(numOfAlbumsColumn)));
            }
            while (musicCursor.moveToNext());
        }
        Collections.sort(artistList, new Comparator<ArtistListItem>() {
            @Override
            public int compare(ArtistListItem artistListItem, ArtistListItem t1) {
                return artistListItem.getName().compareToIgnoreCase(t1.getName());
            }
        });
        if (musicCursor != null) {
            musicCursor.close();
        }

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(mainView.getContext());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        linearLayoutManager.scrollToPosition(0);
        rv.setLayoutManager(linearLayoutManager);

        if(darkMode) {
            rv.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.darkWindowBackground));
        }

        if(artistList.size() != 0) {
            rv.setAdapter(new ArtistAdapter(context, artistList, darkMode));
        } else {
            getActivity().findViewById(R.id.no_artists).setVisibility(View.VISIBLE);
        }
    }
}