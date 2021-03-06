package cn.entertech.bleuisdk.ui.activity;

import android.os.Build;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.TextView;

import cn.entertech.bleuisdk.R;
import cn.entertech.bleuisdk.utils.NetworkState;

import static cn.entertech.bleuisdk.utils.Constant.INTENT_WEB_TITLE;
import static cn.entertech.bleuisdk.utils.Constant.INTENT_WEB_URL;

/**
 * Created by EnterTech on 2017/1/4.
 */

public class WebActivity extends BaseActivity {

    //    private XWalkView xWalkWebView;
    protected WebView mWebView;
    private ProgressBar mProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().setStatusBarColor(getColor(R.color.colorThemeBlue));
        }
        setContentView(R.layout.activity_web);
        String text = getIntent().getStringExtra(INTENT_WEB_TITLE);
        TextView title = (TextView) findViewById(R.id.web_title);
        title.setText(text);

        load();
    }

    protected void load() {
        if (-1 == NetworkState.getConnectedType(this)) {
            findViewById(R.id.web_nonetwork).setVisibility(View.VISIBLE);
            findViewById(R.id.web_progress).setVisibility(View.GONE);
            return;
        }

//        XWalkPreferences.setValue(XWalkPreferences.REMOTE_DEBUGGING, true);
//        xWalkWebView = (XWalkView) findViewById(R.id.web_xwalk);
        mWebView = (WebView) findViewById(R.id.web_webview);
        mProgressBar = (ProgressBar) findViewById(R.id.web_progress);

        findViewById(R.id.web_nonetwork).setVisibility(View.GONE);
        mProgressBar.setVisibility(View.VISIBLE);

        final String url = getIntent().getStringExtra(INTENT_WEB_URL);
        mWebView.setVisibility(View.VISIBLE);
//        xWalkWebView.setVisibility(View.GONE);
        mWebView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                super.onProgressChanged(view, newProgress);
                mProgressBar.setProgress(newProgress);
                if (newProgress >= 100) {
                    mProgressBar.setVisibility(View.GONE);
                }
            }

        });
        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                if (null != mOnFinishListener) {
                    mOnFinishListener.onFinish();
                }
            }
        });
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        mWebView.getSettings().setUseWideViewPort(true);
        mWebView.getSettings().setLoadWithOverviewMode(true);
        mWebView.getSettings().setDomStorageEnabled(true);
        mWebView.loadUrl(url);

//        //暂时注释xWalkWebView，适应很差
//        if (url.equals(Constants.ME_MARKET)) {
//            mWebView.setVisibility(View.VISIBLE);
//            xWalkWebView.setVisibility(View.GONE);
//            mWebView.setWebChromeClient(new WebChromeClient() {
//                @Override
//                public void onProgressChanged(WebView view, int newProgress) {
//                    super.onProgressChanged(view, newProgress);
//                    mProgressBar.setProgress(newProgress);
//                    if (newProgress >= 100) {
//                        mProgressBar.setVisibility(View.GONE);
//                    }
//                }
//
//            });
//            mWebView.setWebViewClient(new WebViewClient());
//            mWebView.getSettings().setJavaScriptEnabled(true);
//            mWebView.loadUrl(url);
//        } else {
//            mWebView.setVisibility(View.GONE);
//            xWalkWebView.setResourceClient(new XWalkResourceClient(xWalkWebView) {
//                @Override
//                public void onProgressChanged(XWalkView view, int progressInPercent) {
//                    super.onProgressChanged(view, progressInPercent);
//                    mProgressBar.setProgress(progressInPercent);
//                    if (progressInPercent >= 100) {
//                        mProgressBar.setVisibility(View.GONE);
//                    }
//                }
//            });
//            xWalkWebView.setVisibility(View.VISIBLE);
//        }
    }

//    @Override
//    protected void onXWalkReady() {
//        final String url = getIntent().getStringExtra(ExtraKey.EXTRA_URL);
//        if (!url.equals(Constants.ME_MARKET)) {
//            xWalkWebView.loadUrl(url);
//        }
//    }

    public void onBack(View view) {
        finish();
    }

    public void onReload(View view) {
        load();
    }

    public interface OnFinishListener {
        void onFinish();
    }

    private OnFinishListener mOnFinishListener;

    protected void setOnFinishListener(OnFinishListener listener) {
        mOnFinishListener = listener;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && mWebView != null && mWebView.canGoBack()) {
            mWebView.goBack();// back to pre page 返回前一个页面
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
