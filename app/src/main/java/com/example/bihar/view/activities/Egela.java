package com.example.bihar.view.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.example.bihar.R;

public class Egela extends AppCompatActivity {

    private static final String URL_EGELA = "https://egela.ehu.eus/";

    private WebView webView;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_egela);

        webView = findViewById(R.id.webview);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebViewClient(new WebViewClient());
        webView.loadUrl(URL_EGELA);
    }

    @Override
    public void onBackPressed() {
        if(!webView.canGoBack()){
            finish();
        }else{
            webView.goBack();
        }

    }
}
