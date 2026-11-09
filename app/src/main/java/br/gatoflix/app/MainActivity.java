package br.gatoflix.app;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.webkit.CookieManager;
import android.webkit.URLUtil;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.TextView;

public class MainActivity extends Activity {

  private WebView web;
  private FrameLayout raiz;
  private View telaCheia;
  private WebChromeClient.CustomViewCallback volta;
  private TextView abertura;

  @Override
  protected void onCreate(Bundle estado) {
    super.onCreate(estado);
    getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

    raiz = new FrameLayout(this);
    raiz.setBackgroundColor(Color.BLACK);

    web = new WebView(this);
    web.setBackgroundColor(Color.BLACK);
    raiz.addView(web, new FrameLayout.LayoutParams(-1, -1));

    abertura = new TextView(this);
    abertura.setText("Gato Flix");
    abertura.setTextColor(0xFFE50914);
    abertura.setTextSize(34);
    abertura.setGravity(Gravity.CENTER);
    abertura.setBackgroundColor(Color.BLACK);
    raiz.addView(abertura, new FrameLayout.LayoutParams(-1, -1));

    setContentView(raiz);
    ajustar();

    if (BuildConfig.DEBUG) WebView.setWebContentsDebuggingEnabled(true);
    web.loadUrl(BuildConfig.SITE);
  }

  private void ajustar() {
    WebSettings s = web.getSettings();
    s.setJavaScriptEnabled(true);
    s.setDomStorageEnabled(true);
    s.setDatabaseEnabled(true);
    s.setMediaPlaybackRequiresUserGesture(false);
    s.setUseWideViewPort(true);
    s.setLoadWithOverviewMode(true);
    s.setSupportMultipleWindows(true);
    s.setJavaScriptCanOpenWindowsAutomatically(true);
    s.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
    CookieManager.getInstance().setAcceptCookie(true);
    CookieManager.getInstance().setAcceptThirdPartyCookies(web, true);

    web.setWebViewClient(new WebViewClient() {
      @Override public boolean shouldOverrideUrlLoading(WebView v, WebResourceRequest req) {
        return fora(req.getUrl().toString());
      }
      @Override public boolean shouldOverrideUrlLoading(WebView v, String url) {
        return fora(url);
      }
      @Override public void onPageFinished(WebView v, String url) {
        if (abertura != null) { abertura.setVisibility(View.GONE); abertura = null; }
      }
    });

    web.setWebChromeClient(new WebChromeClient() {
      @Override public void onShowCustomView(View v, CustomViewCallback cb) {
        if (telaCheia != null) { cb.onCustomViewHidden(); return; }
        telaCheia = v; volta = cb;
        raiz.addView(v, new FrameLayout.LayoutParams(-1, -1));
        web.setVisibility(View.GONE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        esconderSistema();
      }
      @Override public void onHideCustomView() { sairDaCheia(); }
    });

    web.setDownloadListener((url, ua, cd, mime, tam) -> baixar(url, ua, cd, mime));
  }

  private boolean fora(String url) {
    try {
      if (url == null) return false;
      if (url.startsWith("http")) {
        if (url.startsWith(BuildConfig.SITE) || url.contains("/__h/")) return false;
        abrirFora(url);
        return true;
      }
      abrirFora(url);
      return true;
    } catch (Exception e) { return false; }
  }

  private void abrirFora(String url) {
    try { startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url))); } catch (Exception e) { }
  }

  private void baixar(String url, String ua, String cd, String mime) {
    try {
      if (url.startsWith("blob:") || url.startsWith("data:")) { abrirFora(url); return; }
      String nome = URLUtil.guessFileName(url, cd, mime);
      DownloadManager.Request r = new DownloadManager.Request(Uri.parse(url));
      r.setMimeType(mime);
      r.addRequestHeader("User-Agent", ua);
      String c = CookieManager.getInstance().getCookie(url);
      if (c != null) r.addRequestHeader("Cookie", c);
      r.setTitle(nome);
      r.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
      r.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, nome);
      DownloadManager dm = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
      if (dm != null) dm.enqueue(r); else abrirFora(url);
    } catch (Exception e) { abrirFora(url); }
  }

  private void esconderSistema() {
    getWindow().getDecorView().setSystemUiVisibility(
        View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
      | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
  }

  private void sairDaCheia() {
    if (telaCheia == null) return;
    raiz.removeView(telaCheia);
    telaCheia = null;
    web.setVisibility(View.VISIBLE);
    getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
    if (volta != null) { volta.onCustomViewHidden(); volta = null; }
  }

  @Override public boolean onKeyDown(int codigo, KeyEvent ev) {
    if (codigo == KeyEvent.KEYCODE_BACK) {
      if (telaCheia != null) { sairDaCheia(); return true; }
      if (web != null && web.canGoBack()) { web.goBack(); return true; }
    }
    return super.onKeyDown(codigo, ev);
  }

  @Override protected void onPause() { super.onPause(); if (web != null) web.onPause(); }
  @Override protected void onResume() { super.onResume(); if (web != null) web.onResume(); }

  @Override protected void onDestroy() {
    if (web != null) { web.loadUrl("about:blank"); web.destroy(); web = null; }
    super.onDestroy();
  }
}
