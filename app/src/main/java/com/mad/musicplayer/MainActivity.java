package com.mad.musicplayer;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private ArrayList<Song> songs;
    private Song currentSong;
    private TextView songTitle, songArtist, songAlbum;

    private boolean isPlaying;

    @SuppressLint("Range")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        songs = new ArrayList<>();

        isPlaying = false;

        loadAudioFiles();

        songTitle = findViewById(R.id.songTitle);
        songArtist = findViewById(R.id.songArtist);
        songAlbum = findViewById(R.id.songAlbum);
    }

    private void loadAudioFiles() {
        ContentResolver contentResolver = getContentResolver();
        Uri uri = MediaStore.Audio.Media.INTERNAL_CONTENT_URI;
        String[] projection = {
                MediaStore.Audio.AudioColumns.DATA,
                MediaStore.Audio.AudioColumns.TITLE,
                MediaStore.Audio.AudioColumns.ALBUM,
                MediaStore.Audio.ArtistColumns.ARTIST
        };
        Cursor cursor = contentResolver.query(uri, projection, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            do {
                @SuppressLint("Range") String data = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
                @SuppressLint("Range") String title = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
                @SuppressLint("Range") String album = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM));
                @SuppressLint("Range") String artist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
//                @SuppressLint("Range") String duration = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION));

                songs.add(new Song(title, artist, album, "duration", data));

            } while (cursor.moveToNext());
        }
        if (cursor != null) {
            cursor.close();
        }
    }

    public void randomSelect(View view) {
        if(songs.size() > 0) {
            int randomIndex = (int) (Math.random() * songs.size());
            currentSong = songs.get(randomIndex);

            songTitle.setText(currentSong.getTitle());
            songArtist.setText(currentSong.getArtist());
            songAlbum.setText(currentSong.getAlbum());

            songTitle.setText(currentSong.getTitle());
            songArtist.setText(currentSong.getArtist());
            songAlbum.setText(currentSong.getAlbum());
        }
    }

    public void resumePause(View view) {
        if (isPlaying) {
            isPlaying = false;
            ((Button) view).setText("Resume");
        } else {
            isPlaying = true;
            ((Button) view).setText("Pause");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        SharedPreferences sharedPreferences = getPreferences(Context.MODE_PRIVATE);
        String path = sharedPreferences.getString("path", null);

        if(path != null){
            loadAudioFiles();
            songs.forEach(song -> {
                if(song.getPath().equals(path)){
                    currentSong = song;
                    songTitle.setText(currentSong.getTitle());
                    songArtist.setText(currentSong.getArtist());
                    songAlbum.setText(currentSong.getAlbum());
                }
            });
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        SharedPreferences sharedPreferences = getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("path", currentSong.getPath());
        editor.apply();
    }
}