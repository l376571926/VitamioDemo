package group.tonight.mynetvideoplayer;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.LinearLayout;

import io.vov.vitamio.MediaPlayer;
import io.vov.vitamio.Vitamio;
import io.vov.vitamio.widget.MediaController;
import io.vov.vitamio.widget.VideoView;

public class PlayActivity extends AppCompatActivity {
    private static final String TAG = PlayActivity.class.getSimpleName();
    private VideoView mVideoView;
    private int mPosition;
    private MediaController mMediaController;
    private ProgressDialog mDialog;
    private ViewGroup mVideoContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Vitamio.isInitialized(getApplicationContext());
        setContentView(R.layout.activity_play);
        Log.e(TAG, "onCreate: " + savedInstanceState);

        mVideoContainer = (ViewGroup) findViewById(R.id.view_container);
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
        mMediaController = new MediaController(PlayActivity.this, true, ((ViewGroup) mVideoView.getParent()));
        mMediaController.setVisibility(View.GONE);
        mVideoView.setMediaController(mMediaController);
        mVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mp.setPlaybackSpeed(1.0f);
                mVideoView.setVideoLayout(VideoView.VIDEO_LAYOUT_SCALE, 0);
            }
        });
        mVideoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                Log.e(TAG, "onCompletion: ");
                finish();
//                Toast.makeText(PlayActivity.this, "播放完了", Toast.LENGTH_SHORT).show();
            }
        });
        mDialog = new ProgressDialog(this);
        mDialog.setTitle("缓冲中。。。");

        mVideoView.setOnBufferingUpdateListener(new MediaPlayer.OnBufferingUpdateListener() {
            @Override
            public void onBufferingUpdate(MediaPlayer mp, int percent) {
//                Log.e(TAG, "onBufferingUpdate: " + percent);
//                if (percent != 100) {
                mDialog.setMessage(percent + "%");
//                    mDialog.show();
//                } else {
//                    mDialog.dismiss();
//                }
            }
        });
        mVideoView.setOnInfoListener(new MediaPlayer.OnInfoListener() {
            @Override
            public boolean onInfo(MediaPlayer mp, int what, int extra) {
                Log.e(TAG, "onInfo: " + what + " " + extra);
                switch (what) {
                    case MediaPlayer.MEDIA_INFO_VIDEO_TRACK_LAGGING:
                        Log.e(TAG, "MEDIA_INFO_VIDEO_TRACK_LAGGING");
                        return true;
                    case MediaPlayer.MEDIA_INFO_BUFFERING_START://从开始播放的位置开始缓冲
                        Log.e(TAG, "缓冲开始");
                        long duration = mVideoView.getDuration();
                        long currentPosition = mVideoView.getCurrentPosition();
                        Log.e(TAG, "onInfo: " + duration + " " + currentPosition);
                        float v = currentPosition * 1.0f / duration;
                        Log.e(TAG, "播放进度: " + v);
                        if (v >= 0.99) {
                            mVideoView.pause();
                            mVideoView.stopPlayback();
                            Intent intent = new Intent();
                            intent.putExtra("position", mPosition);
                            setResult(Activity.RESULT_OK, intent);
                            finish();
                            return true;
                        }

//                        downloadSize=0;
                        if (mVideoView.isPlaying())
                            mVideoView.pause();
//                        pb.setVisibility(ViewGroup.VISIBLE);
//                        rate.setVisibility(ViewGroup.VISIBLE);
//                        download_rate.setVisibility(ViewGroup.VISIBLE);
                        mDialog.show();
                        return true;

                    case MediaPlayer.MEDIA_INFO_BUFFERING_END://从开始缓冲的地方一直缓冲到填满缓冲区为止
                        Log.e(TAG, "缓冲结束");
//                        pb.setVisibility(ViewGroup.GONE);
//                        rate.setVisibility(ViewGroup.GONE);
//                        download_rate.setVisibility(ViewGroup.GONE);
                        mVideoView.start();

                        mDialog.dismiss();
                        return true;
                    case MediaPlayer.MEDIA_INFO_NOT_SEEKABLE:
                        Log.e(TAG, "MEDIA_INFO_NOT_SEEKABLE");
                        break;
                    case MediaPlayer.MEDIA_INFO_DOWNLOAD_RATE_CHANGED://(extra为下载速率，单位是kb/s)
                        Log.e(TAG, "下载的速率：" + extra);
//                        download_rate.setText(extra+"kb/s");
//                        downloadSize+=extra;
                        return true;
                    default:
                        break;
                }
                return false;
            }
        });
        if (savedInstanceState != null) {
            long position = savedInstanceState.getLong("position", 0);
            mVideoView.seekTo(position);
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
//            Toast.makeText(this, "横屏", Toast.LENGTH_SHORT).show();
            //去掉系统通知栏
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
            //调整视频layout的布局
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            mVideoContainer.setLayoutParams(layoutParams);
            //原视频大小
//            public static final int VIDEO_LAYOUT_ORIGIN = 0;
            //最优选择，由于比例问题还是会离屏幕边缘有一点间距，所以最好把父View的背景设置为黑色会好一点
//            public static final int VIDEO_LAYOUT_SCALE = 1;
            //拉伸，可能导致变形
//            public static final int VIDEO_LAYOUT_STRETCH = 2;
            //会放大可能超出屏幕
//            public static final int VIDEO_LAYOUT_ZOOM = 3;
            //效果还是竖屏大小（字面意思是填充父View）
//            public static final int VIDEO_LAYOUT_FIT_PARENT = 4;
            mVideoView.setVideoLayout(VideoView.VIDEO_LAYOUT_SCALE, 0);
        } else {
            /*清除flag,恢复显示系统状态栏*/
//            Toast.makeText(this, "竖屏", Toast.LENGTH_SHORT).show();
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dpToPx(220));
            mVideoContainer.setLayoutParams(layoutParams);
        }
        super.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.e(TAG, "onSaveInstanceState: ");
        outState.putLong("position", mVideoView.getCurrentPosition());
    }

    int dpToPx(int dps) {
        return Math.round(getResources().getDisplayMetrics().density * dps);
    }
}
