package com.idea.xxx.ideaoftourism;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.here.android.mpa.common.GeoCoordinate;
import com.here.android.mpa.common.Image;
import com.here.android.mpa.common.OnEngineInitListener;
import com.here.android.mpa.mapping.Map;
import com.here.android.mpa.mapping.MapFragment;
import com.here.android.mpa.mapping.MapMarker;
import com.here.android.mpa.mapping.MapObject;
import com.here.android.mpa.search.DiscoveryResult;
import com.here.android.mpa.search.DiscoveryResultPage;
import com.here.android.mpa.search.ErrorCode;
import com.here.android.mpa.search.PlaceLink;
import com.here.android.mpa.search.ResultListener;
import com.here.android.mpa.search.SearchRequest;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

class SearchMapFragmentView {
    public static List<DiscoveryResult> s_ResultList;
    private MapFragment m_searchmapFragment;
    private Activity m_activity;
    private Map m_map;
    private Button m_placeDetailButton;
    private List<MapObject> m_mapObjectList = new ArrayList<>();

    public SearchMapFragmentView(SearchActivity searchActivity) {
        m_activity = searchActivity;
        initMapFragment();
        initSearchControlButton();
        initResultListButton();
    }

    @SuppressWarnings("deprecation")
    private MapFragment getMapFragment() {
        return (MapFragment) m_activity.getFragmentManager().findFragmentById(R.id.mapfragment);
    }

    @SuppressLint("MissingPermission")
    private void initMapFragment() {
        m_searchmapFragment = getMapFragment();
        boolean success = com.here.android.mpa.common.MapSettings.setIsolatedDiskCacheRootPath(
                m_activity.getExternalFilesDir(null) + File.separator + ".here-maps",
                "com.idea.xxx.ideaoftourism.MapService");
        if (!success) {
            Toast.makeText(m_activity.getApplicationContext(), "Unable to set isolated disk cache path.", Toast.LENGTH_LONG).show();
        } else {
            if (m_searchmapFragment != null) {
                m_searchmapFragment.init(error -> {
                    if (error == OnEngineInitListener.Error.NONE) {
                        LocationManager locationManager = (LocationManager) m_activity.getSystemService(Context.LOCATION_SERVICE);
                        LocationListener locationListener = new LocationListener() {
                            @Override
                            public void onLocationChanged(Location location) {
                                double longitude = location.getLongitude();
                                double latitude = location.getLatitude();
                                m_map = m_searchmapFragment.getMap();
                                m_map.setCenter(new GeoCoordinate(latitude, longitude, 0.0),
                                        Map.Animation.NONE);
                                m_map.setZoomLevel((m_map.getMinZoomLevel() + m_map.getMaxZoomLevel()) / 2);
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
                });
            }
        }
    }

    private void initResultListButton() {
        m_placeDetailButton = m_activity.findViewById(R.id.resultListBtn);
        m_placeDetailButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(m_activity, ResultListActivity.class);
                m_activity.startActivity(intent);
            }
        });
    }

    @SuppressLint("MissingPermission")
    private void initSearchControlButton() {
        try {
            LocationManager locationManager = (LocationManager)
                    m_activity.getSystemService(Context.LOCATION_SERVICE);
            LocationListener locationListener = new LocationListener() {
                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) {

                }

                @Override
                public void onProviderEnabled(String provider) {

                }

                @Override
                public void onProviderDisabled(String provider) {

                }

                @Override
                public void onLocationChanged(Location location) {
                    double lat = location.getLatitude();
                    double lon = location.getLongitude();
                    GeoCoordinate around = new GeoCoordinate(lat, lon);
                    Button searchRequestButton = m_activity.findViewById(R.id.searchRequestBtn);
                    searchRequestButton.setOnClickListener(v -> {
                        /* * Trigger a SearchRequest based on the current map center and search query */
                        int radius = 1000;
                        SearchRequest searchRequest = new SearchRequest("sights-museums");
                 /* *
                    eat-drink:
                    restaurant,
                    coffee-tea,
                    snacks-fast-food;
                    sights-museums;
                    leisure-outdoor;
                    natural-geographical;
                    * */
                        // searchRequest.setSearchArea(GeoCoordinate center = m_map.getCenter(), int radius = 1000);
                        searchRequest.setSearchArea(around, radius);
                        searchRequest.execute(discoveryResultPageListener);
                    });
                }
            };
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);



 /* DiscoveryRequest request =
    new SearchRequest("statue").setSearchCenter(around);

  // limit number of items in each result page to 10
  request.setCollectionSize(5);

  ErrorCode error = request.execute(new SearchRequestListener());
  if( error != ErrorCode.NONE ) {
    // Handle request error
    ...
  }*/
        } catch (IllegalArgumentException ex) {
            // Handle invalid create search request parameters
        }

    }

    private ResultListener<DiscoveryResultPage> discoveryResultPageListener = new ResultListener<DiscoveryResultPage>() {
        @Override public void onCompleted(DiscoveryResultPage discoveryResultPage, ErrorCode errorCode) {
            if (errorCode == ErrorCode.NONE) {
                /* No error returned,let's handle the results */
                m_placeDetailButton.setVisibility(View.VISIBLE);
		 	/* * The result is a DiscoveryResultPage object which represents a paginated collection of items.
		 	/*The PlaceLink can be used to retrieve place details by firing another PlaceRequest. */
                s_ResultList = discoveryResultPage.getItems();
                for (DiscoveryResult item : s_ResultList) {
                    /* Add a marker for each result of PlaceLink type. */
                    if (item.getResultType() == DiscoveryResult.ResultType.PLACE) {
                        PlaceLink placeLink = (PlaceLink) item;
                        addMarkerAtPlace(placeLink);
                    }
                }
            } else {
                Toast.makeText(m_activity, "ERROR:Discovery search request returned return error code+ " + errorCode, Toast.LENGTH_SHORT).show();
            }
        }
    };

    private void addMarkerAtPlace(PlaceLink placeLink) {
        MapMarker mapMarker = new MapMarker();
        mapMarker.setCoordinate(new GeoCoordinate(placeLink.getPosition())); m_map.addMapObject(mapMarker); m_mapObjectList.add(mapMarker); }
    private void cleanMap() {
        if (!m_mapObjectList.isEmpty()) {
            m_map.removeMapObjects(m_mapObjectList);
            m_mapObjectList.clear();
        }
        //m_placeDetailButton.setVisibility(View.GONE); }
    }
}

