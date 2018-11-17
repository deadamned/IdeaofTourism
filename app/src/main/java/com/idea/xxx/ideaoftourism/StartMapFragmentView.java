package com.idea.xxx.ideaoftourism;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

import com.here.android.mpa.common.GeoCoordinate;

import com.here.android.mpa.common.OnEngineInitListener;

import com.here.android.mpa.mapping.Map;
import com.here.android.mpa.mapping.MapFragment;
import com.here.android.mpa.mapping.MapMarker;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.Image;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.widget.Toast;

public class StartMapFragmentView {
    private MapFragment m_startmapFragment;
    private Activity m_activity;
    private Map m_map;

    public StartMapFragmentView(Activity activity) {
        m_activity = activity;
        initMapFragment();
    }

    @SuppressWarnings("deprecation")
    private MapFragment getMapFragment() {
        return (MapFragment) m_activity.getFragmentManager().findFragmentById(R.id.map);
    }


    private void initMapFragment() {
        m_startmapFragment = getMapFragment();
        boolean success = com.here.android.mpa.common.MapSettings.setIsolatedDiskCacheRootPath(
                m_activity.getExternalFilesDir(null) + File.separator + ".here-maps",
                "com.idea.xxx.ideaoftourism.MapService");
        if (!success) {
            Toast.makeText(m_activity.getApplicationContext(), "Unable to set isolated disk cache path.", Toast.LENGTH_LONG).show();
        } else {
            if (m_startmapFragment != null) {
                m_startmapFragment.init(new OnEngineInitListener() {
                    @SuppressLint("MissingPermission")
                    @Override
                    public void onEngineInitializationCompleted(OnEngineInitListener.Error error) {
                        if (error == Error.NONE) {
                            LocationManager locationManager = (LocationManager) m_activity.getSystemService(Context.LOCATION_SERVICE);
                            LocationListener locationListener = new LocationListener() {
                                @Override
                                public void onLocationChanged(Location location) {
                                    double longitude = location.getLongitude();
                                    double latitude = location.getLatitude();
                                    m_map = m_startmapFragment.getMap();
                                    m_map.setCenter(new GeoCoordinate(latitude, longitude, 0.0),
                                            Map.Animation.NONE);
                                    m_map.setZoomLevel((m_map.getMinZoomLevel() + m_map.getMaxZoomLevel())/13);
                                    MapMarker mm = new MapMarker();
                                    mm.setCoordinate(new GeoCoordinate(latitude, longitude));
                                    m_map.addMapObject(mm);

                                }

                                @Override
                                public void onStatusChanged(String provider, int status, Bundle extras) {

                                }

                                @Override
                                public void onProviderEnabled(String provider) {

                                }

                                @Override
                                public void onProviderDisabled(String provider) {

                                }
                            };
                            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 500, locationListener);
                            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 500, locationListener);


                        } else {
                            Toast.makeText(m_activity,
                                    "ERROR: Cannot initialize Map with error " + error,
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
        }
    }
}