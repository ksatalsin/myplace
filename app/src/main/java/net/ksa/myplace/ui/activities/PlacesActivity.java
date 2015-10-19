package net.ksa.myplace.ui.activities;

import android.content.Intent;
import android.content.IntentSender;
import android.content.res.Configuration;
import android.location.Location;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceLikelihood;
import com.google.android.gms.location.places.PlaceLikelihoodBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.PlacesStatusCodes;
import com.google.android.gms.location.places.ui.PlacePicker;

import net.ksa.myplace.BuildConfig;
import net.ksa.myplace.R;
import net.ksa.myplace.model.PlaceWrapper;
import net.ksa.myplace.ui.adapter.PlacesRecyclerAdapter;
import net.ksa.myplace.ui.listeners.RecyclerClickListener;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;

public class PlacesActivity extends AppCompatActivity
        implements GoogleApiClient.OnConnectionFailedListener,
        GoogleApiClient.ConnectionCallbacks, SearchView.OnQueryTextListener, RecyclerClickListener {

    private static final int PLACE_PICKER_REQUEST = 678;
    private static final int REQUEST_CHECK_SETTINGS = 679;
    private static final String ARG_KEY_SAVED_DATA = "saved_data";
    private final String TAG = getClass().getSimpleName();

    private GoogleApiClient mGoogleApiClient;
    private FloatingActionButton mFab;
    private Location mLastLocation;
    private LocationRequest mLocationRequest;

    @Bind(R.id.rv_places)
    RecyclerView mPlacesRecycler;

    @Bind(R.id.toolbar_actionbar)
    Toolbar mToolbar;

    private PlacesRecyclerAdapter mPlacesRecyclerAdapter;
    private ArrayList<PlaceWrapper> arrItems;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        //super rude quick move. Shoulde be ORM
        if (savedInstanceState != null)
            arrItems = (ArrayList<PlaceWrapper>) savedInstanceState.getSerializable(ARG_KEY_SAVED_DATA);

        initializeGoogleApiClient();
        initializeLocationRequest();
        initCurrentLocation();
        initializeToolBar();
        initializeFab();

        //have no time fo this :(
        // initializeNavigationDrawer();
    }

    @Override
    public void onClick(PlaceWrapper pw, Palette palette) {

        Intent i = new Intent(this, PlacesDetailsActivity.class);
        i.putExtra(PlacesDetailsActivity.ARG_KEY_PLACE_ID, pw.getId());
        i.putExtra(PlacesDetailsActivity.ARG_KEY_PLACE_NAME, pw.getNameame());
        i.putExtra(PlacesDetailsActivity.ARG_KEY_PLACE_LAT, pw.getLat());
        i.putExtra(PlacesDetailsActivity.ARG_KEY_PLACE_LNG, pw.getLng());

        if (palette != null) {
            i.putExtra(PlacesDetailsActivity.ARG_KEY_PRIMARY_COLOR, palette.getVibrantColor(getResources().getColor(R.color.primary)));
        }

        startActivity(i);
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        ArrayList<PlaceWrapper> data = mPlacesRecyclerAdapter.getData();
        if (data != null) {
            outState.putSerializable(ARG_KEY_SAVED_DATA, data);
        }
    }

    private void initCurrentLocation() {
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (mLastLocation == null) {
            checkSettings();
            return;
        }
        initializeRecycler();
    }

    private void initializeRecycler() {
        int orientation = this.getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
            mPlacesRecycler.setLayoutManager(linearLayoutManager);
        } else {
            GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 3);
            mPlacesRecycler.setLayoutManager(gridLayoutManager);
        }

        mPlacesRecycler.setHasFixedSize(true);
        mPlacesRecycler.setVerticalFadingEdgeEnabled(true);
        mPlacesRecycler.setVerticalScrollBarEnabled(true);

        mPlacesRecyclerAdapter = new PlacesRecyclerAdapter(arrItems, mGoogleApiClient, mLastLocation, this);
        mPlacesRecycler.setAdapter(mPlacesRecyclerAdapter);
    }

    private void initializeLocationRequest() {

        mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        mLocationRequest.setInterval(2000);
        mLocationRequest.setFastestInterval(4000);
        mLocationRequest.setNumUpdates(16);
    }

    private void initializeFab() {
        mFab = (FloatingActionButton) findViewById(R.id.fab);
        mFab.setOnClickListener(view -> guessCurrentPlace());
    }

    void guessCurrentPlace() {
        PendingResult<PlaceLikelihoodBuffer> result = Places.PlaceDetectionApi.getCurrentPlace(mGoogleApiClient, null);
        result.setResultCallback(likelyPlaces -> {

            Log.e(TAG, "guessCurrentPlace onResult: " + likelyPlaces.getStatus().getStatusCode());

            if (likelyPlaces.getStatus().getStatusCode() != PlacesStatusCodes.SUCCESS) {
                checkSettings();
                return;
            }

            mPlacesRecyclerAdapter.removeAll();

            for (PlaceLikelihood plh : likelyPlaces) {

                PlaceWrapper place = new PlaceWrapper();
                place.setNameame(plh.getPlace().getName().toString());
                place.setId(plh.getPlace().getId());
                place.setLat(plh.getPlace().getLatLng().latitude);
                place.setLng(plh.getPlace().getLatLng().longitude);
                if (plh.getPlace().getAddress() != null)
                    place.setAddress(plh.getPlace().getAddress().toString());

                mPlacesRecyclerAdapter.add(0, place);

                if (BuildConfig.DEBUG) {
                    String content = "";
                    if (plh != null && plh.getPlace() != null && !TextUtils.isEmpty(plh.getPlace().getName()))
                        content = "place: " + plh.getPlace().getName() + "\n";
                    if (plh != null)
                        content += "%: " + (int) (plh.getLikelihood() * 100) + "%";
                    Log.v(TAG, content);
                }
            }

            likelyPlaces.release();
        });
    }

    private void showSnackBarr(View v, String s) {
        Snackbar.make(v, s, Snackbar.LENGTH_LONG)
                //.setAction("act", null)
                .show();
    }

    private void showToast(String s) {
        Toast.makeText(this, s, Toast.LENGTH_LONG)
                .show();
    }

    private void initializeToolBar() {
        mToolbar = (Toolbar) findViewById(R.id.toolbar_actionbar);
        setSupportActionBar(mToolbar);
    }


    private void initializeGoogleApiClient() {

        mGoogleApiClient = new GoogleApiClient
                .Builder(this)
                .enableAutoManage(this, 0, this)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mGoogleApiClient != null)
            mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
        super.onStop();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(menu.findItem(R.id.menu_search));
        searchView.setOnQueryTextListener(this);
        return true;
    }

    private void runPlacePicker() {
        if (mGoogleApiClient == null || !mGoogleApiClient.isConnected())
            return;

        PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
        try {
            startActivityForResult(builder.build(getApplicationContext()), PLACE_PICKER_REQUEST);
        } catch (GooglePlayServicesRepairableException e) {
            checkSettings();

        } catch (GooglePlayServicesNotAvailableException e) {
            e.printStackTrace();
        }
    }

    private void checkSettings() {
        LocationSettingsRequest req = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequest)
                .build();

        PendingResult<LocationSettingsResult> result = LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient, req);

        result.setResultCallback(result1 -> {
            final Status status = result1.getStatus();

            Log.v(TAG, "checkSettings " + status.toString());

            switch (status.getStatusCode()) {
                case LocationSettingsStatusCodes.SUCCESS:

                    initCurrentLocation();
                    break;

                case LocationSettingsStatusCodes.INTERNAL_ERROR:
                case LocationSettingsStatusCodes.RESOLUTION_REQUIRED: {

                    try {
                        status.startResolutionForResult(PlacesActivity.this, REQUEST_CHECK_SETTINGS);
                    } catch (IntentSender.SendIntentException e) {
                        Log.e(TAG, e.getLocalizedMessage());
                    }
                    break;
                }
                case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                    Toast.makeText(
                            this,
                            "Service unavailable!",
                            Toast.LENGTH_LONG)
                            .show();
                    break;
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_pick_place:
                runPlacePicker();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PLACE_PICKER_REQUEST && resultCode == RESULT_OK) {

            try {
                Place p = PlacePicker.getPlace(data, this);

                PlaceWrapper place = new PlaceWrapper();
                place.setNameame(p.getName().toString());
                place.setId(p.getId());
                place.setLat(p.getLatLng().latitude);
                place.setLng(p.getLatLng().longitude);
                if (p.getAddress() != null)
                    place.setAddress(p.getAddress().toString());

                mPlacesRecyclerAdapter.removeAll();
                mPlacesRecyclerAdapter.add(0, place);

            } catch (Exception e) {
                e.printStackTrace();
            }


        } else if (requestCode == REQUEST_CHECK_SETTINGS) {
            if (resultCode == RESULT_OK) {

                initCurrentLocation();

            } else {
                Toast.makeText(
                        this,
                        "Location Services need to be enabled!",
                        Toast.LENGTH_LONG)
                        .show();
            }
        }
    }

    @Override
    public void onConnected(Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    @Override
    public boolean onQueryTextSubmit(String query) {

        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        return false;
    }
}
