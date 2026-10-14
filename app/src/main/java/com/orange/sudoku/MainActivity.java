package com.orange.sudoku;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.ValueCallback;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.Toast;

/**
 * 数字九宫格 · WebView 壳
 *
 * 游戏本体是 assets/sudoku.html（单文件、离线、自带 80 关题库），
 * 这里只负责：全屏承载、开启 localStorage（存关卡进度）、接管返回键。
 * 不申请任何权限，也不访问网络。
 */
public class MainActivity extends Activity {

    private static final String START_URL = "file:///android_asset/sudoku.html";
    private static final String BG_COLOR = "#F5F6FA";

    private WebView webView;
    private long lastBackTime = 0L;

    @SuppressLint({"SetJavaScriptEnabled", "ClickableViewAccessibility"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 浅色状态栏，与页面底色一致
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }

        webView = new WebView(this);
        webView.setBackgroundColor(Color.parseColor(BG_COLOR));
        webView.setOverScrollMode(View.OVER_SCROLL_NEVER);
        webView.setLongClickable(false);
        webView.setHapticFeedbackEnabled(false);
        webView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                return true;   // 屏蔽长按选中/弹出菜单，更接近原生手感
            }
        });

        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);   // 关卡进度存在 localStorage，必须开启
        settings.setDatabaseEnabled(true);
        settings.setAllowFileAccess(true);
        settings.setAllowContentAccess(false);
        settings.setSupportZoom(false);
        settings.setBuiltInZoomControls(false);
        settings.setDisplayZoomControls(false);
        settings.setTextZoom(100);             // 不跟随系统字号缩放，避免棋盘错位
        settings.setCacheMode(WebSettings.LOAD_NO_CACHE);
        settings.setMediaPlaybackRequiresUserGesture(true);

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                return true;   // 单机游戏，不跳转任何外部页面
            }
        });

        webView.loadUrl(START_URL);

        FrameLayout root = new FrameLayout(this);
        root.setBackgroundColor(Color.parseColor(BG_COLOR));
        root.addView(webView, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        setContentView(root);
    }

    /**
     * 返回键：先问页面能不能处理（关弹窗 / 退回上一层），处理不了才退出应用。
     * 在首页需要连按两次才退出，避免误触。
     */
    @Override
    public void onBackPressed() {
        if (webView == null) {
            super.onBackPressed();
            return;
        }
        webView.evaluateJavascript(
                "(window.__androidBack && window.__androidBack()) ? '1' : '0'",
                new ValueCallback<String>() {
                    @Override
                    public void onReceiveValue(String value) {
                        if ("\"1\"".equals(value)) {
                            return;               // 页面已处理，不倒计时退出
                        }
                        long now = System.currentTimeMillis();
                        if (now - lastBackTime < 2000L) {
                            finish();
                        } else {
                            lastBackTime = now;
                            Toast.makeText(MainActivity.this, "再按一次退出", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    @Override
    protected void onPause() {
        // 切到后台自动暂停计时，避免用时被算进去
        if (webView != null) {
            webView.evaluateJavascript("window.__androidPause && window.__androidPause()", null);
            webView.onPause();
        }
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (webView != null) {
            webView.onResume();
        }
    }

    @Override
    protected void onDestroy() {
        if (webView != null) {
            ViewGroup parent = (ViewGroup) webView.getParent();
            if (parent != null) {
                parent.removeView(webView);
            }
            webView.destroy();
            webView = null;
        }
        super.onDestroy();
    }
}
