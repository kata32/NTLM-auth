package it.methods.ntlmauth.lib;

import android.content.Context;
import android.os.AsyncTask;

import java.lang.ref.WeakReference;

public class AsyncTasks
{
	public static class test extends AsyncTask<Void, Void, String>
	{
		public interface Callbacks
		{
			void onTestCompleted(String result);
		}

		private final WeakReference<Context> m_contextReference;
		private final WeakReference<Callbacks> m_callbacksReference;

		private String m_url;
		private String m_username;
		private String m_password;

		public test(Context context, String url, String username, String password, Callbacks callbacksListner)
		{
			m_contextReference = new WeakReference<>(context);
			m_callbacksReference = new WeakReference<>(callbacksListner);
			m_url = url;
			m_username = username;
			m_password = password;
		}

		@Override
		protected String doInBackground(Void... params)
		{
			try
			{
				final Context context = m_contextReference.get();

				if (context == null || isCancelled())
					return Const.EMPTY_STRING;

				return HttpConn.getString(m_url, m_username, m_password);
			}
			catch (Exception e)
			{
				e.printStackTrace();
				return e.getMessage();
			}
		}

		@Override
		protected void onCancelled()
		{
			super.onCancelled();

			final Callbacks callbacks = m_callbacksReference.get();

			if (callbacks != null)
				callbacks.onTestCompleted(Const.EMPTY_STRING);
		}

		@Override
		protected void onPostExecute(String result)
		{
			final Callbacks callbacks = m_callbacksReference.get();

			if (callbacks != null)
				callbacks.onTestCompleted(result);
		}
	}
}

