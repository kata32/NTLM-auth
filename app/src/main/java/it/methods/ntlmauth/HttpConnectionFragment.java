package it.methods.ntlmauth;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;

import it.methods.ntlmauth.lib.AsyncTasks;
import it.methods.ntlmauth.lib.Const;

public class HttpConnectionFragment extends Fragment implements AsyncTasks.test.Callbacks
{
	private Switch m_useAuth;
	private TextView m_response;

	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
	{
		View view = inflater.inflate(R.layout.fragment_http_connection, container, false);

		m_useAuth = view.findViewById(R.id.useAuth);
		m_response = view.findViewById(R.id.response);

		Button go = view.findViewById(R.id.go);
		go.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View view)
			{
				SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getContext());

				String url = settings.getString(Const.SETTING_URL, Const.SETTING_URL);
				String username = Const.EMPTY_STRING;
				String password = Const.EMPTY_STRING;

				if (m_useAuth.isChecked())
				{
					username = settings.getString(Const.SETTING_USERNAME, Const.EMPTY_STRING);
					password = settings.getString(Const.SETTING_PASSWORD, Const.EMPTY_STRING);
				}

				new AsyncTasks.test(getContext(), url, username, password,HttpConnectionFragment.this)
					.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);

			}
		});

		return view;
	}

	@Override
	public void onTestCompleted(String result)
	{
		m_response.setText(result);
	}
}
