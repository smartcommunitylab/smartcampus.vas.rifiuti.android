/*******************************************************************************
 * Copyright 2012-2013 Trento RISE
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *        http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either   express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package it.smartcampuslab.riciclo.utils;

import it.smartcampuslab.riciclo.model.PuntoRaccolta;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

/**
 * 
 * LocationHelper
 * 
 * That helper finds the device location using network and GPS. It's possible to
 * add and remove custom LocationListeners one by one or batch. The changes will
 * propagate.
 * 
 * Usage: create an instance of LocationHelper; start the localization with
 * start(); stop the localization with stop().
 * 
 * @author zambotti
 * 
 */

public class LocationHelper {

	private Context mContext;
	private LocationManager mLocationManager;
	private LocationListener mLocationListener;
	private List<LocationListener> mLocationListenersList = new ArrayList<LocationListener>();
	private Location mLocation;

	public LocationHelper(Context mContext) {
		this.mContext = mContext;
	}

	public void start() {
		if (mLocationListener == null) {
			mLocationListener = new DefaultLocationListener();
		}

		if (mLocationManager == null) {
			mLocationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
		}

		Location mLastKnownLocation = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
		if (mLastKnownLocation == null) {
			mLastKnownLocation = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
		}

		if (isBetterLocation(null, mLastKnownLocation)) {
			mLocation = mLastKnownLocation;
		}

		mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, mLocationListener);
		mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, mLocationListener);
	}

	public void stop() {
		mLocationManager.removeUpdates(mLocationListener);
	}

	public Location getLocation() {
		return mLocation;
	}

	public boolean addLocationListener(LocationListener ll) {
		if (ll != null && !mLocationListenersList.contains(ll)) {
			return mLocationListenersList.add(ll);
		}
		return false;
	}

	public boolean addLocationListenersList(List<LocationListener> locationListenersList) {
		for (LocationListener ll : locationListenersList) {
			if (addLocationListener(ll) == false) {
				return false;
			}
		}
		return true;
	}

	public boolean removeLocationListener(LocationListener ll) {
		return mLocationListenersList.remove(ll);
	}

	public boolean removeLocationListenersList(List<LocationListener> locationListenersList) {
		for (LocationListener ll : locationListenersList) {
			removeLocationListener(ll);
		}
		return true;
	}

	/*
	 * Internal classes
	 */
	private class DefaultLocationListener implements LocationListener {
		@Override
		public void onLocationChanged(Location location) {
			if (isBetterLocation(location, mLocation)) {
				mLocation = location;
				// propagation to custom listeners
				for (LocationListener ll : mLocationListenersList) {
					ll.onLocationChanged(location);
				}
			}
		}

		@Override
		public void onProviderDisabled(String provider) {
			// propagation to custom listeners
			for (LocationListener ll : mLocationListenersList) {
				ll.onProviderDisabled(provider);
			}
		}

		@Override
		public void onProviderEnabled(String provider) {
			// propagation to custom listeners
			for (LocationListener ll : mLocationListenersList) {
				ll.onProviderEnabled(provider);
			}
		}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
			// propagation to custom listeners
			for (LocationListener ll : mLocationListenersList) {
				ll.onStatusChanged(provider, status, extras);
			}
		}
	};

	/*
	 * Internal location evaluation methods
	 */
	// private static final int FRESHNESS = 1000 * 60 * 2;
	private static final int FRESHNESS = 1000 * 60 * 1;
	private static final int OLDNESS = 1000 * 60 * 120;

	/**
	 * Determines whether one Location reading is better than the current
	 * Location fix
	 * 
	 * @param location
	 *            The new Location that you want to evaluate
	 * @param currentBestLocation
	 *            The current Location fix, to which you want to compare the new
	 *            one
	 */
	private boolean isBetterLocation(Location location, Location currentBestLocation) {
		if (currentBestLocation == null) {
			// A new location is always better than no location
			return true;
		}

		if (location == null) {
			if (currentBestLocation.getTime() > System.currentTimeMillis() - OLDNESS) {
				return true;
			} else {
				return false;
			}
		}

		// Check whether the new location fix is newer or older
		long timeDelta = location.getTime() - currentBestLocation.getTime();
		boolean isSignificantlyNewer = timeDelta > FRESHNESS;
		boolean isSignificantlyOlder = timeDelta < -FRESHNESS;
		boolean isNewer = timeDelta > 0;

		// If it's been more than FRESHNESS since the current location, use
		// the new location
		// because the user has likely moved
		if (isSignificantlyNewer) {
			return true;
			// If the new location is more than FRESHNESS older, it must be
			// worse
		} else if (isSignificantlyOlder) {
			return false;
		}

		// Check whether the new location fix is more or less accurate
		int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
		boolean isLessAccurate = accuracyDelta > 0;
		boolean isMoreAccurate = accuracyDelta < 0;
		boolean isSignificantlyLessAccurate = accuracyDelta > 200;

		// Check if the old and new location are from the same provider
		boolean isFromSameProvider = isSameProvider(location.getProvider(), currentBestLocation.getProvider());

		// Determine location quality using a combination of timeliness and
		// accuracy
		if (isMoreAccurate) {
			return true;
		} else if (isNewer && !isLessAccurate) {
			return true;
		} else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
			return true;
		}
		return false;
	}

	/** Checks whether two providers are the same */
	private boolean isSameProvider(String provider1, String provider2) {
		if (provider1 == null) {
			return provider2 == null;
		}
		return provider1.equals(provider2);
	}

	public static class PRDistanceComparator implements Comparator<PuntoRaccolta> {
		private Location myLocation = null;

		/**
		 * @param myLocation
		 */
		public PRDistanceComparator(Location myLocation) {
			super();
			this.myLocation = myLocation;
		}

		@Override
		public int compare(PuntoRaccolta lhs, PuntoRaccolta rhs) {
			if (myLocation == null)
				return lhs.getIndirizzo().compareTo(rhs.getIndirizzo());
			Location p1Location = new Location("");
			p1Location.setLatitude(lhs.location()[0]);
			p1Location.setLongitude(lhs.location()[1]);
			float p1distance = myLocation.distanceTo(p1Location);

			Location p2Location = new Location("");
			p2Location.setLatitude(rhs.location()[0]);
			p2Location.setLongitude(rhs.location()[1]);
			float p2distance = myLocation.distanceTo(p2Location);

			return ((Float) p1distance).compareTo((Float) p2distance);
		}
	}

}
