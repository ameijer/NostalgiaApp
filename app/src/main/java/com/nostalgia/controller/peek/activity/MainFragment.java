/**
 * Copyright 2015-present Amberfog
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.nostalgia.controller.peek.activity;

import android.app.Activity;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.nostalgia.Nostalgia;
import com.nostalgia.controller.peek.picker.locationdisplayers.recycler.BaseRecyclerFragment;
import com.nostalgia.controller.peek.picker.locationdisplayers.recycler.LocationRecyclerAdapter;
import com.nostalgia.controller.peek.picker.locationdisplayers.recycler.model.KnownLocationWrapper;
import com.nostalgia.controller.peek.picker.mediadisplayers.recycler.CollectionAdapter;
import com.nostalgia.persistence.model.KnownLocation;
import com.nostalgia.persistence.repo.LocationRepository;
import com.nostalgia.runnable.DiscreteLocationBboxThread;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;
import com.vuescape.nostalgia.R;

import org.geojson.GeoJsonObject;
import org.geojson.GeometryCollection;
import org.geojson.Point;
import org.json.JSONException;

import java.sql.Wrapper;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MainFragment extends Fragment implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
        SlidingUpPanelLayout.PanelSlideListener, LocationListener, GoogleMap.OnMarkerClickListener, HeaderAdapter.ItemClickListener {
    public static final String TAG = "LocMapFrag";
    private static final String ARG_LOCATION = "arg.location";

    // private LockableListView mListView;
    private LockableRecyclerView mListView;
    private SlidingUpPanelLayout mSlidingUpPanelLayout;

    // ListView stuff
    //private View mTransparentHeaderView;
    //private View mSpaceView;
    private View mTransparentView;
    private View mWhiteSpaceView;

    private HeaderAdapter mHeaderAdapter;
    private LocationRepository locRepo;
    private LatLng mLocation;
    private Marker mLocationMarker;
    private List<KnownLocation> discretes = new ArrayList<KnownLocation>();
    private SupportMapFragment mMapFragment;
    private final HashMap<String, KnownLocation> plottedMarkers = new HashMap<String, KnownLocation>();
    private GoogleMap mMap;
    private boolean mIsNeedLocationUpdate = true;
    private Nostalgia app;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;

    private OnMapChangedListener mapCallback;
    private BaseRecyclerFragment.LocationPickedListener locationCallback;

    public interface OnMapChangedListener {
        void onMapChanged(double[] visibleArea);


    }

    public void setLocationPickedListener(BaseRecyclerFragment.LocationPickedListener listener){
        this.locationCallback = listener;
    }
    public void setOnMapChangedListener(OnMapChangedListener listener){
        this.mapCallback = listener;
    }
    public void updateLocations(List<KnownLocation> visibles) {
        discretes.clear();
        discretes.addAll(visibles);

        //plot points

        for(KnownLocation loc : discretes){
            if(loc.getLocation().getGeometry() instanceof GeometryCollection){
                GeometryCollection coll = (GeometryCollection) loc.getLocation().getGeometry();
                for(GeoJsonObject obj : coll.getGeometries()){
                    if(obj instanceof Point){
                        //then we have a plottable location
                        Point toPlot = (Point) obj;

                        LatLng pos = new LatLng(toPlot.getCoordinates().getLatitude(), toPlot.getCoordinates().getLongitude());
                        MarkerOptions mMarkerOptions = new MarkerOptions()
                                .anchor(0.5f, 0.5f)
                                .position(pos)
                                .title(loc.getName())
                                .icon( BitmapDescriptorFactory
                                        .defaultMarker(BitmapDescriptorFactory.HUE_RED));

                        plottedMarkers.put(mMap.addMarker(mMarkerOptions).getId(), loc);

                    }
                }
            }
        }



        new Handler().post(new Runnable() {
            @Override
            public void run() {
                mHeaderAdapter.notifyDataSetChanged();
            }
        });
    }
    public MainFragment() {
    }

    public static MainFragment newInstance(LatLng location) {
        MainFragment f = new MainFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_LOCATION, location);
        f.setArguments(args);
        return f;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        mListView = (LockableRecyclerView) rootView.findViewById(android.R.id.list);
        mListView.setOverScrollMode(ListView.OVER_SCROLL_NEVER);

        mSlidingUpPanelLayout = (SlidingUpPanelLayout) rootView.findViewById(R.id.slidingLayout);
        mSlidingUpPanelLayout.setEnableDragViewTouchEvents(true);

        int mapHeight = getResources().getDimensionPixelSize(R.dimen.map_height);
        mSlidingUpPanelLayout.setPanelHeight(mapHeight); // you can use different height here
        mSlidingUpPanelLayout.setScrollableView(mListView, mapHeight);

        mSlidingUpPanelLayout.setPanelSlideListener(this);

        // transparent view at the top of ListView
        mTransparentView = rootView.findViewById(R.id.transparentView);
        mWhiteSpaceView = rootView.findViewById(R.id.whiteSpaceView);

        // init header view for ListView
        // mTransparentHeaderView = inflater.inflate(R.layout.transparent_header_view, mListView, false);
        // mSpaceView = mTransparentHeaderView.findViewById(R.id.space);

        collapseMap();

        mSlidingUpPanelLayout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                mSlidingUpPanelLayout.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                mSlidingUpPanelLayout.onPanelDragged(0);
            }
        });

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mLocation = getArguments().getParcelable(ARG_LOCATION);
        if (mLocation == null) {
            mLocation = getLastKnownLocation(false);
        }

        mMapFragment = SupportMapFragment.newInstance();
        FragmentTransaction fragmentTransaction = getChildFragmentManager().beginTransaction();
        fragmentTransaction.add(R.id.mapContainer, mMapFragment, "map");
        fragmentTransaction.commit();

        // show white bg if there are not too many items
        mWhiteSpaceView.setVisibility(View.VISIBLE);

        // ListView approach
//        mListView.addHeaderView(mTransparentHeaderView);
//        mListView.setAdapter(new ArrayAdapter<String>(getActivity(), R.layout.simple_list_item, testData));
//        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                mSlidingUpPanelLayout.collapsePane();
//            }
//        });
        mHeaderAdapter = new HeaderAdapter(getContext(), discretes, this);
        mListView.setItemAnimator(null);
        final LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mListView.setLayoutManager(layoutManager);
        mListView.setAdapter(mHeaderAdapter);

        mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        setUpMapIfNeeded();
    }

    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = mMapFragment.getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                mMap.setMyLocationEnabled(false);
                mMap.getUiSettings().setCompassEnabled(false);
                mMap.getUiSettings().setZoomGesturesEnabled(false);
                mMap.getUiSettings().setScrollGesturesEnabled(false);
                mMap.getUiSettings().setZoomControlsEnabled(true);
                mMap.getUiSettings().setMyLocationButtonEnabled(false);
                mMap.setOnMarkerClickListener(this);
                LatLng update = getLastKnownLocation();
                if (update != null) {
                    mMap.moveCamera(CameraUpdateFactory.newCameraPosition(CameraPosition.fromLatLngZoom(update, 11.0f)));
                }
                mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                    @Override
                    public void onMapClick(LatLng latLng) {
                        mIsNeedLocationUpdate = false;
                        moveToLocation(latLng, false);
                    }
                });

                mMap.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {

                    private float currentZoom = -1;

                    @Override
                    public void onCameraChange(CameraPosition pos) {
                        if (pos.zoom != currentZoom) {
                            currentZoom = pos.zoom;
                            updateMapAfterZoom();
                        }
                    }
                });

            }
        }
    }
    private void updateMapAfterZoom() {

        LatLngBounds mapBounds = mMap.getProjection().getVisibleRegion().latLngBounds;

        if( mapBounds.southwest.longitude == 0.0 && mapBounds.southwest.latitude == 0.0 && mapBounds.northeast.longitude == 0.0 && mapBounds.northeast.latitude == 0.0 ){
            Log.i(TAG, "Empty lat/long, skipping point population");
            return;
        }
        //query db for all locations with points within the box (might need to do this server side)
        double[] bbox = new double[] {mapBounds.southwest.longitude, mapBounds.southwest.latitude, mapBounds.northeast.longitude, mapBounds.northeast.latitude};

        if(mapCallback != null){
            mapCallback.onMapChanged(bbox);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        app = ((Nostalgia) activity.getApplication());
        locRepo = app.getLocationRepository();

        try {
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " error casting app class");
        }

    }

    @Override
    public boolean onMarkerClick(Marker marker) {

        String id = marker.getId();
        marker.showInfoWindow();

        return true;
    }
    @Override
    public void onResume() {
        super.onResume();
        // In case Google Play services has since become available.
        setUpMapIfNeeded();
    }

    @Override
    public void onStart() {
        super.onStart();
        // Connect the client.
        mGoogleApiClient.connect();
    }

    @Override
    public void onStop() {
        // Disconnecting the client invalidates it.
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    private LatLng getLastKnownLocation() {
        return getLastKnownLocation(true);
    }

    private LatLng getLastKnownLocation(boolean isMoveMarker) {

        Location loc = app.getLocation();
        if (loc != null) {
            LatLng latLng = new LatLng(loc.getLatitude(), loc.getLongitude());
            if (isMoveMarker) {
                moveMarker(latLng);
            }
            return latLng;
        }
        return null;
    }

    private void moveMarker(LatLng latLng) {
        if (mLocationMarker != null) {
            mLocationMarker.remove();
        }
        mLocationMarker = mMap.addMarker(new MarkerOptions()
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_marker_my_location))
                .position(latLng).anchor(0.5f, 0.5f).draggable(false));
    }

    private void moveToLocation(Location location) {
        if (location == null) {
            return;
        }
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        moveToLocation(latLng);
    }

    private void moveToLocation(LatLng latLng) {
        moveToLocation(latLng, true);
    }

    private void moveToLocation(LatLng latLng, final boolean moveCamera) {
        if (latLng == null) {
            return;
        }
        moveMarker(latLng);
        mLocation = latLng;
        mListView.post(new Runnable() {
            @Override
            public void run() {
                if (mMap != null && moveCamera) {
                    mMap.moveCamera(CameraUpdateFactory.newCameraPosition(CameraPosition.fromLatLngZoom(mLocation, 11.0f)));
                }
            }
        });
    }

    private void collapseMap() {
        if (mHeaderAdapter != null) {
            mHeaderAdapter.showSpace();
        }
        mTransparentView.setVisibility(View.GONE);
        if (mMap != null && mLocation != null) {
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(mLocation, 11f), 1000, null);
        }
        mListView.setScrollingEnabled(true);
    }

    private void expandMap() {
        if (mHeaderAdapter != null) {
           mHeaderAdapter.hideSpace();
        }
        mTransparentView.setVisibility(View.INVISIBLE);
        if (mMap != null) {
            mMap.animateCamera(CameraUpdateFactory.zoomTo(14f), 1000, null);
        }
        mListView.setScrollingEnabled(false);
    }

    @Override
    public void onPanelSlide(View view, float v) {
    }

    @Override
    public void onPanelCollapsed(View view) {
        expandMap();
    }

    @Override
    public void onPanelExpanded(View view) {
        collapseMap();
    }

    @Override
    public void onPanelAnchored(View view) {

    }

    @Override
    public void onLocationChanged(Location location) {
        if (mIsNeedLocationUpdate) {
            moveToLocation(location);
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setNumUpdates(1);

        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    @Override
    public void onItemClicked(int position) {
        mSlidingUpPanelLayout.collapsePane();
    }
}
