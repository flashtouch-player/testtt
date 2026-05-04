package com.miapp.exoplayer;

import android.app.Activity;
import android.app.PictureInPictureParams;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Rational;
import android.view.WindowManager;

import androidx.annotation.OptIn;
import androidx.media3.common.C;
import androidx.media3.common.MediaItem;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.exoplayer.dash.DashMediaSource;
import androidx.media3.exoplayer.drm.DefaultDrmSessionManagerProvider;
import androidx.media3.exoplayer.hls.HlsMediaSource;
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory;
import androidx.media3.exoplayer.source.MediaSource;
import androidx.media3.exoplayer.source.ProgressiveMediaSource;
import androidx.media3.datasource.DefaultHttpDataSource;
import androidx.media3.ui.PlayerView;

@OptIn(markerClass = UnstableApi.class)
public class PlayerActivity extends Activity {

    private ExoPlayer player;
    private PlayerView playerView;
    private BroadcastReceiver receiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        playerView = new PlayerView(this);
        playerView.setUseController(true);
        setContentView(playerView);

        String url           = getIntent().getStringExtra("url");
        String type          = getIntent().getStringExtra("type");
        String drmLicenseUrl = getIntent().getStringExtra("drmLicenseUrl");
        String drmScheme     = getIntent().getStringExtra("drmScheme");

        DefaultHttpDataSource.Factory httpFactory =
                new DefaultHttpDataSource.Factory().setAllowCrossProtocolRedirects(true);

        DefaultMediaSourceFactory mediaSourceFactory = new DefaultMediaSourceFactory(this)
                .setDataSourceFactory(httpFactory);

        MediaItem.Builder itemBuilder = new MediaItem.Builder().setUri(Uri.parse(url));
        if (drmLicenseUrl != null && !drmLicenseUrl.isEmpty()) {
            java.util.UUID scheme = "playready".equalsIgnoreCase(drmScheme)
                    ? C.PLAYREADY_UUID : C.WIDEVINE_UUID;
            itemBuilder.setDrmConfiguration(
                new MediaItem.DrmConfiguration.Builder(scheme)
                    .setLicenseUri(drmLicenseUrl)
                    .build());
            mediaSourceFactory.setDrmSessionManagerProvider(new DefaultDrmSessionManagerProvider());
        }
        MediaItem item = itemBuilder.build();

        MediaSource source;
        switch (type == null ? "auto" : type) {
            case "hls":
                source = new HlsMediaSource.Factory(httpFactory).createMediaSource(item); break;
            case "dash":
                source = new DashMediaSource.Factory(httpFactory).createMediaSource(item); break;
            case "progressive":
                source = new ProgressiveMediaSource.Factory(httpFactory).createMediaSource(item); break;
            default:
                source = mediaSourceFactory.createMediaSource(item);
        }

        player = new ExoPlayer.Builder(this).build();
        playerView.setPlayer(player);
        player.setMediaSource(source);
        player.prepare();
        player.setPlayWhenReady(true);

        receiver = new BroadcastReceiver() {
            @Override public void onReceive(Context c, Intent i) {
                String a = i.getAction();
                if (a == null) return;
                if (a.endsWith("STOP"))   { finish(); }
                if (a.endsWith("PAUSE"))  { if (player != null) player.pause(); }
                if (a.endsWith("RESUME")) { if (player != null) player.play(); }
                if (a.endsWith("PIP"))    { enterPip(); }
            }
        };
        IntentFilter f = new IntentFilter();
        f.addAction("com.miapp.exoplayer.STOP");
        f.addAction("com.miapp.exoplayer.PAUSE");
        f.addAction("com.miapp.exoplayer.RESUME");
        f.addAction("com.miapp.exoplayer.PIP");
        if (Build.VERSION.SDK_INT >= 33) {
            registerReceiver(receiver, f, Context.RECEIVER_NOT_EXPORTED);
        } else {
            registerReceiver(receiver, f);
        }
    }

    private void enterPip() {
        if (Build.VERSION.SDK_INT >= 26) {
            enterPictureInPictureMode(
                new PictureInPictureParams.Builder()
                    .setAspectRatio(new Rational(16, 9)).build());
        }
    }

    @Override protected void onDestroy() {
        super.onDestroy();
        try { unregisterReceiver(receiver); } catch (Exception ignored) {}
        if (player != null) { player.release(); player = null; }
    }
}
