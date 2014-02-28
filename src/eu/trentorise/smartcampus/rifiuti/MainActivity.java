package eu.trentorise.smartcampus.rifiuti;

import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;
import eu.trentorise.smartcampus.rifiuti.data.RifiutiHelper;

public class MainActivity extends ActionBarActivity {

	private int mContentFrameId;
	private DrawerLayout mDrawerLayout;
	private ListView mDrawerList;
	private ActionBarDrawerToggle mDrawerToggle;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(
				Window.FEATURE_INDETERMINATE_PROGRESS);
		
		setContentView(R.layout.activity_main);

		mContentFrameId = R.id.content_frame;
		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		mDrawerList = (ListView) findViewById(R.id.left_drawer);

		mDrawerList.setAdapter(new DrawerArrayAdapter(this, R.layout.drawer_entry, getResources().getStringArray(
				R.array.drawer_entries_strings)));
		mDrawerList.setOnItemClickListener(new DrawerItemClickListener());

		mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.drawable.ic_drawer, R.string.drawer_open,
				R.string.drawer_close) {
			public void onDrawerClosed(View view) {
				super.onDrawerClosed(view);
			}

			public void onDrawerOpened(View drawerView) {
				super.onDrawerOpened(drawerView);
			}
		};

		mDrawerLayout.setDrawerListener(mDrawerToggle);

		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setHomeButtonEnabled(true);

		try {
			RifiutiHelper.init(this.getApplicationContext());
			System.err.println(RifiutiHelper.readTipologiaRaccolta());
			System.err.println(RifiutiHelper.readTipologiaRifiuti());
			System.err.println(RifiutiHelper.getPuntiRaccolta());
			System.err.println(RifiutiHelper.getPuntiRaccoltaPerTipoRaccolta("CARTA, CARTONE E CARTONI PER BEVANDE"));
			System.err.println(RifiutiHelper.getPuntiRaccoltaPerTipoRifiuto("CARTONI PER BEVANDE"));
			System.err.println(RifiutiHelper.getRifiutoPerTipoRaccolta("CARTA, CARTONE E CARTONI PER BEVANDE"));
			System.err.println(RifiutiHelper.getRifiutoPerTipoRifiuti("CARTONI PER BEVANDE"));
			System.err.println(RifiutiHelper.getTipoRifiuto("Acquario"));
			System.err.println(RifiutiHelper.getCalendars(RifiutiHelper.getPuntiRaccolta().get(0)));
		} catch (Exception e) {
			Toast.makeText(this, R.string.app_failure_setup, Toast.LENGTH_LONG).show();
			e.printStackTrace();
			finish();
		}
		
		
		loadFragment(0);
		
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		// Sync the toggle state after onRestoreInstanceState has occurred.
		mDrawerToggle.syncState();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (mDrawerToggle.onOptionsItemSelected(item)) {
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	private void loadFragment(int position) {
		Fragment fragment = null;

		switch (position) {
		case 0:
			fragment = new HomeFragment();
			break;
		case 5:
			fragment = new ProfilesListFragment();
			break;
		default:
			fragment = new DummyFragment();
			break;
		}

		if (fragment != null) {
			// Insert the fragment by replacing any existing fragment
			getSupportFragmentManager().beginTransaction().replace(mContentFrameId, fragment).commit();
			// Highlight the selected item, update the title, close the drawer
			mDrawerList.setItemChecked(position, true);
			// setTitle(mPlanetTitles[position]);
			mDrawerLayout.closeDrawer(mDrawerList);
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

}
