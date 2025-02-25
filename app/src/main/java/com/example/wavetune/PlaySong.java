package com.example.wavetune;

import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.File;
import java.util.ArrayList;

public class PlaySong extends AppCompatActivity {

    TextView currTime, duration, songName;
    ImageView play, prev, next;
    SeekBar seekBar;
    ArrayList<File> songs;
    MediaPlayer mediaPlayer;
    int position;
    String textContent, songDuration;
    Thread updateSeek;
    int durationInMillis, minutes, seconds;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_play_song);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        songName = findViewById(R.id.songName);
        currTime = findViewById(R.id.currTime);
        duration = findViewById(R.id.duration);
        play = findViewById(R.id.play);
        prev = findViewById(R.id.prev);
        next = findViewById(R.id.next);
        seekBar = findViewById(R.id.seekBar);

        songName.setSelected(true);

        Intent intent = getIntent();
        songs = (ArrayList) intent.getParcelableArrayListExtra("songList");
        textContent = intent.getStringExtra("currSong").replace(".mp3", "");
        songName.setText(textContent);
        position = intent.getIntExtra("position", 0);

        MediaPlayerSingleton.playSong(this, songs, position);
        mediaPlayer = MediaPlayerSingleton.getMediaPlayer();

        int durationInMillis = mediaPlayer.getDuration();
        int minutes = (durationInMillis / 1000) / 60;
        int seconds = (durationInMillis / 1000) % 60;
        String songDuration = String.format("%02d:%02d", minutes, seconds);
        duration.setText(songDuration);

        seekBar.setMax(mediaPlayer.getDuration());
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    mediaPlayer.seekTo(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                mediaPlayer.pause();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mediaPlayer.seekTo(seekBar.getProgress());
                mediaPlayer.start();
            }
        });
        updateSeek = new Thread() {
            @Override
            public void run() {
                int currPosition = 0;
                try {
                    while (currPosition < mediaPlayer.getDuration()) {
                        currPosition = mediaPlayer.getCurrentPosition();
                        seekBar.setProgress(currPosition);
                        String formattedTime = formatTime(currPosition);
                        int finalCurrPosition = currPosition;
                        runOnUiThread(() -> {
                            currTime.setText(formattedTime);
                            seekBar.setProgress(finalCurrPosition);
                        });
                        sleep(800);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        updateSeek.start();

        play.setOnClickListener(v -> {
            if (mediaPlayer.isPlaying()) {
                play.setImageResource(R.drawable.play);
                mediaPlayer.pause();
            } else {
                play.setImageResource(R.drawable.pause);
                mediaPlayer.start();
            }
        });

        prev.setOnClickListener(v -> {
            position = (position == 0) ? songs.size() - 1 : position - 1;
            MediaPlayerSingleton.playSong(this, songs, position);
            updateUI();
        });

        next.setOnClickListener(v -> {
            position = (position == songs.size() - 1) ? 0 : position + 1;
            MediaPlayerSingleton.playSong(this, songs, position);
            updateUI();
        });

        mediaPlayer.setOnCompletionListener(mv -> playNextSong());
    }

    private String formatTime(int millis) {
        int minutes = (millis / 1000) / 60;
        int seconds = (millis / 1000) % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }
    private void updateUI() {
        if (updateSeek != null) {
            updateSeek.interrupt();
        }
        mediaPlayer = MediaPlayerSingleton.getMediaPlayer();
        seekBar.setMax(mediaPlayer.getDuration());

        textContent = songs.get(position).getName().replace(".mp3", "");
        songName.setText(textContent);
        play.setImageResource(R.drawable.pause);

        durationInMillis = mediaPlayer.getDuration();
        minutes = (durationInMillis / 1000) / 60;
        seconds = (durationInMillis / 1000) % 60;
        songDuration = String.format("%02d:%02d", minutes, seconds);
        duration.setText(songDuration);

        startSeekBarUpdate();
    }
    private void startSeekBarUpdate() {
        updateSeek = new Thread() {
            @Override
            public void run() {
                try {
                    while (mediaPlayer != null && mediaPlayer.isPlaying()) {
                        int currPosition = mediaPlayer.getCurrentPosition();
                        String formattedTime = formatTime(currPosition);
                        runOnUiThread(() -> {
                            currTime.setText(formattedTime);
                            seekBar.setProgress(currPosition);
                        });
                        Thread.sleep(800);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        updateSeek.start();
    }
    public void playNextSong() {
        position = (position == songs.size() - 1) ? 0 : position + 1;

        MediaPlayerSingleton.playSong(this, songs, position);
        updateSeek.interrupt();
        updateUI();

    }

}