package com.mad.musicplayer;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.PackageManagerCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
public class MainActivity extends AppCompatActivity {

    private Button random, playPause;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        random = (Button) findViewById(R.id.buttonRandom);
        playPause = (Button) findViewById(R.id.buttonResumePlay);

        String[] projection = {MediaStore.Audio.Media._ID, MediaStore.Audio.Media.TITLE, MediaStore.Audio.Media.ARTIST, MediaStore.Audio.Media.ALBUM};
        Cursor cursor = getContentResolver().query(MediaStore.Audio.Media.INTERNAL_CONTENT_URI, projection, null, null, null);

        if (cursor != null) {
            int titleIndex = cursor.getColumnIndex(MediaStore.Audio.Media.TITLE);
            int artistIndex = cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST);
            int albumIndex = cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM);

            while (cursor.moveToNext()) {
                String title = cursor.getString(titleIndex);
                String artist = cursor.getString(artistIndex);
                String album = cursor.getString(albumIndex);


            }
            cursor.close();
        }

    }

    public void randomSelect(View view) {
    }

    public void resumePause(View view) {
    }
}