package it.smartcampuslab.riciclo;

import it.smartcampuslab.riciclo.custom.ExpandedListView;
import it.smartcampuslab.riciclo.data.RifiutiHelper;
import it.smartcampuslab.riciclo.model.Profile;
import it.smartcampuslab.riciclo.notifications.AlarmSetter;
import it.smartcampuslab.riciclo.utils.ArgUtils;
import it.smartcampuslab.riciclo.utils.PreferenceUtils;
import it.smartcampuslab.riciclo.utils.onBackListener;

import java.util.List;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends ActionBarActivity implements ActionBar.OnNavigationListener {

	private int mContentFrameId;
	private DrawerLayout mDrawerLayout;
	private ExpandedListView mDrawerList;
	private ActionBarDrawerToggle mDrawerToggle;
	private RadioGroup rg;

	private Bundle intentBundle;

	private boolean mProfileGroupManage = true;

	// private TutorialHelper mTutorialHelper = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
//		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.activity_main);
		mContentFrameId = R.id.content_frame;

		if (getIntent().getExtras() != null) {
			intentBundle = getIntent().getExtras();
		}

		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		mDrawerList = (ExpandedListView) findViewById(R.id.left_drawer);

		mDrawerList.setAdapter(new DrawerArrayAdapter(this, R.layout.drawer_entry,
				getResources().getStringArray(R.array.drawer_entries_strings)));
		mDrawerList.setOnItemClickListener(new DrawerItemClickListener());

		// mTutorialHelper = new ListViewTutorialHelper(this,
		// mNavDrawerTutorialProvider);

		addNavDrawerButton();

		rg = (RadioGroup) findViewById(R.id.profile_rg);
		rg.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				if (!mProfileGroupManage) {
					mProfileGroupManage = true;
					return;
				}
				Integer i = (Integer) findViewById(R.id.profile_rg).findViewById(checkedId).getTag();
				if (i != null) {
					try {
						PreferenceUtils.setCurrentProfilePosition(MainActivity.this, i);
						setCurrentProfile(null);
						// updateActionBarSubtitle();
						Fragment fragment = getSupportFragmentManager().findFragmentById(mContentFrameId);
						if (fragment != null) {
							Fragment newFragment = getFragmentToReload(fragment);
							if (newFragment != null) {
								getSupportFragmentManager().beginTransaction().replace(mContentFrameId, newFragment).commit();
							}
						}
						// mDrawerLayout.closeDrawer(findViewById(R.id.drawer_wrapper));
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		});
		try {
			RifiutiHelper.init(this.getApplicationContext());
			RifiutiHelper.validateProfiles(this, PreferenceUtils.getProfiles(this));
			if (PreferenceUtils.getProfiles(this).isEmpty()) {
				lockDrawer();
				loadFragment(8);
				Toast.makeText(this, getString(R.string.toast_no_prof), Toast.LENGTH_SHORT).show();
			} else {
				populateProfilesList(true);
			}
		} catch (Exception e) {
			Toast.makeText(this, R.string.app_failure_setup, Toast.LENGTH_LONG).show();
			e.printStackTrace();
			finish();
		}

		/*
		 * use this to start and trigger a service
		 */
		// Intent i = new Intent(getApplicationContext(),
		// NotificationsService.class);
		// getApplicationContext().startService(i);
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		// Sync the toggle state after onRestoreInstanceState has occurred.
		mDrawerToggle.syncState();
	}

	@Override
	protected void onResume() {
		super.onResume();
		RifiutiHelper.locationHelper.start();

		AlarmSetter.setAlarmForNotificationsService(getApplicationContext());

		// if the activity is open by the notification: stop the notifications
		// service
		// if (intentBundle != null &&
		// intentBundle.containsKey(ArgUtils.ARGUMENT_CALENDAR_TOMORROW)
		// && intentBundle.containsKey(ArgUtils.ARGUMENT_PROFILE)) {
		// // stop notifications service
		// boolean isStopped = getApplicationContext().stopService(
		// new Intent(getApplicationContext(), NotificationsService.class));
		// Log.e("MainActivity", "Service stopped? " + isStopped);
		// }
	}

	@Override
	protected void onPause() {
		super.onPause();
		RifiutiHelper.locationHelper.stop();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		// AlarmSetter.cancelAlarmForNotificationsService();
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);

		if (intent.getExtras() != null) {
			intentBundle = intent.getExtras();
		}

		populateProfilesList(true);
	}

	@Override
	public void onBackPressed() {
		Fragment f = getSupportFragmentManager().findFragmentById(R.id.content_frame);
		if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
			// mDrawerLayout.closeDrawer(GravityCompat.START);
			hideDrawerIndicator();
		} else if (f instanceof onBackListener) {
			((onBackListener) f).onBack();
		} else if (!(f instanceof HomeFragment)) {
			loadFragment(0);
		} else {
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setView(View.inflate(this, R.layout.dialog_closing, null));
			builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					MainActivity.super.onBackPressed();
				}
			});
			builder.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					// User cancelled the dialog
				}
			});
			// Create the AlertDialog object and return it
			builder.create().show();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// we can't open the drawer if it's locked
		if (mDrawerLayout.getDrawerLockMode(Gravity.START) != DrawerLayout.LOCK_MODE_LOCKED_CLOSED) {
			if (mDrawerToggle.onOptionsItemSelected(item)) {
				return true;
			}
		}

		return super.onOptionsItemSelected(item);
	}

	@Override
	public boolean onNavigationItemSelected(int arg0, long arg1) {
		try {
			PreferenceUtils.setCurrentProfilePosition(this, arg0);
			setCurrentProfile(null);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	private void setCurrentProfile(Integer position) {
		if (position != null) {
			Profile profile = PreferenceUtils.getProfile(this, position);
			PreferenceUtils.setCurrentProfilePosition(MainActivity.this, position);
			RifiutiHelper.setProfile(profile);
			rg.check(rg.getChildAt(position).getId());
			Toast.makeText(getApplicationContext(),
					getResources().getString(R.string.profile_changed_by_intent, profile.getName()), Toast.LENGTH_LONG).show();
		} else if (PreferenceUtils.getCurrentProfilePosition(this) < 0) {
			RifiutiHelper.setProfile(PreferenceUtils.getProfile(this, 0));
		} else {
			RifiutiHelper.setProfile(PreferenceUtils.getProfile(this, PreferenceUtils.getCurrentProfilePosition(this)));
		}
		updateActionBarSubtitle();
	}

	public void populateProfilesList(boolean loadHome) {
		List<Profile> profiles = PreferenceUtils.getProfiles(this);
		mProfileGroupManage = false;
		if (profiles.size() > 1) {
			findViewById(R.id.curr_profile_tv).setVisibility(View.GONE);
			RadioGroup rg = (RadioGroup) findViewById(R.id.profile_rg);
			rg.setVisibility(View.VISIBLE);
			rg.removeAllViews();
			int curr = PreferenceUtils.getCurrentProfilePosition(this);
			int i = 0;
			for (Profile p : profiles) {
				RadioButton rb = new RadioButton(this);
				rb.setText(p.getName());
				rb.setTextColor(getResources().getColor(android.R.color.white));
				rb.setTag(i);
				rg.addView(rb);
				if (i == curr) {
					rb.setChecked(true);
				} else {
					rb.setChecked(false);
				}
				i++;
			}
		} else {
			((TextView) findViewById(R.id.curr_profile_tv)).setText(profiles.get(0).getName());
			findViewById(R.id.curr_profile_tv).setVisibility(View.VISIBLE);
			findViewById(R.id.profile_rg).setVisibility(View.GONE);
		}
		mDrawerToggle.syncState();

		Integer profilePosition = null;
		if (intentBundle != null && intentBundle.containsKey(ArgUtils.ARGUMENT_NOTIFICATION_PROFILE)) {
			Profile intentBundleProfile = (Profile) intentBundle.getSerializable(ArgUtils.ARGUMENT_NOTIFICATION_PROFILE);
			for (int pc = 0; pc < profiles.size(); pc++) {
				Profile p = profiles.get(pc);
				if (p.toString().equals(intentBundleProfile.toString())) {
					profilePosition = pc;
					break;
				}
			}
		}
		setCurrentProfile(profilePosition);

		if (loadHome) {
			unlockDrawer();
			showDrawerIndicator();
			loadFragment(0);
			// updateActionBarSubtitle();
		}
	}

	private void updateActionBarSubtitle() {
		getSupportActionBar()
				.setSubtitle(PreferenceUtils.getProfiles(this).get(PreferenceUtils.getCurrentProfilePosition(this)).getName());
	}

	private void addNavDrawerButton() {
		mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.drawable.ic_drawer, R.string.drawer_open,
				R.string.drawer_close) {
			public void onDrawerClosed(View view) {
				super.onDrawerClosed(view);
			}

			public void onDrawerOpened(View drawerView) {
				super.onDrawerOpened(drawerView);
				if (RifiutiHelper.isFirstLaunchMenu(MainActivity.this)) {
					RifiutiHelper.disableFirstLaunchMenu(MainActivity.this);
				}
			}

			public void onDrawerSlide(View drawerView, float slideOffset) {
				super.onDrawerSlide(drawerView, slideOffset);
				if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
					mDrawerLayout.bringChildToFront(drawerView);
					mDrawerLayout.requestLayout();
					mDrawerLayout.setScrimColor(Color.TRANSPARENT);
				}
			}
		};

		mDrawerLayout.setDrawerListener(mDrawerToggle);

		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setHomeButtonEnabled(true);
	}

	/**
	 * @param fragment
	 * @return
	 */
	private Fragment getFragmentToReload(Fragment fragment) {
		if (fragment instanceof ProfilesListFragment || fragment instanceof FeedbackFragment
				|| fragment instanceof ContactsListFragment || fragment instanceof InfoFragment) {
			return null;
		}

		try {
			return (Fragment) fragment.getClass().newInstance();
		} catch (Exception e) {
			return fragment;
		}
	}

	private void loadFragment(int position) {
		Fragment fragment = null;
		Intent intent = null;

		switch (position) {
		case 0:
			fragment = new HomeFragment();
			if (intentBundle != null && intentBundle.containsKey(ArgUtils.ARGUMENT_CALENDAR_TOMORROW)) {
				fragment.setArguments(intentBundle);
			}
			break;
		case 1:
			fragment = new MapFragment();
			break;
		case 2:
			fragment = new TipoRaccoltaListFragment();
			break;
		case 3:
			fragment = new ProfilesListFragment();
			break;
		case 4:
			fragment = new FeedbackFragment();
			break;
		case 5:
			fragment = new ContactsListFragment();
			break;
		case 6:
			prepareTutorial();
			fragment = new HomeFragment();
			break;
		case 7:
			// fragment = new InfoFragment();
			intent = new Intent(getApplicationContext(), InfoActivity.class);
			break;
		case 8:
			fragment = new ProfileFragment();
			break;
		default:
			fragment = new DummyFragment();
			break;
		}

		if (fragment != null) {
			// Insert the fragment by replacing any existing fragment
			getSupportFragmentManager().beginTransaction().replace(mContentFrameId, fragment).commit();
			// Highlight the selected item, close the drawer
			mDrawerList.setItemChecked(position, true);
			// setTitle(mPlanetTitles[position]);
			mDrawerLayout.closeDrawer(findViewById(R.id.drawer_wrapper));
			// erase intent
			intentBundle = null;
		} else if (intent != null) {
			startActivity(intent);
		}
	}

	private void prepareTutorial() {
		RifiutiHelper.resetTutorialDoveLoButto(getApplicationContext());
	}

	// USE WITH CARE!!
	public void lockDrawer() {
		if (mDrawerLayout != null) {
			mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
			getSupportActionBar().setHomeButtonEnabled(false);
		}
	}

	// USE WITH CARE!!
	public void unlockDrawer() {
		if (mDrawerLayout != null) {
			mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
			getSupportActionBar().setHomeButtonEnabled(true);
		}
	}

	// USE WITH CARE!!
	public void hideDrawerIndicator() {
		if (mDrawerToggle != null) {
			mDrawerToggle.setDrawerIndicatorEnabled(false);
		}
	}

	// USE WITH CARE!!
	public void showDrawerIndicator() {
		if (mDrawerToggle != null) {
			mDrawerToggle.setDrawerIndicatorEnabled(true);
		}
	}

	/**
	 * Drawer item click listener
	 */
	private class DrawerItemClickListener implements ListView.OnItemClickListener {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			loadFragment(position);
		}
	}

	@Override
	protected void onActivityResult(int arg0, int arg1, Intent arg2) {
		Fragment f = getSupportFragmentManager().findFragmentById(mContentFrameId);
		if (f != null && f instanceof DoveLoButtoFragment && f.isVisible()) {
			getSupportFragmentManager().findFragmentById(mContentFrameId).onActivityResult(arg0, arg1, arg2);
		}
		super.onActivityResult(arg0, arg1, arg2);
	}

}
