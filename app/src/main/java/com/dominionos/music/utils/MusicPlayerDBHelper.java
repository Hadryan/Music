package com.dominionos.music.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.dominionos.music.utils.items.SongListItem;

import java.util.ArrayList;
import java.util.Collections;

public class MusicPlayerDBHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "MusicPlayingDB";

    public MusicPlayerDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_PLAYBACK_TABLE = "CREATE TABLE playback (" +
                "song_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "song_real_id INTEGER," +
                "song_album_id INTEGER," +
                "song_desc TEXT," +
                "song_fav INTEGER," +
                "song_path TEXT," +
                "song_name TEXT," +
                "song_count INTEGER," +
                "song_album_name TEXT," +
                "song_mood TEXT," +
                "song_playing INTEGER)";
        db.execSQL(CREATE_PLAYBACK_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS playback");
        this.onCreate(db);
    }

    private static final String TABLE_PLAYBACK = "playback";

    private static final String SONG_KEY_ID = "song_id";
    private static final String SONG_KEY_REAL_ID = "song_real_id";
    private static final String SONG_KEY_ALBUMID = "song_album_id";
    private static final String SONG_KEY_DESC = "song_desc";
    private static final String SONG_KEY_FAV = "song_fav";
    private static final String SONG_KEY_PATH = "song_path";
    private static final String SONG_KEY_NAME = "song_name";
    private static final String SONG_KEY_ALBUM_NAME = "song_album_name";
    private static final String SONG_KEY_PLAYING = "song_playing";

    public void addSong(SongListItem song) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.putNull(SONG_KEY_ID);
        values.put(SONG_KEY_REAL_ID, (int) song.getId());
        values.put(SONG_KEY_ALBUMID, song.getAlbumId());
        values.put(SONG_KEY_DESC, song.getDesc());
        values.put(SONG_KEY_FAV, song.getFav());
        values.put(SONG_KEY_PATH, song.getPath());
        values.put(SONG_KEY_NAME, song.getName());
        values.put(SONG_KEY_ALBUM_NAME, song.getAlbumName());

        db.insert(TABLE_PLAYBACK, null, values);
        db.close();
    }

    public void removeSong(int pos) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM " + TABLE_PLAYBACK + " WHERE " +
                SONG_KEY_REAL_ID + "='" + pos + "'");
    }

    public void shuffleRows() {
        ArrayList<SongListItem> songs = getCurrentPlayingList();
        Collections.shuffle(songs);
        clearPlayingList();
        addSongs(songs);
    }

    public SongListItem getNextSong(int currentId) {
        String query = "SELECT * FROM " + TABLE_PLAYBACK;
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(query, null);
        SongListItem firstSong = null;
        if (cursor.moveToFirst()) {
            boolean isNext = false, isFirst = true;
            do {
                if (isFirst) {
                    firstSong = getSongFromCursor(cursor);
                    isFirst = false;
                } else if (isNext)
                    return getSongFromCursor(cursor);
                if (cursor.getString(1).matches(String.valueOf(currentId))) {
                    isNext = true;
                }
            } while (cursor.moveToNext());
        }
        db.close();
        return firstSong;
    }

    public SongListItem getPrevSong(int currentId) {
        String query = "SELECT * FROM " + TABLE_PLAYBACK;
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(query, null);
        boolean isFirst = true;
        int counter = 0;
        ArrayList<SongListItem> songs = new ArrayList<>();
        if (cursor.moveToFirst()) {
            do {
                if (isFirst) {
                    isFirst = false;
                } else if (cursor.getString(1).matches(String.valueOf(currentId))) {
                    return songs.get(counter - 1);
                }
                songs.add(getSongFromCursor(cursor));
                counter++;
            } while (cursor.moveToNext());
        }
        db.close();
        return songs.get(songs.size() - 1);
    }

    public SongListItem getFirstSong() {
        String query = "SELECT * FROM " + TABLE_PLAYBACK + " LIMIT 1";
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(query, null);
        if (cursor.moveToFirst()) {
            db.close();
            return getSongFromCursor(cursor);
        }
        db.close();
        return null;
    }

    public ArrayList<SongListItem> getCurrentPlayingList() {
        ArrayList<SongListItem> songs = new ArrayList<>();
        String query = "SELECT  * FROM " + TABLE_PLAYBACK;
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(query, null);
        SongListItem song;
        if (cursor.moveToFirst()) {
            do {
                song = getSongFromCursor(cursor);
                songs.add(song);
            } while (cursor.moveToNext());
        }
        db.close();
        return songs;
    }

    public int getPlaybackTableSize() {
        String query = "SELECT  * FROM " + TABLE_PLAYBACK;
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(query, null);
        int count = cursor.getCount();
        cursor.close();
        db.close();
        return count;
    }

    public void clearPlayingList() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM " + TABLE_PLAYBACK);
        db.close();
    }


    public void addSongs(ArrayList<SongListItem> playList) {
        SQLiteDatabase db = this.getWritableDatabase();
        for (int i = 0; i < playList.size(); i++) {
            SongListItem song = playList.get(i);
            ContentValues values = new ContentValues();
            Log.v("sname", song.getName());
            values.putNull(SONG_KEY_ID);
            values.put(SONG_KEY_REAL_ID, (int) song.getId());
            values.put(SONG_KEY_ALBUMID, song.getAlbumId());
            values.put(SONG_KEY_DESC, song.getDesc());
            values.put(SONG_KEY_FAV, song.getFav());
            values.put(SONG_KEY_PATH, song.getPath());
            values.put(SONG_KEY_NAME, song.getName());
            values.put(SONG_KEY_ALBUM_NAME, song.getAlbumName());
            values.put(SONG_KEY_PLAYING, 0);
            db.insert(TABLE_PLAYBACK, null, values);
        }
    }

    public SongListItem getSong(int playingPos) {
        String query = "SELECT  * FROM " + TABLE_PLAYBACK + " WHERE " +
                SONG_KEY_REAL_ID + "='" + playingPos + "'";
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(query, null);
        if (cursor.moveToFirst()) {
            return getSongFromCursor(cursor);
        }
        return null;
    }

    private SongListItem getSongFromCursor(Cursor cursor) {
        boolean fav;
        fav = !cursor.getString(4).matches("0");
        return new SongListItem(Long.valueOf(cursor.getString(1)),
                cursor.getString(6), cursor.getString(3),
                cursor.getString(5), fav, Long.parseLong(cursor.getString(2)),
                cursor.getString(8));
    }

}
