package com.mad.musicplayer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements MediaPlayer.OnPreparedListener {

    private ArrayList<Song> songs;
    private Song currentSong;
    private TextView songTitle, songArtist, songAlbum, textCoordinates, textLocationName;
    private Button resumePause, randomSelect;
    private SeekBar volumeBar;

    private FusedLocationProviderClient client;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    private Geocoder geocoder;

    private double latitude = 0;
    private double longitude = 0;

    private MediaPlayer mediaPlayer;

    @SuppressLint("Range")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        songTitle = findViewById(R.id.songTitle);
        songArtist = findViewById(R.id.songArtist);
        songAlbum = findViewById(R.id.songAlbum);
        textCoordinates = findViewById(R.id.textCoordinates);
        textLocationName = findViewById(R.id.textLocationName);

        resumePause = findViewById(R.id.buttonResumePlay);
        randomSelect = findViewById(R.id.buttonRandom);

        volumeBar = findViewById(R.id.volumeBar);

        resumePause.setEnabled(false);

        client = LocationServices.getFusedLocationProviderClient(this);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            return;
        }

        geocoder = new Geocoder(this, Locale.getDefault());

        locationRequest = LocationRequest.create();
        locationRequest.setInterval(10000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                if (locationResult == null) return;

                for (Location location : locationResult.getLocations()) {
                    latitude = location.getLatitude();
                    longitude = location.getLongitude();

                    if (currentSong != null) {
                        float[] distance = new float[1];
                        Location.distanceBetween(latitude, longitude, currentSong.getLatitude(), currentSong.getLongitude(), distance);
                        Log.e("Distance", String.valueOf(distance[0]));
                        if (distance[0] > 1000) {
                            mediaPlayer.pause();
                        } else if (!mediaPlayer.isPlaying()) {
                            mediaPlayer.start();
                        }
                    }
                }
            }
        };


        client.getLastLocation().addOnSuccessListener(this, location -> {
            if (location != null) {
                String text = location.getLatitude() + " : " + location.getLongitude();
                Log.d("Location", text);
            } else {
                Log.d("Location", "Location is null");
            }
        });
        songs = new ArrayList<>();

        loadAudioFiles();

        if (currentSong == null && songs.size() > 0) {
            currentSong = songs.get(0);
        }
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        try {
            mediaPlayer.setDataSource(currentSong.getPath());
            mediaPlayer.setLooping(true);
            mediaPlayer.setOnPreparedListener(this);
            mediaPlayer.prepareAsync();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        volumeBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int volume, boolean fromUser) {
                float newVolume = (float) (Math.log(100 - volume) / Math.log(100));
                mediaPlayer.setVolume(1 - newVolume, 1 - newVolume);
                Log.d("Volume", String.valueOf(volume));
                Log.d("Volume", String.valueOf(1 - newVolume));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                Log.d("Volume", "Start");
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                Log.d("Volume", "Stop");
            }
        });


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

                songs.add(new Song(title, artist, album, data, 0, 0));

            } while (cursor.moveToNext());
        }
        if (cursor != null) {
            cursor.close();
        }
    }

    public void randomSelect(View view) {
        if (songs.size() > 0) {
            int randomIndex = (int) (Math.random() * songs.size());
            currentSong = songs.get(randomIndex);

            currentSong.setLatitude(latitude);
            currentSong.setLongitude(longitude);

            songTitle.setText(currentSong.getTitle());
            songArtist.setText(currentSong.getArtist());
            songAlbum.setText(currentSong.getAlbum());

            textCoordinates.setText(String.format(Locale.getDefault(), "%.2f : %.2f", latitude, longitude));
            textLocationName.setText(getAddressForLocation(new Location("random") {{
                setLatitude(latitude);
                setLongitude(longitude);
            }}));

            try {
                mediaPlayer.pause();
                mediaPlayer.reset();
                mediaPlayer.setLooping(true);
                mediaPlayer.setDataSource(currentSong.getPath());
                mediaPlayer.setOnPreparedListener(this);
                mediaPlayer.prepareAsync();


                if (!mediaPlayer.isPlaying()) {
                    mediaPlayer.start();
                    resumePause.setText("Resume");
                } else {
                    mediaPlayer.pause();
                    resumePause.setText("Pause");
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void resumePause(View view) {
        if (!mediaPlayer.isPlaying()) {
            mediaPlayer.start();
            ((Button) view).setText("Pause");
        } else {
            mediaPlayer.pause();
            ((Button) view).setText("Resume");
        }
    }

    @SuppressLint("MissingPermission")
    @Override
    protected void onResume() {
        super.onResume();

        client.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
        );

        SharedPreferences sharedPreferences = getPreferences(Context.MODE_PRIVATE);
        String path = sharedPreferences.getString("path", null);
        String latitudeString = sharedPreferences.getString("latitude", null);
        String longitudeString = sharedPreferences.getString("longitude", null);

        if (path != null) {
            loadAudioFiles();
            songs.forEach(song -> {
                if (song.getPath().equals(path)) {
                    currentSong = song;
                    currentSong.setLatitude(Double.parseDouble(latitudeString == null ? "0" : latitudeString));
                    currentSong.setLongitude(Double.parseDouble(longitudeString == null ? "0" : longitudeString));
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

        client.removeLocationUpdates(locationCallback);
        SharedPreferences sharedPreferences = getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("path", currentSong.getPath());
        editor.putString("latitude", String.valueOf(currentSong.getLatitude()));
        editor.putString("longitude", String.valueOf(currentSong.getLongitude()));
        editor.apply();
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        resumePause.setEnabled(true);
    }

    private String getAddressForLocation(Location location) {
        List<Address> addresses;

        try {
            addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
        } catch (IOException e) {
            e.printStackTrace();
            return "<IO error when connecting to geocoder>";
        }

        if (addresses == null || addresses.size() == 0) {
            return "<no info on location";
        }

        Address address = addresses.get(0);
        return address.getAddressLine(0);
    }
}