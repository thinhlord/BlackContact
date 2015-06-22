package vn.savvycom.blackcontact;

import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.Toast;

import java.io.IOException;

public class MusicService extends Service implements MediaPlayer.OnCompletionListener,
        MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener {
    MediaPlayer mediaPlayer;
    public static final String url = "http://programmerguru.com/android-tutorial/wp-content/uploads/2013/04/hosannatelugu.mp3";
    public static final String BROADCAST = "MusicService";
    public static final String START = "start";

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mediaPlayer.setOnPreparedListener(this);
        mediaPlayer.setOnErrorListener(this);
        try {
            mediaPlayer.setDataSource(url);
        } catch (IOException e) {
            e.printStackTrace();
        }
        mediaPlayer.setOnCompletionListener(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (!mediaPlayer.isPlaying()) {
            Toast.makeText(getApplicationContext(), "Connecting", Toast.LENGTH_LONG).show();
            mediaPlayer.prepareAsync();
        }
        return START_STICKY;
    }

    public void onDestroy() {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
        }
        mediaPlayer.release();
    }

    public void onCompletion(MediaPlayer _mediaPlayer) {
        stopSelf();
    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        Toast.makeText(getApplicationContext(), "Connected", Toast.LENGTH_LONG).show();
        mediaPlayer.start();
        sendBroadcast(true);
    }

    @Override
    public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
        sendBroadcast(false);
        return false;
    }

    public void sendBroadcast(boolean success) {
        Intent intentBroadcast = new Intent(BROADCAST);
        intentBroadcast.putExtra(START, success);
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intentBroadcast);
    }
}