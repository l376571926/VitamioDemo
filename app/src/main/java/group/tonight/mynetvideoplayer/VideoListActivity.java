package group.tonight.mynetvideoplayer;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;

import java.util.ArrayList;

public class VideoListActivity extends AppCompatActivity implements BaseQuickAdapter.OnItemClickListener {

    private static final String TAG = VideoListActivity.class.getSimpleName();
    private RecyclerView mRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_list);

        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        mRecyclerView.setAdapter(mBaseQuickAdapter);
        mBaseQuickAdapter.setOnItemClickListener(this);

        if (getIntent().hasExtra("list")) {
            ArrayList<VideoDataBean> videoDataBeanList = getIntent().getParcelableArrayListExtra("list");
            mBaseQuickAdapter.setNewData(videoDataBeanList);
        }
    }

    private BaseQuickAdapter<VideoDataBean, BaseViewHolder> mBaseQuickAdapter = new BaseQuickAdapter<VideoDataBean, BaseViewHolder>(android.R.layout.simple_list_item_2) {
        @Override
        protected void convert(BaseViewHolder helper, VideoDataBean item) {
            helper.setText(android.R.id.text1, item.getTitle());
            helper.setText(android.R.id.text2, item.getSize());
        }
    };

    @Override
    public void onItemClick(BaseQuickAdapter adapter, View view, final int position) {
        final VideoDataBean dataBean = (VideoDataBean) adapter.getItem(position);
        new AlertDialog.Builder(this)
                .setTitle("即将播放")
                .setMessage(dataBean.getTitle())
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(VideoListActivity.this, PlayActivity.class);
                        intent.putExtra("videoUrl", dataBean.getUrl());
                        intent.putExtra("position", position);
                        startActivityForResult(intent, 0);
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (data != null) {
                if (data.hasExtra("position")) {
                    int position = data.getIntExtra("position", -1);
                    if (position != -1) {
                        position++;
                        if (position < mBaseQuickAdapter.getItemCount()) {
                            VideoDataBean dataBean = mBaseQuickAdapter.getItem(position);
                            Intent intent = new Intent(VideoListActivity.this, PlayActivity.class);
                            intent.putExtra("videoUrl", dataBean.getUrl());
                            intent.putExtra("position", position);
                            startActivityForResult(intent, 0);
                        }
                    }
                }
            }
        }
    }
}
