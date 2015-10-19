package net.ksa.myplace.model;

import java.io.Serializable;

public class PlaceWrapper implements Serializable {

    String id;
    String address;
    String nameame;
    double lat;
    double lng;
    String phoneNumber;
    float getRating;
    int getPriceLevel;

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getNameame() {
        return nameame;
    }

    public void setNameame(String nameame) {
        this.nameame = nameame;
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


}
