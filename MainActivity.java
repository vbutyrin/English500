package ru.englishfivehundred;

import android.app.Activity;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import java.util.Locale;

public class MainActivity extends Activity {
    private WebView webView;
    private TextToSpeech speech;
    private boolean speechReady = false;

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        speech = new TextToSpeech(getApplicationContext(), status -> {
            speechReady = status == TextToSpeech.SUCCESS;
            if (speechReady) {
                speech.setLanguage(Locale.US);
                speech.setSpeechRate(0.85f);
            }
        });
        webView = new WebView(this);
        setContentView(webView);
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setAllowFileAccess(true);
        settings.setAllowContentAccess(false);
        settings.setJavaScriptCanOpenWindowsAutomatically(false);
        webView.setWebViewClient(new WebViewClient());
        webView.setWebChromeClient(new WebChromeClient());
        webView.addJavascriptInterface(new SpeechBridge(), "AndroidSpeech");
        webView.loadUrl("file:///android_asset/index.html");
    }

    private final class SpeechBridge {
        @JavascriptInterface
        public void speak(String text) {
            if (text == null || text.length() > 250) return;
            runOnUiThread(() -> {
                if (speechReady && speech != null) {
                    speech.speak(text, TextToSpeech.QUEUE_FLUSH, null, "pronunciation");
                }
            });
        }
    }

    @Override
    protected void onDestroy() {
        if (speech != null) {
            speech.stop();
            speech.shutdown();
        }
        if (webView != null) webView.destroy();
        super.onDestroy();
    }
}
