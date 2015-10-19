package net.ksa.myplace.ui.activities;

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
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.PlacePhotoMetadata;
import com.google.android.gms.location.places.PlacePhotoMetadataBuffer;
import com.google.android.gms.location.places.PlacePhotoMetadataResult;
import com.google.android.gms.location.places.PlacePhotoResult;
import com.google.android.gms.location.places.Places;

import net.ksa.myplace.R;
import net.ksa.myplace.model.PlaceWrapper;
import net.ksa.myplace.ui.adapter.PhotosRecyclerAdapter;
import net.ksa.myplace.ui.listeners.RecyclerClickListener;

import java.io.Serializable;
import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;

public class PlacesDetailsActivity extends AppCompatActivity
        implements GoogleApiClient.OnConnectionFailedListener,
        GoogleApiClient.ConnectionCallbacks, RecyclerClickListener {

    private static final int REQUEST_CHECK_SETTINGS = 679;
    private static final String ARG_KEY_SAVED_DATA = "saved_data";
    public static final String ARG_KEY_PRIMARY_COLOR = "prmary_color";
    public static final String ARG_KEY_PLACE_ID = "place_id";
    public static final String ARG_KEY_PLACE_NAME = "place_name";
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
    private String mPlaceId;
    private String mName;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);
        ButterKnife.bind(this);

        mName = getIntent().getStringExtra(ARG_KEY_PLACE_NAME);
        mPlaceId = getIntent().getStringExtra(ARG_KEY_PLACE_NAME);

        initializeGoogleApiClient();
        initializeLocationRequest();
        initializeToolBar();
        initializeRecycler();
        initializePhotos();
    }

    /**
     * Load a bitmap from the photos API asynchronously
     * by using buffers and result callbacks.
     */
    private void initializePhotos() {
        Places.GeoDataApi.getPlacePhotos(mGoogleApiClient, mPlaceId)
                    .setResultCallback(new ResultCallback<PlacePhotoMetadataResult>() {

                        @Override
                        public void onResult(PlacePhotoMetadataResult photos) {
                            if (!photos.getStatus().isSuccess()) {
                                return;
                            }

                            PlacePhotoMetadataBuffer photoMetadataBuffer = photos.getPhotoMetadata();
                            if (photoMetadataBuffer.getCount() > 0) {
                                PlacePhotoMetadata placePhotoMetadata = photoMetadataBuffer.get(0);

                                mPhotoRecyclerAdapter.add(0, placePhotoMetadata);
                                //Log.v(TAG, "Photo loaded");
                            }
                            photoMetadataBuffer.release();
                        }
                    });

    }

    @Override
    public void onClick(PlaceWrapper pw, Palette mPalette) {

    }

    public class SavedData implements Serializable{
        private ArrayList<PlacePhotoMetadata> data;

        public ArrayList<PlacePhotoMetadata> getData() {
            return data;
        }

        public void setData(ArrayList<PlacePhotoMetadata> data) {
            this.data = data;
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        ArrayList<PlacePhotoMetadata> data = mPhotoRecyclerAdapter.getData();

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

        getSupportActionBar().setTitle(mName);
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
}
