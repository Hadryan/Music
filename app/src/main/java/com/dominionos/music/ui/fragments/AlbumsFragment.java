package com.dominionos.music.ui.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.afollestad.async.Action;
import com.bumptech.glide.Glide;
import com.dominionos.music.R;
import com.dominionos.music.utils.SpacesItemDecoration;
import com.dominionos.music.utils.Utils;
import com.dominionos.music.adapters.AlbumsAdapter;
import com.dominionos.music.items.Album;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class AlbumsFragment extends Fragment {

    private FastScrollRecyclerView gv;
    private Context context;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_albums, container, false);

        context = getContext();
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        boolean darkMode = sharedPref.getBoolean("dark_theme", false);

        gv = (FastScrollRecyclerView) v.findViewById(R.id.album_grid);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(context, Utils.calculateNoOfColumns(context));
        gridLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        gridLayoutManager.scrollToPosition(0);
        gv.setLayoutManager(gridLayoutManager);
        gv.addItemDecoration(new SpacesItemDecoration());
        gv.setHasFixedSize(true);
        Utils.setWindowColor(gv, context, darkMode);

        getAlbumList();

        return v;
    }

    private void getAlbumList() {
        new Action<ArrayList<Album>>() {

            @NonNull
            @Override
            public String id() {
                return "album_list";
            }

            @Nullable
            @Override
            protected ArrayList<Album> run() throws InterruptedException {
                final ArrayList<Album> albumList = new ArrayList<>();
                final String orderBy = MediaStore.Audio.Albums.ALBUM;
                Cursor musicCursor = context.getContentResolver().
                        query(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI, null, null, null, orderBy);

                if (musicCursor != null && musicCursor.moveToFirst()) {
                    int titleColumn = musicCursor.getColumnIndex
                            (android.provider.MediaStore.Audio.Albums.ALBUM);
                    int idColumn = musicCursor.getColumnIndex
                            (android.provider.MediaStore.Audio.Albums._ID);
                    int artistColumn = musicCursor.getColumnIndex
                            (android.provider.MediaStore.Audio.Albums.ARTIST);
                    int numOfSongsColumn = musicCursor.getColumnIndex
                            (android.provider.MediaStore.Audio.Albums.NUMBER_OF_SONGS);
                    int albumArtColumn = musicCursor.getColumnIndex
                            (android.provider.MediaStore.Audio.Albums.ALBUM_ART);
                    do {
                        albumList.add(new Album(musicCursor.getLong(idColumn),
                                musicCursor.getString(titleColumn),
                                musicCursor.getString(artistColumn),
                                musicCursor.getString(albumArtColumn),
                                musicCursor.getInt(numOfSongsColumn)
                        ));
                    }
                    while (musicCursor.moveToNext());
                }
                Collections.sort(albumList, new Comparator<Album>() {
                    @Override
                    public int compare(Album album, Album t1) {
                        return album.getName().compareToIgnoreCase(t1.getName());
                    }
                });
                if (musicCursor != null) {
                    musicCursor.close();
                }
                return albumList;
            }

            @Override
            protected void done(ArrayList<Album> albumList) {
                if(albumList.size() != 0) {
                    gv.setAdapter(new AlbumsAdapter(context, albumList, Glide.with(context)));
                } else {
                    getActivity().findViewById(R.id.no_albums).setVisibility(View.VISIBLE);
                }
            }
        }.execute();
    }
}