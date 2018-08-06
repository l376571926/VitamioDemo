package group.tonight.mynetvideoplayer;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import io.vov.vitamio.MediaPlayer;
import io.vov.vitamio.widget.MediaController;
import io.vov.vitamio.widget.VideoView;

public class PlayActivity extends AppCompatActivity {
    private static final String TAG = PlayActivity.class.getSimpleName();
    private VideoView mVideoView;
    private int mPosition;
    private MediaController mMediaController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);

        mVideoView = (VideoView) findViewById(R.id.surface_view);

        String path = "http://192.168.1.121:8080/%E7%94%B5%E8%A7%86%E5%89%A7/%E7%A5%9E%E8%AF%9D/[%E8%BF%85%E6%92%AD%E5%BD%B1%E9%99%A2www.XunBo.Cc]%E7%A5%9E%E8%AF%9D40.%E5%9B%BD%E8%AF%AD%E4%B8%AD%E5%AD%97.dvd.rmvb";
        if (getIntent().hasExtra("videoUrl")) {
            path = getIntent().getStringExtra("videoUrl");
        }
        if (getIntent().hasExtra("position")) {
            mPosition = getIntent().getIntExtra("position", -1);
        }
        mVideoView.setVideoPath(path);

        mVideoView.requestFocus();
        mVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mp.setPlaybackSpeed(1.0f);
                mp.setOnVideoSizeChangedListener(new MediaPlayer.OnVideoSizeChangedListener() {
                    @Override
                    public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
                        mMediaController = new MediaController(PlayActivity.this);
                        mVideoView.setMediaController(mMediaController);
                        mMediaController.setAnchorView(mVideoView);
                    }
                });
            }
        });
        mVideoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                Toast.makeText(PlayActivity.this, "播放完了", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
