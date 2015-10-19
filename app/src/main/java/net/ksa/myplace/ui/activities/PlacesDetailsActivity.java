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
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
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
import com.google.android.gms.location.places.PlaceLikelihood;
import com.google.android.gms.location.places.PlaceLikelihoodBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.PlacesStatusCodes;
import com.google.android.gms.location.places.ui.PlacePicker;

import net.ksa.myplace.BuildConfig;
import net.ksa.myplace.R;
import net.ksa.myplace.model.PlaceWrapper;
import net.ksa.myplace.ui.adapter.PhotosRecyclerAdapter;
import net.ksa.myplace.ui.adapter.PlacesRecyclerAdapter;
import net.ksa.myplace.ui.listeners.RecyclerClickListener;

import java.io.Serializable;
import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;

public class PlacesDetailsActivity extends AppCompatActivity
        implements GoogleApiClient.OnConnectionFailedListener,
        GoogleApiClient.ConnectionCallbacks, SearchView.OnQueryTextListener, RecyclerClickListener {

    private static final int REQUEST_CHECK_SETTINGS = 679;
    private static final String ARG_KEY_SAVED_DATA = "saved_data";
    private final String TAG = getClass().getSimpleName();

    private GoogleApiClient mGoogleApiClient;
    private FloatingActionButton mFab;
    private Location mLastLocation;
    private LocationRequest mLocationRequest;


    @Bind(R.id.rv_photos)
    RecyclerView mPhotoRecycler;

    @Bind(R.id.toolbar_actionbar)
    Toolbar mToolbar;
    private PhotosRecyclerAdapter mPhotoRecyclerAdapter;
    private SavedData mSD;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);
        ButterKnife.bind(this);

        //super rude and quick move
        if (savedInstanceState != null)
            mSD = (SavedData)savedInstanceState.getSerializable(ARG_KEY_SAVED_DATA);

        initializeGoogleApiClient();
        initializeLocationRequest();
        initializeToolBar();

        initializeRecycler();
    }

    @Override
    public void onClick(PlaceWrapper pw) {

    }

    public class SavedData implements Serializable{
        private ArrayList<PlaceWrapper> data;

        public ArrayList<PlaceWrapper> getData() {
            return data;
        }

        public void setData(ArrayList<PlaceWrapper> data) {
            this.data = data;
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        ArrayList<PlaceWrapper> data = mPhotoRecyclerAdapter.getData();

        SavedData sd = new SavedData();
        sd.setData(data);

        if(mPhotoRecyclerAdapter.getData()!=null)
            outState.putSerializable(ARG_KEY_SAVED_DATA, sd);
    }


    private void initializeRecycler() {
        int orientation = this.getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
            mPhotoRecycler.setLayoutManager(linearLayoutManager);
        } else {
            GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 3);
            mPhotoRecycler.setLayoutManager(gridLayoutManager);
        }

        mPhotoRecycler.setHasFixedSize(true);
        mPhotoRecycler.setVerticalFadingEdgeEnabled(true);
        mPhotoRecycler.setVerticalScrollBarEnabled(true);

        mPhotoRecyclerAdapter = new PhotosRecyclerAdapter(mSD, mGoogleApiClient, mLastLocation);
        mPhotoRecycler.setAdapter(mPhotoRecyclerAdapter);
    }

    private void initializeLocationRequest() {

        mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        mLocationRequest.setInterval(2000);
        mLocationRequest.setFastestInterval(4000);
        mLocationRequest.setNumUpdates(16);
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
        getMenuInflater().inflate(R.menu.detail, menu);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(menu.findItem(R.id.menu_map));
        searchView.setOnQueryTextListener(this);
        return true;
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
