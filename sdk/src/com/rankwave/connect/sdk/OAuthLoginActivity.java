package com.rankwave.connect.sdk;

import com.rankwave.connect.sdk.core.OAuthTwitter;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.net.http.SslError;
import android.os.Bundle;
import android.webkit.SslErrorHandler;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;

public class OAuthLoginActivity extends Activity {

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		LinearLayout linearLayout = new LinearLayout(this);
		linearLayout.setBackgroundColor(Color.WHITE);
		linearLayout.setOrientation(LinearLayout.VERTICAL);
		WebView webview = new WebView(this);
		linearLayout.addView(webview);
		setContentView(linearLayout);

		webview.setWebViewClient(new WebViewClient() {

			@Override
			public void onReceivedSslError(WebView view,
					SslErrorHandler handler, SslError error) {

				// super.onReceivedSslError(view, handler, error);
				handler.proceed();
			}

			public void onPageFinished(WebView view, String url) {

				super.onPageFinished(view, url);

				if (url != null && url.equals("http://mobile.twitter.com/")) {
					finish();
				} else if (url != null
						&& url.startsWith(OAuthTwitter.TWIT_CALLBACK_URL
								.toString())) {
					String[] params = url.split("\\?")[1].split("&");
					String oauthToken = "";
					String oauthVerifier = "";

					try {
						if (params.length > 0
								&& params[0].startsWith("oauth_token")) {
							oauthToken = params[0].split("=")[1];
						} else if (params.length > 1
								&& params[1].startsWith("oauth_token")) {
							oauthToken = params[1].split("=")[1];
						}

						if (params.length > 0
								&& params[0].startsWith("oauth_verifier")) {
							oauthVerifier = params[0].split("=")[1];
						} else if (params.length > 1
								&& params[1].startsWith("oauth_verifier")) {
							oauthVerifier = params[1].split("=")[1];
						}

						if (oauthToken.length() > 0
								|| oauthVerifier.length() > 0) {
							Intent resultIntent = new Intent();
							resultIntent.putExtra("oauthToken", oauthToken);
							resultIntent.putExtra("oauthVerifier",
									oauthVerifier);

							setResult(RESULT_OK, resultIntent);
							finish();
						} else {
							setResult(RESULT_CANCELED, null);
							finish();
						}

					} catch (Exception e) {
						e.printStackTrace();
						finish();
					}
				}
			}

		});

		Intent passedIntent = getIntent();
		String authUrl = passedIntent.getStringExtra("authUrl");
		webview.loadUrl(authUrl);
	}

	@Override
	public void onBackPressed() {

		setResult(RESULT_CANCELED, null);
		finish();
	}
}