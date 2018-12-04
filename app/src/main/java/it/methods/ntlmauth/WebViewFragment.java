package it.methods.ntlmauth;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.ConsoleMessage;
import android.webkit.HttpAuthHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Switch;

import it.methods.ntlmauth.lib.Const;
import it.methods.ntlmauth.lib.Utility;

public class WebViewFragment extends Fragment
{
	private static final String CLASS_TAG = "WebViewFragment";

	private WebView m_webView;
	private ProgressBar m_progress;
	private Switch m_useAuth;

	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
	{
		View view = inflater.inflate(R.layout.fragment_web_view, container, false);

		m_progress = view.findViewById(R.id.progressBar);
		m_progress.setVisibility(View.GONE);

		m_webView = view.findViewById(R.id.webView);
		m_useAuth = view.findViewById(R.id.useAuth);

		Button go = view.findViewById(R.id.go);
		go.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View view)
			{
				SharedPreferences impostazioni = PreferenceManager.getDefaultSharedPreferences(getContext());

				m_webView.loadUrl(impostazioni.getString(Const.SETTING_URL, Const.EMPTY_STRING));
			}
		});

		ConfigureWebView();

		return view;
	}

	@SuppressLint({"SetJavaScriptEnabled"})
	private void ConfigureWebView()
	{
		WebViewClientExt webViewClient = new WebViewClientExt();
		m_webView.setWebViewClient(webViewClient);
		m_webView.setWebChromeClient(new WebChromeClientExt());
		m_webView.getSettings().setAppCacheEnabled(true);
		m_webView.getSettings().setJavaScriptEnabled(true);
		m_webView.getSettings().setBuiltInZoomControls(true);
		m_webView.getSettings().setDisplayZoomControls(false);
		m_webView.getSettings().setAllowFileAccess(true);
		m_webView.getSettings().setAllowContentAccess(true);
		m_webView.getSettings().setLoadWithOverviewMode(true);
		m_webView.getSettings().setUserAgentString(Utility.getUserAgent());

		m_webView.setOnKeyListener(new View.OnKeyListener()
		{
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event)
			{
				if ((keyCode == KeyEvent.KEYCODE_BACK) && m_webView.canGoBack())
				{
					m_webView.goBack();
					return true;
				}

				return false;
			}
		});
	}

	private class WebViewClientExt extends WebViewClient
	{
		@Override
		public void onPageStarted(WebView view, String url, Bitmap favicon)
		{
			m_progress.setVisibility(View.VISIBLE);
		}

		@Override
		public void onPageFinished(WebView view, String url)
		{
			m_progress.setVisibility(View.GONE);
		}

		@Override
		public void onReceivedHttpAuthRequest(WebView view, HttpAuthHandler handler, String host, String realm)
		{
			Log.i(CLASS_TAG, "onReceivedHttpAuthRequest host " + host + ", realm " + realm);

			if (m_useAuth.isChecked())
			{
				SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getContext());

				handler.proceed(
					settings.getString(Const.SETTING_USERNAME, Const.EMPTY_STRING),
					settings.getString(Const.SETTING_PASSWORD, Const.EMPTY_STRING));
			}

			super.onReceivedHttpAuthRequest(view, handler, host, realm);
		}
	}

	private class WebChromeClientExt extends WebChromeClient
	{
		WebChromeClientExt()
		{}

		@Override
		public boolean onConsoleMessage(ConsoleMessage consoleMessage)
		{
			String testo = "javascript error: " + consoleMessage.message();

			if (!consoleMessage.sourceId().equals(Const.EMPTY_STRING))
			{
				testo +=
					" from line " + consoleMessage.lineNumber() + " " +
					"of " + consoleMessage.sourceId();
			}

			Log.w(CLASS_TAG, testo);

			return super.onConsoleMessage(consoleMessage);
		}
	}


}
