package com.nodepp.smartnode.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.nodepp.smartnode.R;
import com.nodepp.smartnode.view.TitleBar;
import com.nodepp.smartnode.view.loadingdialog.LoadingDialog;

public class WebActivity extends BaseActivity {

    private WebView mWebview;
    private WebSettings mWebSettings;
    private LoadingDialog loadingDialog;
    private static String mUrl = "http://shop.m.taobao.com/shop/shop_index.htm?user_id=3346625927";
    private TitleBar titleBar;
    private String title;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_store);
        Intent intent = getIntent();
        mUrl = intent.getStringExtra("url");
        title = intent.getStringExtra("title");
        initView();
    }

    private void initView() {
        titleBar = (TitleBar) findViewById(R.id.title_bar);
        titleBar.setTitle(title);
        mWebview = (WebView) findViewById(R.id.webView);
        mWebSettings = mWebview.getSettings();
        mWebSettings.setJavaScriptEnabled(true);
        mWebSettings.setSupportZoom(true);
        mWebview.requestFocus();
        loadingDialog = new LoadingDialog(this,"数据加载中");
        mWebview.loadUrl(mUrl);
        WebViewClient client = new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
            }
        };
        mWebview.setWebViewClient(client);
        //设置不用系统浏览器打开,直接显示在当前Webview
//        mWebview.setWebViewClient(new WebViewClient() {
//            @Override
//            public boolean shouldOverrideUrlLoading(WebView view, String url) {
//                return super.shouldOverrideUrlLoading(view,url);
//            }
//
//            @Override
//            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
//                handler.proceed();
//            }
//        });
        //设置WebChromeClient类
        mWebview.setWebChromeClient(new WebChromeClient() {
            //获取网站标题
            @Override
            public void onReceivedTitle(WebView view, String title) {
                System.out.println("标题在这里");
            }


            //获取加载进度
            @Override
            public void onProgressChanged(WebView view, int newProgress) {

            }
        });

        //设置WebViewClient类
        mWebview.setWebViewClient(new WebViewClient() {
            //设置加载前的函数
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                System.out.println("开始加载了");
                if (!WebActivity.this.isFinishing()){
                    loadingDialog.show();
                }
            }

            //设置结束加载函数
            @Override
            public void onPageFinished(WebView view, String url) {
                if (!WebActivity.this.isFinishing()){
                    loadingDialog.hide();
                }
            }
        });

    }

    //点击返回上一页面而不是退出浏览器
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && mWebview.canGoBack()) {
            mWebview.goBack();
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }
    //销毁Webview
    @Override
    protected void onDestroy() {
        if (mWebview != null) {
            mWebview.loadDataWithBaseURL(null, "", "text/html", "utf-8", null);
            mWebview.clearHistory();

            ((ViewGroup) mWebview.getParent()).removeView(mWebview);
            mWebview.destroy();
            mWebview = null;
        }
        if (loadingDialog != null){
            loadingDialog.dismiss();
        }
        super.onDestroy();
    }
}
