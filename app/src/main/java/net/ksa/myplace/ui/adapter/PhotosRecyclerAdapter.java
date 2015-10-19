package net.ksa.myplace.ui.adapter;

import android.location.Location;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.places.PlacePhotoMetadata;
import com.google.android.gms.location.places.PlacePhotoResult;

import net.ksa.myplace.R;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;

public class PhotosRecyclerAdapter extends RecyclerView.Adapter<PhotosRecyclerAdapter.ViewHolder> {

    private ArrayList<PlacePhotoMetadata> mPhotos = new ArrayList<>();
    private GoogleApiClient mGoogleApiClient;

    public ArrayList<PlacePhotoMetadata> getData() {
        return mPhotos;
    }

    public ArrayList<PlacePhotoMetadata> getPhotos() {
        return mPhotos;
    }

    public void setPhotos(ArrayList<PlacePhotoMetadata> mPhotos) {
        this.mPhotos = mPhotos;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        @Bind(R.id.iv_photo)
        ImageView mPhoto;

        @Bind(R.id.tv_title)
        TextView mNameText;


        ResultCallback<PlacePhotoResult> mDisplayPhotoResultCallback
                = new ResultCallback<PlacePhotoResult>() {
            @Override
            public void onResult(PlacePhotoResult placePhotoResult) {
                if (!placePhotoResult.getStatus().isSuccess()) {
                    return;
                }
                mPhoto.setImageBitmap(placePhotoResult.getBitmap());
            }
        };


        public ViewHolder(View v) {
            super(v);
            ButterKnife.bind(this, v);
        }
    }

    @Override
    public void onViewRecycled(ViewHolder holder) {
        super.onViewRecycled(holder);
        holder.mPhoto.setImageResource(android.R.drawable.ic_menu_gallery);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final PlacePhotoMetadata item = mPhotos.get(position);

        item.getScaledPhoto(mGoogleApiClient, holder.mPhoto.getWidth(),
                holder.mPhoto.getHeight())
                .setResultCallback(holder.mDisplayPhotoResultCallback);

        CharSequence attribution = item.getAttributions();
        if (attribution == null) {
            holder.mNameText.setVisibility(View.GONE);
        } else {
            holder.mNameText.setVisibility(View.VISIBLE);
            holder.mNameText.setText(Html.fromHtml(attribution.toString()));
        }
    }

    public void add(int position, PlacePhotoMetadata item) {
        mPhotos.add(position, item);
        notifyItemInserted(position);
    }

    public void remove(PlacePhotoMetadata item) {
        int position = mPhotos.indexOf(item);
        mPhotos.remove(position);
        notifyItemRemoved(position);
    }

    public void removeAll() {
        mPhotos.clear();
    }

    public void add(PlacePhotoMetadata place) {
        mPhotos.add(place);
    }

    @Override
    public int getItemCount() {
        return mPhotos.size();
    }

    public PhotosRecyclerAdapter(ArrayList<PlacePhotoMetadata> photos, @NonNull GoogleApiClient googleApiClient, Location lastLocation) {
        if (photos != null)
            mPhotos = photos;
        mGoogleApiClient = googleApiClient;
    }

    @Override
    public PhotosRecyclerAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_place, parent, false);
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

}