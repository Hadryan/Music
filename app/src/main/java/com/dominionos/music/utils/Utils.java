package com.dominionos.music.utils;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.provider.MediaStore;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.dominionos.music.R;
import com.dominionos.music.adapters.DialogPlaylistAdapter;
import com.dominionos.music.items.Playlist;
import com.dominionos.music.items.Song;

import java.util.List;

public class Utils {

    public static void setPrimaryTextColor(TextView view, Context context, boolean darkMode) {
        view.setTextColor(darkMode
                ? ContextCompat.getColor(context, R.color.primaryTextDark)
                : ContextCompat.getColor(context, R.color.primaryTextLight));
    }

    public static void setSecondaryTextColor(TextView view, Context context, boolean darkMode) {
        view.setTextColor(darkMode
                ? ContextCompat.getColor(context, R.color.secondaryTextDark)
                : ContextCompat.getColor(context, R.color.secondaryTextLight));
    }

    public static void setWindowColor(View view, Context context, boolean darkMode) {
        view.setBackgroundColor(darkMode
                ? ContextCompat.getColor(context, R.color.darkWindowBackground)
                : ContextCompat.getColor(context, R.color.windowBackground));
    }

    public static void setContentColor(View view, Context context, boolean darkMode) {
        view.setBackgroundColor(darkMode
                ? ContextCompat.getColor(context, R.color.darkContentColour)
                : ContextCompat.getColor(context, R.color.contentColour));
    }

    public static int getAutoStatColor(int baseColor) {
        float[] hsv = new float[3];
        Color.colorToHSV(baseColor, hsv);
        hsv[2] *= 0.8f;
        return Color.HSVToColor(hsv);
    }

    public static String getAlbumArt(Context context, long albumId) {
        Cursor cursor = context.getContentResolver().query(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
                new String[]{MediaStore.Audio.Albums._ID, MediaStore.Audio.Albums.ALBUM_ART},
                MediaStore.Audio.Albums._ID + "=?",
                new String[]{String.valueOf(albumId)},
                null);
        String imagePath = "";
        if (cursor != null && cursor.moveToFirst()) {
            imagePath = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Albums.ALBUM_ART));
            cursor.close();
        }
        return imagePath;
    }

    public static void addToPlaylistDialog(Context context, Song position) {
        LinearLayoutManager layoutManager = new LinearLayoutManager(context);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        layoutManager.scrollToPosition(0);
        MySQLiteHelper sqLiteHelper = new MySQLiteHelper(context);
        List<Playlist> playlist = sqLiteHelper.getAllPlaylist();
        playlist.add(new Playlist(-1, context.getString(R.string.create_new_playlist)));
        new MaterialDialog.Builder(context)
                .title(context.getString(R.string.add_to_playlist))
                .positiveText(context.getString(R.string.done))
                .adapter(new DialogPlaylistAdapter(context, playlist, position), layoutManager)
                .show();
    }

    public static int dpToPx(Context context, int dp) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        return Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
    }

    public static int calculateNoOfColumns(Context context) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        float dpWidth = displayMetrics.widthPixels / displayMetrics.density;
        return (int) (dpWidth / Config.ALBUM_CARD_WIDTH);
    }
}
