package it.methods.ntlmauth.lib;

import android.util.Log;

import java.io.IOException;
import java.net.CookieHandler;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.JavaNetCookieJar;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class HttpConn
{
	private static final String CLASS_TAG = "HttpConn";
	private static final String HEADER_USER_AGENT = "User-Agent";

	public static String getString(String urlToGet, String username, String password) throws IOException
	{
		Log.i(CLASS_TAG, urlToGet);

		OkHttpClient client = getClient(username, password).build();

		Request request = getRequest(urlToGet);

		Response response = client.newCall(request).execute();

		if (!response.isSuccessful())
			throw new IOException(response.message());

		if (response.body() == null)
			return Const.EMPTY_STRING;
		else
			return response.body().string();
	}

	private static OkHttpClient.Builder getClient(String username, String password)
	{
		OkHttpClient.Builder httpClientBuilder = new OkHttpClient.Builder()
			.connectTimeout(5, TimeUnit.SECONDS)
			.writeTimeout(10, TimeUnit.SECONDS)
			.readTimeout(10, TimeUnit.SECONDS)
			.followRedirects(true)
			.followSslRedirects(true);

		if (CookieHandler.getDefault() != null)
			httpClientBuilder.cookieJar(new JavaNetCookieJar(CookieHandler.getDefault()));

		httpClientBuilder.addNetworkInterceptor(new Interceptor()
		{
			@Override
			public Response intercept(Chain chain) throws IOException
			{
				Request request = chain.request();

				Response response = chain.proceed(request);
				Log.v(CLASS_TAG, String.format("(%s) %s", response.code(), request.url()));

				return response;
			}
		});

		httpClientBuilder.addInterceptor(new Interceptor()
		{
			@Override
			public Response intercept(Chain chain) throws IOException
			{
				Request request = chain.request();

				Request requestWithUserAgent = request.newBuilder()
					.header(HEADER_USER_AGENT, Utility.getUserAgent())
					.build();

				return chain.proceed(requestWithUserAgent);
			}
		});

		if (!username.equals(Const.EMPTY_STRING))
			httpClientBuilder.authenticator(new NtlmAuthHelper(username, password));

		return httpClientBuilder;
	}

	private static Request getRequest(String urlToGet) throws MalformedURLException
	{
		Request.Builder requestBuilder = new Request.Builder();

		URL url = new URL(urlToGet);
		requestBuilder.url(url);

		return requestBuilder.build();
	}
}
