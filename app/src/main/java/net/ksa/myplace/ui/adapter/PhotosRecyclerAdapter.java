package net.ksa.myplace.ui.adapter;

import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.DetectedActivity;
import com.google.android.gms.location.places.PlacePhotoMetadataBuffer;
import com.google.android.gms.location.places.PlacePhotoMetadataResult;
import com.google.android.gms.location.places.PlacePhotoResult;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.SphericalUtil;

import net.ksa.myplace.R;
import net.ksa.myplace.model.PlaceWrapper;
import net.ksa.myplace.ui.activities.PlacesActivity;
import net.ksa.myplace.ui.activities.PlacesDetailsActivity;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;

public class PhotosRecyclerAdapter extends RecyclerView.Adapter<PhotosRecyclerAdapter.ViewHolder> {
    private final Location mLastLocation;
    private ArrayList<PlaceWrapper> mPlaces = new ArrayList<>();
    private GoogleApiClient mGoogleApiClient;

    public ArrayList<PlaceWrapper> getData() {
        return mPlaces;
    }

    public ArrayList<PlaceWrapper> getmPlaces() {
        return mPlaces;
    }

    public void setmPlaces(ArrayList<PlaceWrapper> mPlaces) {
        this.mPlaces = mPlaces;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        @Bind(R.id.iv_photo)
        ImageView mPhoto;

        @Bind(R.id.iv_icon_place)
        ImageView mPlaceIcon;

        @Bind(R.id.tv_title)
        TextView mNameText;

        @Bind(R.id.tv_address)
        TextView mAddressText;

        @Bind(R.id.tv_distance)
        TextView mDistanceText;


        ResultCallback<PlacePhotoResult> mDisplayPhotoResultCallback
                = new ResultCallback<PlacePhotoResult>() {
            @Override
            public void onResult(PlacePhotoResult placePhotoResult) {
                if (!placePhotoResult.getStatus().isSuccess()) {
                    return;
                }
                mPhoto.setImageBitmap(placePhotoResult.getBitmap());

                Palette.from(placePhotoResult.getBitmap()).generate(new Palette.PaletteAsyncListener() {
                    @Override
                    public void onGenerated(Palette palette) {

                        if (palette == null)
                            return;

                        Drawable iconDrawable;
                        int color;

                        Resources resources = itemView.getResources();
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            iconDrawable = resources.getDrawable(R.drawable.ic_place, itemView.getContext().getTheme());
                            color = resources.getColor(R.color.secondary_text, itemView.getContext().getTheme());
                        } else {
                            iconDrawable = resources.getDrawable(R.drawable.ic_place);
                            color = resources.getColor(R.color.secondary_text);
                        }

                        if (iconDrawable != null) {
                            iconDrawable.setColorFilter(palette.getVibrantColor(color), PorterDuff.Mode.MULTIPLY);
                            mPlaceIcon.setBackground(iconDrawable);
                        }

                        if (resources.getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT)
                            mDistanceText.setTextColor(color);
                    }
                });
            }
        };

        void showDistance(LatLng latLng) {
            double distance = SphericalUtil.computeDistanceBetween(latLng, new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude()));
            mDistanceText.setText(formatNumber(distance));
        }

        private String formatNumber(double distance) {
            String unit = "м";
            if (distance < 1) {
                distance *= 1000;
                unit = "мм";
            } else if (distance > 1000) {
                distance /= 1000;
                unit = "км";
            }

            return String.format("%4.3f%s", distance, unit);
        }

        /**
         * Load a bitmap from the photos API asynchronously
         * by using buffers and result callbacks.
         *
         * @param id
         */
        void placePhotosAsync(String id) {
            Places.GeoDataApi.getPlacePhotos(mGoogleApiClient, id)
                    .setResultCallback(new ResultCallback<PlacePhotoMetadataResult>() {

                        @Override
                        public void onResult(PlacePhotoMetadataResult photos) {
                            if (!photos.getStatus().isSuccess()) {
                                return;
                            }

                            PlacePhotoMetadataBuffer photoMetadataBuffer = photos.getPhotoMetadata();
                            if (photoMetadataBuffer.getCount() > 0) {
                                photoMetadataBuffer.get(0)
                                        .getScaledPhoto(mGoogleApiClient, mPhoto.getWidth(),
                                                mPhoto.getHeight())
                                        .setResultCallback(mDisplayPhotoResultCallback);
                            }
                            photoMetadataBuffer.release();
                        }
                    });
        }

        public ViewHolder(View v) {
            super(v);
            ButterKnife.bind(this, v);
        }
    }

    @Override
    public void onViewRecycled(ViewHolder holder) {
        super.onViewRecycled(holder);
        holder.mPhoto.setImageResource(android.R.drawable.ic_menu_gallery);
        holder.mPlaceIcon.setImageDrawable(null);
        holder.mPlaceIcon.setImageResource(R.drawable.ic_place);
        holder.mDistanceText.setText("");
        holder.mAddressText.setText("");
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final PlaceWrapper item = mPlaces.get(position);

      /*  //
        holder.mPlaceIcon.setImageDrawable(null);
        holder.mPlaceIcon.setImageResource(R.drawable.ic_place);*/

        if (!TextUtils.isEmpty(item.getNameame()))
            holder.mNameText.setText(item.getNameame());

        if (!TextUtils.isEmpty(item.getAddress()))
            holder.mAddressText.setText(item.getAddress());

        holder.placePhotosAsync(item.getId());

        if (mLastLocation != null && item.getLatLng() != null)
            holder.showDistance(item.getLatLng());
    }

    public void add(int position, PlaceWrapper item) {
        mPlaces.add(position, item);
        notifyItemInserted(position);
    }

    public void remove(PlaceWrapper item) {
        int position = mPlaces.indexOf(item);
        mPlaces.remove(position);
        notifyItemRemoved(position);
    }

    public void removeAll() {
        mPlaces.clear();
    }

    public void add(PlaceWrapper place) {
        mPlaces.add(place);
    }

    @Override
    public int getItemCount() {
        return mPlaces.size();
    }

    public PhotosRecyclerAdapter(PlacesDetailsActivity.SavedData sd, @NonNull GoogleApiClient googleApiClient, Location lastLocation) {
        mLastLocation = lastLocation;
        if (sd != null && sd.getData() != null)
            mPlaces = sd.getData();
        mGoogleApiClient = googleApiClient;
    }

    @Override
    public PhotosRecyclerAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_place, parent, false);
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

}