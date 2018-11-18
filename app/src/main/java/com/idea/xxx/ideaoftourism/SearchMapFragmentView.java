package com.idea.xxx.ideaoftourism;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.PointF;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.here.android.mpa.common.GeoCoordinate;
import com.here.android.mpa.common.Image;
import com.here.android.mpa.common.OnEngineInitListener;
import com.here.android.mpa.common.ViewObject;
import com.here.android.mpa.mapping.Map;
import com.here.android.mpa.mapping.MapFragment;
import com.here.android.mpa.mapping.MapGesture;
import com.here.android.mpa.mapping.MapMarker;
import com.here.android.mpa.mapping.MapObject;
import com.here.android.mpa.mapping.MapOverlay;
import com.here.android.mpa.search.DiscoveryResult;
import com.here.android.mpa.search.DiscoveryResultPage;
import com.here.android.mpa.search.ErrorCode;
import com.here.android.mpa.search.Place;
import com.here.android.mpa.search.PlaceLink;
import com.here.android.mpa.search.PlaceRequest;
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
    private LinearLayout m_placeDetailLayout;
    private TextView m_placeName;
    private TextView m_placeLocation;

    private void initUIElements() {
        /*
         * An overlay layout will pop up to display some place details.To simplify the logic, this
         * layout is currently not being handled for screen rotation event.It disappears if the
         * screen is being rotated.
         */
        m_placeDetailLayout = m_activity.findViewById(R.id.placeDetailLayout);
        m_placeDetailLayout.setVisibility(View.GONE);

        m_placeName = m_activity.findViewById(R.id.placeName);
        m_placeLocation = m_activity.findViewById(R.id.placeLocation);

        Button closePlaceDetailButton = m_activity.findViewById(R.id.closeLayoutButton);
        closePlaceDetailButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (m_placeDetailLayout.getVisibility() == View.VISIBLE) {
                    m_placeDetailLayout.setVisibility(View.GONE);
                }
            }
        });
    }

    public SearchMapFragmentView(SearchActivity searchActivity) {
        m_activity = searchActivity;
        initUIElements();
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
                                m_map.setZoomLevel((m_map.getMinZoomLevel() + m_map.getMaxZoomLevel()) / 13);
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
        GeoCoordinate geoCoordinate = new GeoCoordinate(placeLink.getPosition());
        mapMarker.setCoordinate(geoCoordinate);
        m_map.addMapObject(mapMarker);
        m_mapObjectList.add(mapMarker);

        TextView textView = new TextView(m_activity.getApplicationContext());
        textView.setText(placeLink.getTitle());
        MapOverlay mapOverlay = new MapOverlay(textView, geoCoordinate);
        mapOverlay.setAnchorPoint(new PointF(0, -70));
        m_map.addMapOverlay(mapOverlay);

        MapGesture.OnGestureListener onGestureListener = new MapGesture.OnGestureListener() {
            @Override
            public void onPanStart() {

            }

            @Override
            public void onPanEnd() {

            }

            @Override
            public void onMultiFingerManipulationStart() {

            }

            @Override
            public void onMultiFingerManipulationEnd() {

            }

            @Override
            public boolean onMapObjectsSelected(List<ViewObject> list) {
                for (ViewObject viewObject : list) {
                    MapObject mapObject = (MapObject) viewObject;

                    if (mapObject.equals(mapMarker)) {
                        PlaceRequest placeRequest = placeLink.getDetailsRequest();
                        placeRequest.execute(m_placeResultListener);
                        return true;
                    }
                }
                return false;
            }

            @Override
            public boolean onTapEvent(PointF pointF) {
                return false;
            }

            @Override
            public boolean onDoubleTapEvent(PointF pointF) {
                return false;
            }

            @Override
            public void onPinchLocked() {

            }

            @Override
            public boolean onPinchZoomEvent(float v, PointF pointF) {
                return false;
            }

            @Override
            public void onRotateLocked() {

            }

            @Override
            public boolean onRotateEvent(float v) {
                return false;
            }

            @Override
            public boolean onTiltEvent(float v) {
                return false;
            }

            @Override
            public boolean onLongPressEvent(PointF pointF) {
                return false;
            }

            @Override
            public void onLongPressRelease() {

            }

            @Override
            public boolean onTwoFingerTapEvent(PointF pointF) {
                return false;
            }
        };
        getMapFragment().getMapGesture().addOnGestureListener(onGestureListener, 0, true);
    }
    private void cleanMap() {
        if (!m_mapObjectList.isEmpty()) {
            m_map.removeMapObjects(m_mapObjectList);
            m_mapObjectList.clear();
        }
        //m_placeDetailButton.setVisibility(View.GONE); }
    }

    private ResultListener<Place> m_placeResultListener = new ResultListener<Place>() {
        @Override
        public void onCompleted(Place place, ErrorCode errorCode) {
            if (errorCode == ErrorCode.NONE) {
                /*
                 * No error returned,let's show the name and location of the place that just being
                 * selected.Additional place details info can be retrieved at this moment as well,
                 * please refer to the HERE Android SDK API doc for details.
                 */
                m_placeDetailLayout.setVisibility(View.VISIBLE);
                m_placeName.setText(place.getName());
                GeoCoordinate geoCoordinate = place.getLocation().getCoordinate();
                m_placeLocation.setText(geoCoordinate.toString());
            } else {
                Toast.makeText(m_activity.getApplicationContext(),
                        "ERROR:Place request returns error: " + errorCode, Toast.LENGTH_SHORT)
                        .show();
            }

        }
    };
}

