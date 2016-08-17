package com.gigaStorm.twinRinks;

import android.app.ActionBar;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

// import com.viewpagerindicator.TitlePageIndicator;

/**
 * <code>Activity_Main</code> loads the three main fragments into a frame
 * layout.
 *
 * @author Andrew Mass
 * @see FragmentActivity
 */
public class Activity_Main extends FragmentActivity {

	private ActionBar actionBar;

	private Data_MemoryManager memoryManager;

	private ViewPager viewPager;

	private PagerAdapter pagerAdapter;

	private static final String TAG = "Activity_Main";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.layout_main);

		String SubTag = "OnCreate(): ";
		String androidId = Settings.Secure.getString(this.getContentResolver(),
				android.provider.Settings.Secure.ANDROID_ID);
		if (androidId == null || androidId.equals("9774d56d682e549c")) {
			// We are running on the emulator. Debugging should be ON.
			Logger.e(TAG, SubTag + "Enabling VERBOSE debugging. androidID = "
					+ androidId);
			Logger.enableLogging(Log.VERBOSE);
		} else {
			// We are running on a phone. Debugging should be OFF.
			Logger.e(TAG, SubTag + "Enabling ERRORS only debugging. androidID = "
					+ androidId);
			Logger.enableLogging(Log.ERROR);
		}
		// If there is a need to debug the app, uncomment the following line
		//Logger.enableLogging(Log.VERBOSE);

		getWindow().setBackgroundDrawableResource(android.R.color.black);

		actionBar = getActionBar();
		actionBar.setHomeButtonEnabled(false);
		actionBar.setDisplayShowTitleEnabled(false);
	}

	@Override
	protected void onResume() {
		viewPager = (ViewPager) findViewById(R.id.viewPager_main_main);
		pagerAdapter = new ScreenSlidePagerAdapter(getSupportFragmentManager());
		viewPager.setAdapter(pagerAdapter);
/*
    TitlePageIndicator titleIndicator = (TitlePageIndicator) findViewById(R.id.viewPagerIndicator);
    titleIndicator.setViewPager(viewPager);
    titleIndicator.setBackgroundColor(getResources().getColor(
        R.color.vpi__background_holo_dark));
    titleIndicator
        .setFooterIndicatorStyle(TitlePageIndicator.IndicatorStyle.None);
    titleIndicator.setCurrentItem(viewPager.getCurrentItem());
*/
		super.onResume();
	}

	@Override
	public void onBackPressed() {
		if (viewPager.getCurrentItem() == 0) {
			super.onBackPressed();
		} else {
			viewPager.setCurrentItem(viewPager.getCurrentItem() - 1);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu_main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		String SubTag = "onOptionsItemSelected(): ";
		switch (item.getItemId()) {
			case android.R.id.home:
				startActivity(new Intent(this, Activity_Main.class));
				return true;

			case R.id.menu_refresh:
				memoryManager = new Data_MemoryManager(this);
				memoryManager.refreshData();
				return true;

			case R.id.menu_about:
				startActivity(new Intent(this, Activity_About.class));
				return true;

			case R.id.menu_addToCalendar:
				if (Build.VERSION.SDK_INT >= 14) {
					if ((checkSelfPermission(android.Manifest.permission.READ_CALENDAR) != PackageManager.PERMISSION_GRANTED) ||
							checkSelfPermission(android.Manifest.permission.WRITE_CALENDAR) != PackageManager.PERMISSION_GRANTED) {

						requestPermissions(new String[]{android.Manifest.permission.READ_CALENDAR},
								REQUEST_CODE_ASK_PERMISSIONS);

						Logger.e(TAG, SubTag + "No permissions for updating calendar. Requesting access");
					} else {
						Data_CalendarManager man = new Data_CalendarManager(this);
						man.saveGamesToCalendar();
					}
				}
				return true;

			case R.id.menu_settings:
				startActivity(new Intent(this, Activity_Settings.class));
				return true;

			default:
				return super.onOptionsItemSelected(item);
		}
	}


	private class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {
		public ScreenSlidePagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int position) {
			switch (position) {
				case 0:
					return new Fragment_Upcoming();
				case 1:
					return new Fragment_Schedule();
				case 2:
					return new Fragment_SignIn();
				default:
					return new Fragment_Upcoming();
			}
		}

		@Override
		public CharSequence getPageTitle(int position) {
			switch (position) {
				case 0:
					return "Upcoming";
				case 1:
					return "Schedule";
				case 2:
					return "Sub Sign-In";
				default:
					return "Error";
			}
		}

		@Override
		public int getCount() {
			return 3;
		}
	}
}
