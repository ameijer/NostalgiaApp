package com.nostalgia.controller.capturemoment.review.places;


import android.app.Activity;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Toast;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.nostalgia.Nostalgia;
import com.nostalgia.persistence.model.KnownLocation;
import com.nostalgia.persistence.model.User;
import com.nostalgia.persistence.repo.LocationRepository;
import com.nostalgia.persistence.repo.UserRepository;
import com.nostalgia.runnable.DiscreteLocationBboxThread;
import com.nostalgia.runnable.KnownLocationCreatorThread;
import com.nostalgia.runnable.UserAttributeUpdaterThread;
import com.vuescape.nostalgia.R;

import org.geojson.Feature;
import org.geojson.GeoJsonObject;
import org.geojson.GeometryCollection;
import org.geojson.Point;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by alex on 11/3/15.
 */
public class LocationCreationFragment extends Fragment implements GoogleMap.OnMarkerClickListener, ExistingLocationDialogFragment.ExistingLocationConfirmListener, NewLocationDialogFragment.NewLocationConfirmListener, SeekBar.OnSeekBarChangeListener, OnMapReadyCallback {

    public static final String TAG = "LocMapFrag";
    private Nostalgia app;

    public static final double METERS_PER_MILE = 1609.344;
    private LatLng point = null;
    private Circle locationFence;
    public static int MAX_RADIUS_MI = 1;
    private GoogleMap mMap;
    Marker currentLocation;

    private double maxZoomSupported;
    private List<KnownLocation> discretes = new ArrayList<KnownLocation>();
    private LocationRepository locRepo;
    private LatLngBounds mapVisibleBounds ;
    private final HashMap<String, KnownLocation> plottedMarkers = new HashMap<String, KnownLocation>();
    private double currentZoom;
    private FragmentManager mainFragmentManager;
    private UserRepository userRepo;
    private NewLocationDialogFragment newLocFrag = new NewLocationDialogFragment();
    private ExistingLocationDialogFragment existDialogFrag = new ExistingLocationDialogFragment();
    private SupportMapFragment mapFrag = new SupportMapFragment();
    private Fragment currentFocus;
    private final HashMap<String, KnownLocation> chosen = new HashMap<String, KnownLocation>();

    private OnLocationCreateListener mCallback;

    public void setCallback(OnLocationCreateListener callback) {
        this.mCallback = callback;
    }

    private LinearLayout mTutorialChangeSize;

    @Override
    public void onMapReady(GoogleMap googleMap) {
        showNewLocationCreateFragment();
        this.mMap = googleMap;

        // Check if we were successful in obtaining the map.
        if (mMap != null) {
            setUpMap();
        }

    }

    public interface OnLocationCreateListener{
        void onLocationsChosen(ArrayList<KnownLocation> selected);
        void onLocationCreated(KnownLocation created);
    }

