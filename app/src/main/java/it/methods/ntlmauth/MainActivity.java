package it.methods.ntlmauth;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import java.net.CookieHandler;
import java.net.CookiePolicy;

import it.methods.ntlmauth.lib.CookieManagerHelper;

public class MainActivity extends AppCompatActivity
{
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		ViewPager viewPager = findViewById(R.id.tabs_container);

		TabLayout tabLayout = findViewById(R.id.tabs);
		tabLayout.setupWithViewPager(viewPager);
		viewPager.setAdapter(new SectionsPagerAdapter(getSupportFragmentManager(), getIntent().getExtras()));

		CookieHandler.setDefault(new CookieManagerHelper(CookiePolicy.ACCEPT_ALL));
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		Intent intent;

		switch (item.getItemId())
		{
			case R.id.menu_settings:
				intent = new Intent(this, SettingsActivity.class);
				startActivity(intent);
				return true;

			case R.id.menu_reset_cookie:
				((CookieManagerHelper)CookieHandler.getDefault()).resetCookie();
				return true;

			default:
				return super.onOptionsItemSelected(item);
		}
	}

	private class SectionsPagerAdapter extends FragmentPagerAdapter
	{
		private final Bundle m_bundle;
		private int m_pageCount;

		SectionsPagerAdapter(FragmentManager fm, Bundle bundle)
		{
			super(fm);

			m_bundle = bundle;
			m_pageCount = 2;
		}

		@Override
		public Fragment getItem(int position)
		{
			Fragment fragment = null;

			switch (position)
			{
				case 0:
					fragment = new HttpConnectionFragment();
					break;

				case 1:
					fragment = new WebViewFragment();
					break;
			}

			if (fragment != null)
				fragment.setArguments(m_bundle);

			return fragment;
		}

		@Override
		public int getCount()
		{
			return m_pageCount;
		}

		@Override
		public CharSequence getPageTitle(int position)
		{
			switch (position)
			{
				case 0:
					return "okhttp";

				case 1:
					return "Web View";
			}

			return null;
		}
	}
}
