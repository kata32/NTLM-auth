package it.methods.ntlmauth.lib;

import android.util.Log;
import android.webkit.ValueCallback;

import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.CookieStore;
import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class CookieManagerHelper extends CookieManager
{
	private final static String CLASS_TAG = "CookieManagerHelper";
	private android.webkit.CookieManager m_webkitCookieManager;

	public CookieManagerHelper(CookiePolicy cookiePolicy)
	{
		super(null, cookiePolicy);

		m_webkitCookieManager = android.webkit.CookieManager.getInstance();
	}

	public void resetCookie()
	{
		if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.LOLLIPOP)
		{
			m_webkitCookieManager.removeSessionCookie();
		}
		else
		{
			m_webkitCookieManager.removeSessionCookies(new ValueCallback<Boolean>()
			{
				@Override
				public void onReceiveValue(Boolean aBoolean)
				{
					Log.i(CLASS_TAG, "session cookies removed");
				}
			});
		}
	}

	@Override
	public void put(URI uri, Map<String, List<String>> responseHeaders)
	{
		if ((uri == null) || (responseHeaders == null))
			return;

		String url = uri.toString();

		for (String headerKey : responseHeaders.keySet())
		{
			if ((headerKey == null) || !(headerKey.equalsIgnoreCase("Set-Cookie2") || headerKey.equalsIgnoreCase("Set-Cookie")))
				continue;

			List<String> headers = responseHeaders.get((headerKey));

			if (headers != null)
			{
				for (String headerValue : headers)
				{
					m_webkitCookieManager.setCookie(url, headerValue);
				}
			}
		}
	}

	@Override
	public Map<String, List<String>> get(URI uri, Map<String, List<String>> requestHeaders)
	{
		if ((uri == null) || (requestHeaders == null))
			throw new IllegalArgumentException("Argument is null");

		String url = uri.toString();

		Map<String, List<String>> res = new java.util.HashMap<>();

		String cookie = m_webkitCookieManager.getCookie(url);

		if (cookie != null)
			res.put("Cookie", Collections.singletonList(cookie));

		return res;
	}

	@Override
	public CookieStore getCookieStore()
	{
		throw new UnsupportedOperationException();
	}
}