    public static LocationCreationFragment newInstance(){
        Bundle args = new Bundle();
        LocationCreationFragment fragment = new LocationCreationFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        app = ((Nostalgia) activity.getApplication());

        locRepo = app.getLocationRepository();
        userRepo = app.getUserRepo();
        mainFragmentManager = getActivity().getSupportFragmentManager();
        newLocFrag.setSeekBarListener(this);

        try {
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " error casting app class");
        }

    }

    private void fillSurroundingLocations(){
        plottedMarkers.clear();
        //get bbox of currently visible map area
        LatLngBounds mapBounds = mMap.getProjection().getVisibleRegion().latLngBounds;

        if( mapBounds.southwest.longitude == 0.0 && mapBounds.southwest.latitude == 0.0 && mapBounds.northeast.longitude == 0.0 && mapBounds.northeast.latitude == 0.0 ){
            Log.i(TAG, "Empty lat/long, skipping point population");
            return;
        }
        //query db for all locations with points within the box (might need to do this server side)
        double[] bbox = new double[] {mapBounds.southwest.longitude, mapBounds.southwest.latitude, mapBounds.northeast.longitude, mapBounds.northeast.latitude};
        DiscreteLocationBboxThread bboxer = null;
        try {
            bboxer = new DiscreteLocationBboxThread(bbox);
        } catch (JSONException e) {
            Log.e(TAG, "error creating thread", e);
        }
        bboxer.start();
        try {
            bboxer.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        List<KnownLocation> rawLocs = bboxer.getMatching();

        if(rawLocs == null){
            String msg = "error getting locations in bbox, unable to draw known points";
            Log.e(TAG, msg);
            Toast.makeText(getActivity(), msg, Toast.LENGTH_LONG).show();
            return;
        }

        if(currentZoom > maxZoomSupported) {
            maxZoomSupported = currentZoom;
            discretes.clear();
            discretes.addAll(rawLocs);
        }
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

    }

    private int calculateZoomLevel(int screenWidth, float accuracy) {
        double equatorLength = 40075004; // in meters
        double metersPerPixel = equatorLength / 256;
        int zoomLevel = 1;
        while ((metersPerPixel * (double) screenWidth) > accuracy) {
            metersPerPixel /= 2;
            zoomLevel++;
        }

        return zoomLevel;
    }


    private void adjustZoom(){

        double radiusMi = (currentZoom * MAX_RADIUS_MI) / 100.00;
        double radiusMeter = radiusMi * METERS_PER_MILE;


        double diameterMeter = radiusMeter * 2;

        // Screen measurements
        DisplayMetrics metrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);

        // Use min(width, height) (to properly fit the screen
        int screenSize = Math.min(metrics.widthPixels, metrics.heightPixels);

        // Equators length
        long equator = 40075004;

        // The meters per pixel required to show the whole area the user might be located in
        double requiredMpp = diameterMeter/screenSize;

        // Calculate the zoom level
        double zoomLevel = ((Math.log(equator / (256 * requiredMpp))) / Math.log(2)) - 1.55;

        CameraUpdate cameraUpdate = CameraUpdateFactory.zoomTo((float) zoomLevel);
        mMap.animateCamera(cameraUpdate);
        fillSurroundingLocations();

    }

    private void drawUserRadius(){

        double doubleZoom = Math.max(currentZoom, 0.3);
        double radiusMi = (currentZoom * MAX_RADIUS_MI) / 100.00;

        double radiusMeter = radiusMi * METERS_PER_MILE;

        CircleOptions circleOptions = new CircleOptions()
                .center(point)
                .radius(radiusMeter).strokeColor(Color.rgb(0, 200, 190));
        // In meters

        if(locationFence != null){
            locationFence.remove();
        }

        if(mMap != null) {
            locationFence = mMap.addCircle(circleOptions);
        }

    }

    /**
     * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
     * installed) and the map has not already been instantiated.. This will ensure that we only ever
     * call {@link #setUpMap()} once when {@link #mMap} is not null.
     * <p/>
     * If it isn't installed {@link SupportMapFragment} (and
     * {@link com.google.android.gms.maps.MapView MapView}) will show a prompt for the user to
     * install/update the Google Play services APK on their device.
     * <p/>
     * A user can return to this FragmentActivity after following the prompt and correctly
     * installing/updating/enabling the Google Play services. Since the FragmentActivity may not
     * have been completely destroyed during this process (it is likely that it would only be
     * stopped or paused), {@link #onCreate(Bundle)} may not be called again so we should call this
     * method in {@link #onResume()} to guarantee that it will be called.
     */
    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
           this.mapFrag.getMapAsync(this);

        }
    }

    /**
     * This is where we can add markers or lines, add listeners or move the camera. In this case, we
     * just add a marker near Africa.
     * <p/>
     * This should only be called once and when we are sure that {@link #mMap} is not null.
     */
    private void setUpMap() {

        Location current = app.getLocation();
        User loggedIn = userRepo.getLoggedInUser();
        try {
            point = new LatLng(current.getLatitude(), current.getLongitude());
        } catch (Exception e){

            Log.e(TAG, "error getting location from gps. FALLING BACK TO LAST KNOWN", e);

            Point lastKnown = loggedIn.getLastKnownLoc();
            point = new LatLng(lastKnown.getCoordinates().getLatitude(), lastKnown.getCoordinates().getLongitude());
            current = new Location("StoredLocation");
            current.setLatitude(lastKnown.getCoordinates().getLatitude());
            current.setLongitude(lastKnown.getCoordinates().getLongitude());


        }

        locationListener.onMyLocationChange(current);

        mMap.setIndoorEnabled(true);
        mMap.setBuildingsEnabled(true);
        mMap.getUiSettings().setAllGesturesEnabled(false);
        mMap.getUiSettings().setMyLocationButtonEnabled(false);
        mMap.setMyLocationEnabled(false);

        float zoomFloat = 18;

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(point, zoomFloat));
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mMap.setOnMarkerClickListener(this);
        mMap.setOnMyLocationChangeListener(locationListener);
        mMap.getUiSettings().setZoomControlsEnabled(false);
        drawUserRadius();
        fillSurroundingLocations();
        setupMapGestures();
    }
    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        Bundle b = getArguments();

    }

    public enum TutorialType {
        CHANGE_SIZE
    }

    private void setupMapGestures(){
        mTutorialChangeSize.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mTutorialChangeSize.setVisibility(LinearLayout.GONE);
            }
        });

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                displayTutorial(TutorialType.CHANGE_SIZE);
            }
        });
    }

    private void displayTutorial(TutorialType type){
        if(type.equals(TutorialType.CHANGE_SIZE)){
            mTutorialChangeSize.setVisibility(LinearLayout.VISIBLE);
        }
    }

    GoogleMap.OnMyLocationChangeListener locationListener = new GoogleMap.OnMyLocationChangeListener() {

        @Override
        public void onMyLocationChange(Location location) {


            drawMarker(location);
        }

        private void drawMarker(Location location) {

            LatLng currentPosition = new LatLng(location.getLatitude(),
                    location.getLongitude());
            currentLocation = mMap.addMarker(new MarkerOptions()
                    .position(currentPosition)
                    .snippet(
                            "Lat:" + location.getLatitude() + "Lng:"
                                    + location.getLongitude())
                    .icon(BitmapDescriptorFactory
                            .defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
                    .title("position"));

        }

    };

    private void updateUserZoom() throws Exception {
        //update user zoom
        User loggedIn = userRepo.getLoggedInUser();

        boolean updateZoom = true;
        String zoom = loggedIn.getSettings().get("zoom_level");
        if(zoom != null){

            double preferredZoom = Double.parseDouble(zoom);
            if(preferredZoom == currentZoom){
                updateZoom = false;
            }

        }

        if (updateZoom) {
            loggedIn.getSettings().put("zoom_level", Double.toString(currentZoom));

            Map<String, String> changed = new HashMap<>();
            changed.put("zoom_level", Double.toString(currentZoom));
            UserAttributeUpdaterThread updatr = new UserAttributeUpdaterThread(loggedIn.get_id(), UserAttributeUpdaterThread.Attribute.SETTING, changed);
            updatr.start();
            userRepo.save(loggedIn);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = super.onCreateView(inflater, container, savedInstanceState);

        View myView = inflater.inflate(R.layout.fragment_location_create, container, false);
        mTutorialChangeSize = (LinearLayout) myView.findViewById(R.id.tutorial_change_size);

        showMapFragment();

        return myView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setUpMapIfNeeded();
    }

    private void showMapFragment() {
        FragmentTransaction fragTransaction = getChildFragmentManager().beginTransaction();
        fragTransaction.replace(R.id.location_create_map_frag_holder, this.mapFrag, "locationdialog");

        fragTransaction.commit();
        getChildFragmentManager().executePendingTransactions();

    }
    @Override
    public boolean onMarkerClick(Marker marker) {

        String id = marker.getId();

        if(currentLocation != null) {
            if (id.equals(currentLocation.getId())){

                return true;
            }
        }
        if(chosen.keySet().contains(id)){
            //remove
            chosen.remove(id);
        } else {
            //add
            KnownLocation selected = plottedMarkers.get(id);
            chosen.put(id, selected);
        }

        if(chosen.keySet().size() > 0){

            showExistingLocationPickerFragment();
            this.existDialogFrag.getDisplay().clear();
            this.existDialogFrag.getDisplay().addAll(chosen.values());
            this.existDialogFrag.setOnConfirmedListener(this);
            this.existDialogFrag.notifyChanged();
            // Open the info window for the marker
            marker.showInfoWindow();
        } else {
            showNewLocationCreateFragment();
        }


        return true;
    }

    private void showNewLocationCreateFragment() {
        if(currentFocus != null){
            if(currentFocus instanceof NewLocationDialogFragment){
                Log.d(TAG, "new location fragment already displaying, not adding again");
                return;
            }
        }
        currentFocus = this.newLocFrag;
        FragmentTransaction fragTransaction = mainFragmentManager.beginTransaction();
        fragTransaction.replace(R.id.location_dialog_holder, this.newLocFrag, "locationdialog");
        fragTransaction.commit();
        newLocFrag.setOnConfirmedListener(this);
    }

    private void showExistingLocationPickerFragment() {

        if(currentFocus != null){
            if(currentFocus instanceof ExistingLocationDialogFragment){
                Log.d(TAG, "existing location fragment already displaying, not adding again");
                return;
            }
        }

        currentFocus = this.existDialogFrag;
        FragmentTransaction fragTransaction = mainFragmentManager.beginTransaction();
        fragTransaction.replace(R.id.location_dialog_holder, existDialogFrag, "existingdialog");
        fragTransaction.commit();
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {


        //implement scaled zooming
        if(progress < 20) {
            currentZoom = progress / 4.0;
        } else if (progress < 50){
            currentZoom = progress / 3.0;
        } else if(progress < 80){
            currentZoom = progress / 2.0;
        } else {
            currentZoom = progress;
        }


        //update map, circle radius
        drawUserRadius();

        adjustZoom();

    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        if(null != mTutorialChangeSize) {
            mTutorialChangeSize.setVisibility(LinearLayout.GONE);
        }
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }




    private double[][] computePolygon(){


        final double MILES_PER_DEGREE_LAT = 69.2;

        double radiusMi = (currentZoom * MAX_RADIUS_MI) / 100.00;
        double latDeltaDeg = radiusMi / MILES_PER_DEGREE_LAT;


        if(point == null){
            String msg = "location required";
            Log.e(TAG, msg);
            Toast.makeText(getActivity(), msg, Toast.LENGTH_LONG).show();
            return null;
        }

        double curLat = point.latitude;
        final double MILES_PER_DEGREE_LONG = (Math.PI / 180.00) * 3963.1676 * Math.cos(curLat);

        double longDeltaDeg = radiusMi / MILES_PER_DEGREE_LONG;


        double[] topLeft = new double[]{point.longitude - longDeltaDeg, point.latitude + latDeltaDeg};
        double[] topRight = new double[]{point.longitude + longDeltaDeg, point.latitude + latDeltaDeg};
        double[] bottomLeft = new double[]{point.longitude - longDeltaDeg, point.latitude - latDeltaDeg};
        double[] bottomRight = new double[]{point.longitude + longDeltaDeg, point.latitude - latDeltaDeg};

        double[][] result = new double[][]{topLeft, topRight, bottomLeft, bottomRight};


        return result;
    }

    private KnownLocation createLocation(String name)  {
        User loggedIn = userRepo.getLoggedInUser();

        if(loggedIn == null){
            String msg = "error - must be logged in to create location";
            Log.e(TAG, msg);
            Toast.makeText(getActivity(), msg, Toast.LENGTH_LONG).show();
            return null;
        }

        String textName = name;
        if(textName == null || textName.length() < 3){
            String msg = "invalid location name";
            Log.e(TAG, msg);
            Toast.makeText(getActivity(), msg, Toast.LENGTH_LONG).show();
            return null;
        }


        KnownLocation newLoc = new KnownLocation();
        newLoc.setName(textName);

        newLoc.getProperties().put("CREATOR_NAME", userRepo.getLoggedInUser().getUsername());

        double[][] polygon = this.computePolygon();
        if(polygon == null){
            return null;
        }
        StringBuffer polyString = new StringBuffer();

        //skip last one
        for(int i = 0; i < polygon.length - 1; i++){
            double[] tempCoord = polygon[i];
            String cur = "                 [" + tempCoord[0] + ", " + tempCoord[1] +"],\n";
            polyString.append(cur);
        }

        //add in last coord (has no comma at end)
        double[] tempCoord = polygon[polygon.length - 1];
        String cur = "                 [" + tempCoord[0] + ", " + tempCoord[1] +"]\n";
        polyString.append(cur);

        Feature object = null;
        try {
            String pointAsString =
                    "{\n" +
                            "  \"type\": \"Feature\",\n" +
                            "  \"properties\": {\n" +
                            "    \"name\": \"" + name + "\",\n" +
                            "    \"radius\":" + ((currentZoom * MAX_RADIUS_MI) / 100.00)  +"\n" +
                            "  },\n" +
                            "  \"geometry\": {\n" +
                            "    \"type\": \"GeometryCollection\",\n" +
                            "     \"geometries\": [\n" +
                            "        { \"type\": \"Polygon\",\n"  +
                            "          \"coordinates\": [\n" +
                            "              [\n" +
                            polyString.toString() +
                            "              ]\n" +
                            "            ]\n" +
                            "       }";

            //if we have a more accurate point, store that as well
            if(point!= null) {
                pointAsString += ",\n" +
                        " { \"type\": \"Point\",\n" +
                        "          \"coordinates\": [" + point.longitude +"," + point.latitude +"]\n" +
                        "        }\n";

            }


            pointAsString +=  "  ]\n" +
                    "  }\n" +
                    "}   ";

            object=   new ObjectMapper().readValue(pointAsString, Feature.class);
        } catch (Exception e) {
            e.printStackTrace();
        }


        newLoc.setCreatorId(loggedIn.get_id());
        newLoc.setLocation(object);


        KnownLocationCreatorThread creator = new KnownLocationCreatorThread(newLoc);
        creator.start();
        try {
            creator.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        KnownLocation added = creator.getAdded();

        if(null != mCallback){
            mCallback.onLocationCreated(added);
        } else {
            Log.w(TAG, "No callback set for LocationCreationFragment.");
        }

        return added;
    }

    @Override
    public void onExistingLocationsSelected(ArrayList<KnownLocation> selected) {
        if(mCallback != null){
            mCallback.onLocationsChosen(selected);
        }
    }

    @Override
    public void onNewLocationsSelected(KnownLocation created) {

        try {
            updateUserZoom();
        } catch (Exception e){
            Log.e(TAG, "ERROR SAVING USER");
        }

        //add accurate point
        KnownLocation actual = this.createLocation(created.getName());


        //quit activity, manual add new location into selector fragment
        ArrayList<KnownLocation> creates = new ArrayList<KnownLocation>();
        creates.add(actual);
        if(mCallback != null){
            mCallback.onLocationsChosen(creates);
        }
    }
}
