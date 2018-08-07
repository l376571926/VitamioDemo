package group.tonight.mynetvideoplayer;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcel;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;

import io.vov.vitamio.Vitamio;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = MainActivity.class.getSimpleName();
    private EditText mVideoUrlView;
    private Button mCommitView;
    private WebView mWebView;

    private Button mParseButton;
    private ParseTask mParseTask;
    private ArrayList<VideoDataBean> mVideoDataBeanList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Vitamio.isInitialized(getApplicationContext());
        setContentView(R.layout.activity_main);

        mVideoUrlView = (EditText) findViewById(R.id.video_url);
        mCommitView = (Button) findViewById(R.id.commit);
        mParseButton = (Button) findViewById(R.id.parse);
        mCommitView.setOnClickListener(this);
        mParseButton.setOnClickListener(this);

        mWebView = (WebView) findViewById(R.id.web_view);
        initWebViewSetting(mWebView);
        mWebView.setWebViewClient(mWebViewClient);
        mWebView.setWebChromeClient(mWebChromeClient);

        mWebView.loadUrl("http://192.168.1.121:8080/");

        mVideoDataBeanList = new ArrayList<>();
    }

    private WebViewClient mWebViewClient = new WebViewClient() {
        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            Log.e(TAG, "onPageStarted: " + url);
            mParseButton.setEnabled(false);
            if (mParseTask != null) {
                mParseTask.cancel(true);
            }
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            Log.e(TAG, "onPageFinished: " + url);
            mParseTask = new ParseTask();
            mParseTask.execute(url);

        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            Log.e(TAG, "shouldOverrideUrlLoading: " + url);
            if (url.endsWith(".rmvb")) {
                //http://192.168.1.121:8080/%E7%94%B5%E8%A7%86%E5%89%A7/%E7%A5%9E%E8%AF%9D/[%E8%BF%85%E6%92%AD%E5%BD%B1%E9%99%A2www.XunBo.Cc]%E7%A5%9E%E8%AF%9D40.%E5%9B%BD%E8%AF%AD%E4%B8%AD%E5%AD%97.dvd.rmvb
                Intent intent = new Intent(MainActivity.this, PlayActivity.class);
                intent.putExtra("videoUrl", url);
                startActivity(intent);
            }
            return super.shouldOverrideUrlLoading(view, url);
        }
    };

    private WebChromeClient mWebChromeClient = new WebChromeClient() {

    };

    private void initWebViewSetting(WebView view) {
        WebSettings webSettings = view.getSettings();
        webSettings.setUseWideViewPort(true);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setBuiltInZoomControls(true);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.commit:
                String url = mVideoUrlView.getText().toString();
                if (TextUtils.isEmpty(url.trim())) {
                    Toast.makeText(this, "请填写视频链接", Toast.LENGTH_SHORT).show();
                    return;
                }
                mWebView.loadUrl(url);
                break;
            case R.id.parse:
                new AlertDialog.Builder(MainActivity.this)
                        .setMessage("解析完成，是否跳转视频列表？")
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent = new Intent(MainActivity.this, VideoListActivity.class);
                                intent.putParcelableArrayListExtra("list", mVideoDataBeanList);
                                startActivity(intent);
                            }
                        })
                        .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        })
                        .show();
                break;
            default:
                break;
        }
    }

    private class ParseTask extends AsyncTask<String, Void, ArrayList<VideoDataBean>> {
        @Override
        protected ArrayList<VideoDataBean> doInBackground(String... strings) {
            try {
                ArrayList<VideoDataBean> videoDataBeanList = new ArrayList<>();
                Document document = Jsoup.connect(strings[0]).get();
                String baseUri1 = document.baseUri();
                Elements elements = document.select("a[href*=rmvb]");
                for (Element element : elements) {
                    if (isCancelled()) {
                        break;
                    }
                    VideoDataBean dataBean = new VideoDataBean(Parcel.obtain());
                    String text = element.text();
                    dataBean.setTitle(text);
                    String href = element.attr("href");
                    dataBean.setUrl(baseUri1 + href);
                    Element parent = element.parent().parent();
                    Elements allElements = parent.getAllElements();
                    for (Element allElement : allElements) {
                        if (isCancelled()) {
                            break;
                        }
                        String sizeText = allElement.text();
                        if (!sizeText.endsWith("MB")) {
                            continue;
                        }
                        dataBean.setSize(sizeText);
                    }
                    videoDataBeanList.add(dataBean);
                }
                return videoDataBeanList;
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(final ArrayList<VideoDataBean> videoDataBeanList) {
            super.onPostExecute(videoDataBeanList);
            if (videoDataBeanList == null) {
                return;
            }
            if (videoDataBeanList.isEmpty()) {
                return;
            }
            mVideoDataBeanList.clear();
            mVideoDataBeanList.addAll(videoDataBeanList);
            mParseButton.setEnabled(true);
        }

    }
}
