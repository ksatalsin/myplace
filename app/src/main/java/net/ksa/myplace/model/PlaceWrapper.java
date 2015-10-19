package net.ksa.myplace.model;

import android.net.Uri;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Locale;

public class PlaceWrapper implements Serializable {

    String id;

    ArrayList<Integer> placeTypes;

    String address;

    Locale locale;

    String nameame;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public ArrayList<Integer> getPlaceTypes() {
        return placeTypes;
    }

    public void setPlaceTypes(ArrayList<Integer> placeTypes) {
        this.placeTypes = placeTypes;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Locale getLocale() {
        return locale;
    }

    public void setLocale(Locale locale) {
        this.locale = locale;
    }

    public String getNameame() {
        return nameame;
    }

    public void setNameame(String nameame) {
        this.nameame = nameame;
    }

    public LatLng getLatLng() {
        return latLng;
    }

    public void setLatLng(LatLng latLng) {
        this.latLng = latLng;
    }

    public LatLngBounds getViewport() {
        return viewport;
    }

    public void setViewport(LatLngBounds viewport) {
        this.viewport = viewport;
    }

    public Uri getWebsiteUri() {
        return websiteUri;
    }

    public void setWebsiteUri(Uri websiteUri) {
        this.websiteUri = websiteUri;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public float getGetRating() {
        return getRating;
    }

    public void setGetRating(float getRating) {
        this.getRating = getRating;
    }

    public int getGetPriceLevel() {
        return getPriceLevel;
    }

    public void setGetPriceLevel(int getPriceLevel) {
        this.getPriceLevel = getPriceLevel;
    }

    LatLng latLng;

    LatLngBounds viewport;

    Uri websiteUri;

    String phoneNumber;

    float getRating;

    int getPriceLevel;
}
