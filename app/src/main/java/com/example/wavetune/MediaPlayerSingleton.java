package com.example.wavetune;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;

public class MediaPlayerSingleton {
    private static MediaPlayer mediaPlayer;
    private static int currentPosition = -1;

    public static void playSong(Context context, ArrayList<File> songs, int position) {
        if (mediaPlayer != null) {
            if (position == currentPosition && mediaPlayer.isPlaying()) {
                // Same song is already playing, do nothing
                return;
            }
            // Stop and release current media player
            mediaPlayer.stop();
            mediaPlayer.release();
        }

        // Start new song
        Uri uri = Uri.parse(songs.get(position).toString());
        mediaPlayer = MediaPlayer.create(context, uri);
        mediaPlayer.start();
        currentPosition = position;
    }

    public static MediaPlayer getMediaPlayer() {
        return mediaPlayer;
    }

    public static void stopMediaPlayer() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
            currentPosition = -1;
        }
    }
}
