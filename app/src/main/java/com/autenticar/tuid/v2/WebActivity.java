package com.autenticar.tuid.v2;

import android.os.Bundle;
import android.os.PersistableBundle;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.autenticar.tuid.R;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class WebActivity extends AppCompatActivity {

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.web_activity);
        WebView webView = findViewById(R.id.webview);
        cargarUrl(webView, getIntent().getStringExtra("url"));

        setTitle("Novedades");
        findViewById(R.id.salir).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }


    void cargarCodigoHtml(WebView webView){
        final String mimeType = "text/html";
        final String encoding = "UTF-8";
        String html = "<br /><br />Read the handouts please for tomorrow.<br /><br /><!--homework help homework" +
                "help help with homework homework assignments elementary school high school middle school" +
                "// --><font color='#60c000' size='4'><strong>Please!</strong></font>" +
                "<img src='http://www.homeworknow.com/hwnow/upload/images/tn_star300.gif'  />";


        webView.loadDataWithBaseURL("", html, mimeType, encoding, "");
    }

    void cargarUrl(WebView webView, String url){
        webView.getSettings().setJavaScriptEnabled(true);
        webView.loadUrl(url);
        webView.setWebViewClient(new WebViewClient() {

            public void onPageFinished(WebView view, String url) {
                findViewById(R.id.progress).setVisibility(View.GONE);
            }
        });
    }
}
