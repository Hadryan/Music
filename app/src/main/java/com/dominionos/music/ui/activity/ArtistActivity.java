package com.dominionos.music.ui.activity;


import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.bumptech.glide.Glide;
import com.dominionos.music.R;
import com.dominionos.music.adapters.SongsAdapter;
import com.dominionos.music.items.Song;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class ArtistActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        boolean darkMode = sharedPrefs.getBoolean("dark_theme", false);

        setTheme(darkMode ? R.style.AppTheme_Dark : R.style.AppTheme_Main);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_artist);
        String artistName = getIntent().getStringExtra("artistName");

        Toolbar toolbar = (Toolbar) findViewById(R.id.artist_toolbar);
        setSupportActionBar(toolbar);
        if(getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(artistName);
        }

        RecyclerView rv = (RecyclerView) findViewById(R.id.artist_recycler_view);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        layoutManager.scrollToPosition(0);
        rv.setLayoutManager(layoutManager);

        final ArrayList<Song> songList = new ArrayList<>();
        final String where = MediaStore.Audio.Media.IS_MUSIC + "=1";
        final String orderBy = MediaStore.Audio.Media.TITLE;
        Cursor musicCursor = getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                null, where, null, orderBy);
        if (musicCursor != null && musicCursor.moveToFirst()) {
            int titleColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media.TITLE);
            int idColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media._ID);
            int artistColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media.ARTIST);
            int pathColumn = musicCursor.getColumnIndex
                    (MediaStore.Audio.Media.DATA);
            int albumIdColumn = musicCursor.getColumnIndex
                    (MediaStore.Audio.Media.ALBUM_ID);
            int albumColumn = musicCursor.getColumnIndex
                    (MediaStore.Audio.Media.ALBUM);
            do {
                if(artistName.equals(musicCursor.getString(artistColumn))) {
                    songList.add(new Song(musicCursor.getLong(idColumn),
                            musicCursor.getString(titleColumn),
                            musicCursor.getString(artistColumn),
                            musicCursor.getString(pathColumn), false,
                            musicCursor.getLong(albumIdColumn),
                            musicCursor.getString(albumColumn)));
                }
            }
            while (musicCursor.moveToNext());
            Collections.sort(songList, new Comparator<Song>() {
                @Override
                public int compare(Song song, Song t1) {
                    return song.getName().compareTo(t1.getName());
                }
            });

            rv.setBackgroundColor(darkMode
                    ? ContextCompat.getColor(this, R.color.darkWindowBackground)
                    : ContextCompat.getColor(this, R.color.windowBackground));
            rv.setAdapter(new SongsAdapter(this, songList, darkMode, Glide.with(this), false));
        }
        if (musicCursor != null) {
            musicCursor.close();
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}
